/***************************************************************************
 * Pushdown Engine for Kubling Connectors
 * --------------------------------------
 * This module implements the generic pushdown logic used by the
 * Kubling Connector SDK. It determines which parts of a query can be
 * executed by the upstream API directly, and which parts must be handled
 * locally by Kubling as a post-filter step.
 *
 * Goals:
 *   - Identify usable filters for API calls
 *   - Select the most efficient field for pushdown (priority-based)
 *   - Support both single-call and multi-call strategies
 *   - Fall back to "NONE" cleanly when pushdown is not possible
 *
 * This module contains NO API-specific logic. It is fully generic.
 ***************************************************************************/

/* -------------------------------------------------------------------------
 * Utility: Detect whether the query has no filters at all.
 *
 * qfJson is the raw parsed QueryFilter JSON provided by Kubling.
 * A query with no filters is usually invalid for API-backed connectors.
 * (Handled at a higher level in sdkSelect.)
 * ---------------------------------------------------------------------- */
export function hasNoFilters(qfJson) {
    return !qfJson.filters || qfJson.filters.length === 0;
}

/* -------------------------------------------------------------------------
 * Convert the Kubling filter JSON into a usable map structure:
 *
 * Example input:
 *     filters: [
 *         { field: "orderId", operation: "EQUAL", value: 123 },
 *         { field: "status",  operation: "EQUAL", value: "OK" }
 *     ]
 *
 * Output:
 *     {
 *         orderId: [{ op: "EQUAL", val: 123 }],
 *         status:  [{ op: "EQUAL", val: "OK"  }]
 *     }
 *
 * This map is later consumed by computePushdown().
 * ---------------------------------------------------------------------- */
export function buildFilterMap(qfJson) {
    const map = {};

    if (!qfJson.filters) return map;

    for (const f of qfJson.filters) {
        const field = f.field;
        const op = f.operation;
        const val = f.value;

        if (!map[field]) map[field] = [];
        map[field].push({ op, val });
    }

    return map;
}

/* -------------------------------------------------------------------------
 * computePushdown()
 * -----------------
 * Determines the optimal pushdown strategy based on:
 *   - The filters provided in filterMap
 *   - The pushdown capabilities defined for the connector
 *
 * CAPABILITIES format:
 *   {
 *       priority: ["orderId", "status", ...],
 *       fields: {
 *           orderId: ["EQUAL", ...],
 *           status:  ["EQUAL", "IN"],
 *           ...
 *       },
 *       multiCallFields: ["orderId"]
 *   }
 *
 * Algorithm:
 *   1) Iterate fields in priority order.
 *   2) If the field exists in both filterMap and capabilities:
 *   3) Extract only the supported operators.
 *   4) Choose between:
 *        - MULTI_CALL  → multiple calls (one per value)
 *        - SINGLE_CALL → one call with a single filter
 *   5) If none matches → return { strategy: "NONE" }
 *
 * NOTE:
 *   This function NEVER throws errors for missing filters. Validation
 *   is handled at the SDK level, not here.
 * ---------------------------------------------------------------------- */
export function computePushdown(filterMap, CAPABILITIES) {

    const priority = CAPABILITIES.priority || [];

    for (let i = 0; i < priority.length; i++) {
        const field = priority[i];
        const caps = CAPABILITIES.fields[field];

        if (!caps) continue;          // field not supported
        if (!filterMap[field]) continue; // no filter for this field

        const supported = [];
        const flist = filterMap[field];

        // Filter operators supported by this field
        for (let j = 0; j < flist.length; j++) {
            const f = flist[j];
            if (caps.indexOf(f.op) >= 0) {
                supported.push(f);
            }
        }

        if (supported.length === 0) continue;

        /* -------------------------------------------------------------
         * MULTI-CALL STRATEGY:
         *   Example:
         *      WHERE orderId IN (10, 20, 30)
         *   →  call API once per orderId
         * ---------------------------------------------------------- */
        if (CAPABILITIES.multiCallFields.indexOf(field) >= 0) {
            const values = [];
            for (let k = 0; k < supported.length; k++) {
                values.push(supported[k].val);
            }

            return {
                strategy: "MULTI_CALL",
                field: field,
                values: values
            };
        }

        /* -------------------------------------------------------------
         * SINGLE-CALL STRATEGY:
         *   Use the first supported operator-value pair.
         *   Most REST APIs only support one filter at a time.
         * ---------------------------------------------------------- */
        return {
            strategy: "SINGLE_CALL",
            opts: (() => {
                const o = {};
                o[field] = supported[0].val;
                return o;
            })()
        };
    }

    // No usable pushdown
    return { strategy: "NONE" };
}

/* -------------------------------------------------------------------------
 * runPushdownSync()
 * -----------------
 * Executes the pushdown strategy using the given API adapter.
 *
 * The adapter must implement:
 *     API.list(opts)
 *
 * Strategies:
 *   - NONE:       → full scan (API.list({}))
 *   - SINGLE_CALL → one invocation
 *   - MULTI_CALL  → N invocations (aggregate results)
 *
 * NOTE:
 *   This function is synchronous because Kubling executes modules
 *   in a synchronous runtime (GraalJS).
 * ---------------------------------------------------------------------- */
export function runPushdownSync(push, API) {

    if (push.strategy === "NONE") {
        return API.list({});
    }

    if (push.strategy === "SINGLE_CALL") {
        return API.list(push.opts);
    }

    if (push.strategy === "MULTI_CALL") {
        let all = [];
        for (let i = 0; i < push.values.length; i++) {
            const v = push.values[i];
            const opts = {};
            opts[push.field] = v;
            const r = API.list(opts);

            if (r && r.length) {
                for (let k = 0; k < r.length; k++) {
                    all.push(r[k]);
                }
            }
        }
        return all;
    }

    throw "Unknown pushdown strategy";
}
