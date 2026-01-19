/***********************************************************************
 * Payments Handler (using Kubling SDK)
 ***********************************************************************/

import PaymentsApi, { FILTER_MAPPING, PAYMENTS_CAPABILITIES } from "../client/payment/api/PaymentsApi";

import { sdkSelect, sdkInsert, sdkUpdate, sdkDelete } from "../utils/kubling-sdk";

const ctx = "Orders.Payments";

/***********************************************************************
 * PaymentsAPI Adapter
 * - Encapsulates field mapping and HTTP error handling
 * - Specific to the external API, NOT to Kubling itself
 ***********************************************************************/
function PaymentsAPI(api, fieldMap) {

    function mapFields(obj) {
        const mapped = {};
        for (const k in obj) {
            mapped[fieldMap[k] || k] = obj[k];
        }
        return mapped;
    }

    function parseResponse(resp) {

        logger.debug(ctx, "PaymentsAPI status=" + resp.statusCode);

        if (resp.statusCode >= 400) {

            if (resp.statusCode === 404)
                throw new Error("Payment 404 Not Found → " + resp.content);

            if (resp.statusCode === 409)
                throw new Error("Payment conflict (409) → " + resp.content);

            if (resp.statusCode === 422)
                throw new Error("Invalid Payment payload (422) → " + resp.content);

            if (resp.statusCode === 500)
                throw new Error("Payment service error (500) → " + resp.content);

            throw new Error("Payments API (" + resp.statusCode + ") → " + resp.content);
        }

        if (!resp.content)
            return null;

        return JSON.parse(resp.content);
    }

    return {

        /* SELECT */
        list(opts) {
            const payload = mapFields(opts);
            logger.debug(ctx, "PaymentsAPI.list → " + JSON.stringify(payload));
            return parseResponse(api.listPayments(payload)) || [];
        },

        /* INSERT */
        create(row) {
            const payload = mapFields(row);
            logger.debug(ctx, "PaymentsAPI.create → " + JSON.stringify(payload));
            return parseResponse(api.createPayment(payload));
        },

        /* UPDATE (PATCH incremental) */
        patch(orderId, patchObj) {
            const payload = mapFields(patchObj);
            logger.debug(ctx, "PaymentsAPI.patch → " + JSON.stringify(payload));
            return parseResponse(api.partialUpdatePayment(orderId, payload));
        },

        /* DELETE */
        delete(orderId) {
            logger.debug(ctx, "PaymentsAPI.delete → orderId=" + orderId);
            return parseResponse(api.deletePayment(orderId));
        }
    };
}

/***********************************************************************
 * Main handler
 ***********************************************************************/
export default {

    /* -----------------------------------------------------------------
     * SELECT
     * ----------------------------------------------------------------*/
    select(queryFilter, resultSet) {

        logger.debug(ctx, queryFilter.json);
        const payments = PaymentsAPI(new PaymentsApi(), queryFilter.sourceFieldNames);

        const rows = sdkSelect(
            queryFilter,
            PAYMENTS_CAPABILITIES,
            payments,
            FILTER_MAPPING,
            false,
            ctx
        );

        resultSet.dataFormat("JSON");
        for (const row of rows) {
            logger.debug(ctx, JSON.stringify(row));
            resultSet.addRow(JSON.stringify(row));
        }
    },

    /* -----------------------------------------------------------------
     * INSERT
     * ----------------------------------------------------------------*/
    insert(insertOperation, affectedRows) {
        
        const payments = PaymentsAPI(new PaymentsApi(), insertOperation.sourceFieldNames);

        logger.debug(ctx, insertOperation.transaction.insideTransaction);
        logger.debug(ctx, insertOperation.transaction.transactionId);
        logger.debug(ctx, insertOperation.transaction.isRollingBack);
        logger.debug(ctx, insertOperation.transaction.isInPreCommitPhase);

        sdkInsert(insertOperation, row => {
            const respObject = payments.create(row);
            affectedRows.addGeneratedKey("externalTxId", respObject.externalTxId)
            affectedRows.increment();
        }, ctx);
    },

    /* -----------------------------------------------------------------
     * UPDATE
     * ----------------------------------------------------------------*/
    update(updateOperation, affectedRows) {

        const payments = PaymentsAPI(new PaymentsApi(), updateOperation.sourceFieldNames);
        logger.debug(ctx, updateOperation.jsonList.stringify())
        logger.debug(ctx, updateOperation.updates.stringify())

        logger.debug(ctx, updateOperation.transaction.insideTransaction);
        logger.debug(ctx, updateOperation.transaction.transactionId);
        logger.debug(ctx, updateOperation.transaction.isRollingBack);
        logger.debug(ctx, updateOperation.transaction.isInPreCommitPhase);

        sdkUpdate(updateOperation, (original, patch) => {

            const orderId = original.orderId;

            logger.debug(ctx, "PATCH orderId=" + orderId +
                " patch=" + JSON.stringify(patch));

            payments.patch(orderId, patch);
            affectedRows.increment();

        }, ctx);
    },

    /* -----------------------------------------------------------------
     * DELETE
     * ----------------------------------------------------------------*/
    delete(deleteOperation, affectedRows) {

        const payments = PaymentsAPI(new PaymentsApi(), deleteOperation.sourceFieldNames);

        logger.debug(ctx, deleteOperation.transaction.insideTransaction);
        logger.debug(ctx, deleteOperation.transaction.transactionId);
        logger.debug(ctx, deleteOperation.transaction.isRollingBack);
        logger.debug(ctx, deleteOperation.transaction.isInPreCommitPhase);

        sdkDelete(deleteOperation, doc => {

            logger.debug(ctx, "DELETE orderId=" + doc.orderId);

            payments.delete(doc.orderId);
            affectedRows.increment();

        }, ctx);
    }
};
