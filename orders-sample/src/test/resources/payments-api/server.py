from flask import Flask, request, jsonify
import datetime
import random
import uuid

app = Flask(__name__)

# VOLATILE IN-MEMORY DATABASE
PAYMENTS = {}
NEXT_ID = 1


# ------------------------------------------
# UTILITIES
# ------------------------------------------

def utc_now():
    return datetime.datetime.utcnow().replace(microsecond=0).isoformat() + "Z"


def simulate_failure(probability=0.0):
    """Simulate random upstream failures, useful for testing transactions."""
    if random.random() < probability:
        return jsonify({"error": "Simulated upstream failure"}), 500
    return None


# ------------------------------------------
# GET /payments
# ------------------------------------------

@app.route("/payments", methods=["GET"])
def list_payments():
    # Optional failure simulation
    fail = simulate_failure(0.00)  # Adjust for stress tests
    if fail:
        return fail

    orderId = request.args.get("orderId")
    status = request.args.get("status")
    since = request.args.get("since")

    results = []

    for p in PAYMENTS.values():
        ok = True

        if orderId and str(p["orderId"]) != orderId:
            ok = False
        if status and p["status"] != status:
            ok = False
        if since and p["timestamp"] < since:
            ok = False

        if ok:
            results.append(p)

    return jsonify(results)


# ------------------------------------------
# GET /payments/{id}/status
# ------------------------------------------

@app.route("/payments/<int:orderId>/status", methods=["GET"])
def payment_status(orderId):
    if orderId not in PAYMENTS:
        return jsonify({"error": "Payment not found"}), 404

    p = PAYMENTS[orderId]

    return jsonify({
        "orderId": p["orderId"],
        "status": p["status"],
        "externalTxId": p.get("externalTxId")
    })


# ------------------------------------------
# POST /payments
# ------------------------------------------

@app.route("/payments", methods=["POST"])
def create_payment():
    global NEXT_ID

    body = request.json
    if not body:
        return jsonify({"error": "Invalid JSON"}), 422

    # Required fields
    required_fields = ["amount", "currency", "orderId"]
    for f in required_fields:
        if f not in body:
            return jsonify({"error": f"{f} is required"}), 422

    orderId = int(body["orderId"])

    # Conflict: already exists
    if orderId in PAYMENTS:
        return jsonify({"error": "Payment already exists"}), 409

    payment = {
        "orderId": orderId,
        "status": body.get("status", "PENDING"),
        "amount": body["amount"],
        "currency": body["currency"],
        "externalTxId": body.get("externalTxId") or str(uuid.uuid4()),
        "timestamp": utc_now()
    }

    PAYMENTS[orderId] = payment

    return jsonify(payment), 201


# ------------------------------------------
# PUT /payments/{id}
# ------------------------------------------

@app.route("/payments/<int:orderId>", methods=["PUT"])
def update_payment(orderId):
    if orderId not in PAYMENTS:
        return jsonify({"error": "Payment not found"}), 404

    body = request.json
    if not body:
        return jsonify({"error": "Invalid JSON"}), 422

    # Optional conflict simulation
    if body.get("conflict") == True:
        return jsonify({"error": "Simulated conflict"}), 409

    # Replace full document except orderId
    updated = {
        "orderId": orderId,
        "status": body.get("status", PAYMENTS[orderId]["status"]),
        "amount": body.get("amount", PAYMENTS[orderId]["amount"]),
        "currency": body.get("currency", PAYMENTS[orderId]["currency"]),
        "externalTxId": body.get("externalTxId", PAYMENTS[orderId].get("externalTxId")),
        "timestamp": utc_now()
    }

    PAYMENTS[orderId] = updated
    return jsonify(updated)


# ------------------------------------------
# PATCH /payments/{id}
# ------------------------------------------

@app.route("/payments/<int:orderId>", methods=["PATCH"])
def partial_update_payment(orderId):
    if orderId not in PAYMENTS:
        return jsonify({"error": "Payment not found"}), 404

    body = request.json
    if not body:
        return jsonify({"error": "Invalid JSON"}), 422

    # Optional simulated conflict
    if body.get("conflict") == True:
        return jsonify({"error": "Simulated conflict"}), 409

    # Apply only the fields provided
    for k, v in body.items():
        if k != "orderId":
            PAYMENTS[orderId][k] = v

    PAYMENTS[orderId]["timestamp"] = utc_now()

    return jsonify(PAYMENTS[orderId])


# ------------------------------------------
# DELETE /payments/{id}
# ------------------------------------------

@app.route("/payments/<int:orderId>", methods=["DELETE"])
def delete_payment(orderId):
    if orderId not in PAYMENTS:
        return jsonify({"error": "Payment not found"}), 404

    # Optional simulated conflict
    if request.args.get("conflict") == "true":
        return jsonify({"error": "Simulated deletion conflict"}), 409

    del PAYMENTS[orderId]
    return jsonify({"deleted": True})


# ------------------------------------------
# ENTRYPOINT
# ------------------------------------------

if __name__ == "__main__":
    print("Fake Payment API (EXTENDED, VOLATILE, TX-FAILURES ENABLED) listening on :8080")
    app.run(host="0.0.0.0", port=8080)
