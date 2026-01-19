/***************************************************************************
 * Kubling Connector SDK
 * ----------------------
 * This SDK provides the common building blocks required to implement
 * table handlers inside Kubling JavaScript (ECMAScript) data-source modules.
 *
 * Its goal is to:
 *   - Minimize boilerplate code
 *   - Ensure consistent behavior across connectors
 *   - Reduce mistakes in SELECT / INSERT / UPDATE / DELETE handlers
 *   - Normalize source-field mapping and pushdown strategies
 *
 * The SDK is fully compatible with the Kubling runtime and its execution
 * model (ECMAScript, synchronous execution, polyglot objects, etc.).
 *
 * You can use this SDK in any custom Kubling connector to avoid rewriting
 * the same logic for every API integration.
 ***************************************************************************/

import { buildFilterMap, computePushdown, hasNoFilters, runPushdownSync } from "./pushdown";

/**
 * Utility: ensures an object is a ListProxyObject (Kubling JS type).
 * Throws a readable error otherwise.
 */
function ensureListProxy(obj, errorMsg) {
    if (!obj || typeof obj.array !== "function") {
        throw new Error(errorMsg);
    }
    return obj;
}

/**
 * Parses the `sourceFieldNames` map provided by Kubling.
 *
 * Example:
 *    "a=b;c=d;e=f"
 *
 * becomes:
 *    { a: "b", c: "d", e: "f" }
 *
 * This mapping represents:
 *   tableField → sourceFieldInAPI
 */
function parseSourceFieldNames(raw) {
    if (!raw || typeof raw.stringify !== "function")
        return {};

    const str = raw.stringify();
    const map = {};
    if (!str) return map;

    const parts = str.split(";");
    for (const p of parts) {
        if (!p) continue;
        const [k, v] = p.split("=");
        if (k && v) map[k] = v;
    }
    return map;
}

/**
 * Generic mapper:
 *   For every key in `obj`, remap it using the mapping table.
 *
 * Useful for:
 *   - tableField → jsonField
 *   - jsonField → apiQueryParam
 */
function mapWithSourceNames(obj, sourceFieldNames) {
    const mapped = {};
    for (const k in obj) {
        mapped[sourceFieldNames[k] || k] = obj[k];
    }
    return mapped;
}

/* -------------------------------------------------------------------------
 * SELECT
 * ---------------------------------------------------------------------- */
/**
 * Handles SELECT logic for a Kubling data-source.
 *
 * Responsibilities:
 *   1) Extract filters from the Kubling query
 *   2) Map table fields to API fields using sourceFieldNames
 *   3) Optionally apply an additional mapping for API filter-parameter names
 *   4) Compute the pushdown strategy
 *   5) Execute the strategy through the adapter
 *
 * Arguments:
 *   - queryFilter    : Kubling QueryFilterProxyObject
 *   - capabilities   : Pushdown capabilities definition for this API
 *   - adapter        : Object implementing .list(opts)
 *   - filterMapping  : Optional additional mapping (jsonField → queryParam)
 *   - ctx            : Logging context prefix
 */
export function sdkSelect(queryFilter, capabilities, adapter, filterMapping, allowsListAll, ctx) {

    ctx = ctx || "Kubling.SDK";

    const qfJson = JSON.parse(queryFilter.json);
    const filterMap = buildFilterMap(qfJson);

    // Step 1 — Map table fields → JSON-level API fields
    let mappedFilters = mapWithSourceNames(filterMap, queryFilter.sourceFieldNames);

    // Step 2 — Map JSON fields → query-parameter names (optional)
    if (filterMapping) {
        mappedFilters = mapWithSourceNames(mappedFilters, filterMapping);
    }

    // Step 3 — Compute pushdown feasibility and strategy
    const strategy = computePushdown(mappedFilters, capabilities);

    // Step 4 — Validate that the query has meaningful filters
    if (!allowsListAll && (hasNoFilters(qfJson) || strategy.strategy === "NONE")) {
        throw new Error(ctx + " → Query requires filters (none provided)");
    }

    // Step 5 — Execute the strategy and return the result rows
    return runPushdownSync(strategy, adapter);
}

/* -------------------------------------------------------------------------
 * INSERT
 * ---------------------------------------------------------------------- */
/**
 * Handles INSERT logic for a single document.
 *
 * Kubling guarantees:
 *   - Only one row is inserted at a time
 *   - `insertOperation.jsonList` contains exactly one JSON document
 *
 * This SDK:
 *   - Parses the JSON
 *   - Remaps table fields → API fields
 *   - Calls the provided createFn(mappedRow)
 */
export function sdkInsert(insertOperation, createFn, ctx) {

    ctx = ctx || "Kubling.SDK";

    const json = insertOperation.jsonList.stringify();
    const row = JSON.parse(json);

    const mapped = mapWithSourceNames(row, insertOperation.sourceFieldNames);

    createFn(mapped);
}

export function sdkUpdate(updateOperation, patchFn, ctx) {

    ctx = ctx || "Kubling.SDK";

    const docs = ensureListProxy(
        updateOperation.jsonList,
        ctx + " → UPDATE: invalid jsonList"
    ).array();

    const updates = ensureListProxy(
        updateOperation.updates,
        ctx + " → UPDATE: invalid updates"
    ).array();

    // No matching documents
    if (docs.length === 0) {
        logger.debug(ctx, "UPDATE: no documents match WHERE → no-op");
        return;
    }

    // Kubling guarantee: updates.length === docs.length
    if (updates.length !== docs.length) {
        throw new Error(ctx + " → UPDATE mismatch: updates.length != jsonList.length");
    }

    const sourceNames = parseSourceFieldNames(updateOperation.sourceFieldNames);

    for (let i = 0; i < docs.length; i++) {

        const original = JSON.parse(docs[i]);

        // ⚠ updates[i] is a ListProxyObject of SetOperationProxyObject
        const setOps = ensureListProxy(
            updates[i],
            ctx + " → UPDATE per-document ops invalid"
        ).array();

        const patch = {};

        for (const op of setOps) {
            const sourceKey = op.fieldName;
            const apiKey = sourceNames[sourceKey] || sourceKey;
            patch[apiKey] = op.value;
        }

        logger.debug(ctx, "PATCH for orderId=" + original.orderId +
            " => " + JSON.stringify(patch));

        patchFn(original, patch);
    }
}


/***************************************************************************
 * DELETE
 ***************************************************************************/
/**
 * Handles DELETE logic for documents.
 *
 * Kubling provides:
 *   - jsonList: list of full documents that matched the WHERE clause
 *
 * This SDK:
 *   - Parses each document
 *   - Applies the source-field mapping
 *   - Calls deleteFn(mappedRow)
 */
export function sdkDelete(deleteOperation, deleteFn, ctx) {

    ctx = ctx || "Kubling.SDK";

    const docs = ensureListProxy(deleteOperation.jsonList, ctx + " → DELETE invalid jsonList").array();
    const sourceNames = deleteOperation.sourceFieldNames;

    for (const docStr of docs) {
        const doc = JSON.parse(docStr);
        const mapped = mapWithSourceNames(doc, sourceNames);
        deleteFn(mapped);
    }
}

/***************************************************************************
 * End of SDK
 ***************************************************************************/
