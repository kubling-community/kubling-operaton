import OrderIssuesApi, { ORDER_ISSUES_CAPABILITIES, FILTER_MAPPING } from "../client/issues/api/OrderIssuesApi";

import { sdkSelect, sdkInsert, sdkUpdate, sdkDelete } from "../utils/kubling-sdk";

const ctx = "Orders.Issues";

function OrderIssuesAPI(api, fieldMap) {

    function mapFields(obj) {
        const mapped = {};
        for (const k in obj) {
            mapped[fieldMap[k] || k] = obj[k];
        }
        return mapped;
    }

    function parseResponse(resp, acceptNotFound) {

        logger.debug(ctx, "OrderIssuesAPI status=" + resp.statusCode);
        logger.debug(ctx,
            "acceptNotFound value=" + acceptNotFound +
            " type=" + typeof acceptNotFound
        );

        if (resp.statusCode === 404 && acceptNotFound === true) {
            return null;
        }

        if (resp.statusCode >= 400) {

            const errorMessages = {
                404: "Issue 404 Not Found",
                409: "Issue conflict (409)",
                422: "Invalid Issue payload (422)",
                500: "Issue service error (500)"
            };

            const baseMessage =
                errorMessages[resp.statusCode] ||
                `Issues API (${resp.statusCode})`;

            throw new Error(`${baseMessage} → ${resp.content}`);
        }

        if (!resp.content)
            return null;

        return JSON.parse(resp.content);
    }

    return {

        /* -----------------------------------------------------------------
         * SELECT
         * ----------------------------------------------------------------*/
        list(opts) {
            const payload = mapFields(opts);

            logger.debug(ctx, "OrderIssuesAPI.list → " + JSON.stringify(payload));

            if (payload.id) {
                const resp = api.issuesIdGet(payload.id);
                return parseResponse(resp, true) || [];
            }

            if (payload.from || payload.to) {
                const resp = api.issuesByDateGet(payload);
                return parseResponse(resp, true) || [];
            }

            if (payload.orderId) {
                const resp = api.ordersOrderIdIssuesGet(payload.orderId);
                return parseResponse(resp, true) || [];
            }

            //
            // Case 3: /issues (w/o filters)
            //
            const resp = api.issuesGet();
            return parseResponse(resp, true) || [];
        },


        /* -----------------------------------------------------------------
         * INSERT → POST /issues
         * ----------------------------------------------------------------*/
        create(row) {
            const payload = mapFields(row);
            logger.debug(ctx, "OrderIssuesAPI.create → " + JSON.stringify(payload));
            const resp = api.issuesPost(payload);
            return parseResponse(resp);
        },

        /* UPDATE (PATCH incremental) */
        patch(orderId, patchObj) {
            const payload = mapFields(patchObj);
            logger.debug(ctx, "OrderIssuesAPI.patch → " + JSON.stringify(payload));
            return parseResponse(api.partialUpdateIssue(orderId, payload));
        },

        /* -----------------------------------------------------------------
         * DELETE → DELETE /issues/{id}
         * ----------------------------------------------------------------*/
        delete(id) {
            const resp = api.issuesIdDelete(id);
            return parseResponse(resp);
        },

    };
}

/***********************************************************************
 * Main handler
 ***********************************************************************/
export default {

    /* SELECT */
    select(queryFilter, resultSet) {

        logger.debug(queryFilter.json)
        const issues = OrderIssuesAPI(
            new OrderIssuesApi(),
            queryFilter.sourceFieldNames
        );

        const rows = sdkSelect(
            queryFilter,
            ORDER_ISSUES_CAPABILITIES,
            issues,
            FILTER_MAPPING,
            false,
            ctx
        );

        const rowsArray = Array.isArray(rows) ? rows : (rows ? [rows] : []);

        resultSet.dataFormat("JSON");

        for (const row of rowsArray) {
            logger.debug(ctx, JSON.stringify(row));
            resultSet.addRow(JSON.stringify(row));
        }
    },

    /* INSERT */
    insert(insertOperation, affectedRows) {

        logger.debug(ctx, "OrderIssuesAPI.insert → Insert Operation Signal Received");

        const issues = OrderIssuesAPI(
            new OrderIssuesApi(),
            insertOperation.sourceFieldNames
        );

        sdkInsert(insertOperation, row => {
            const respObject = issues.create(row);
            affectedRows.addGeneratedKey("id", respObject.id)
            affectedRows.increment();
        }, ctx);
    },

    /* DELETE */
    delete(deleteOperation, affectedRows) {

        const issues = OrderIssuesAPI(
            new OrderIssuesApi(),
            deleteOperation.sourceFieldNames
        );

        sdkDelete(deleteOperation, doc => {
            issues.delete(doc.id);
            affectedRows.increment();
        }, ctx);
    },

    update(updateOperation, affectedRows) {

        const issues = OrderIssuesAPI(
            new OrderIssuesApi(),
            updateOperation.sourceFieldNames
        );

        sdkUpdate(updateOperation, (original, patch) => {

            const id = original.id;

            logger.debug(ctx,
                "PATCH id=" + id +
                " patch=" + JSON.stringify(patch)
            );

            issues.patch(id, patch);
            affectedRows.increment();

        }, ctx);
    }
};
