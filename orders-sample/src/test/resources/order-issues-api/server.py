from flask import Flask, request, jsonify
import datetime
import random

app = Flask(__name__)

# VOLATILE IN-MEMORY DATABASE
ISSUES = {}
NEXT_ID = 1


# ------------------------------------------
# UTILITIES
# ------------------------------------------

def utc_now():
    return datetime.datetime.utcnow().replace(microsecond=0).isoformat() + "Z"


def simulate_failure(probability=0.0):
    """Simulate random upstream failures, useful for testing transaction boundaries."""
    if random.random() < probability:
        return jsonify({"error": "Simulated upstream failure"}), 500
    return None


# ------------------------------------------
# GET /issues
# ------------------------------------------

@app.route("/issues", methods=["GET"])
def list_issues():
    print("Listing issues")
    # Optional failure simulation
    fail = simulate_failure(0.00)
    if fail:
        return fail

    orderId = request.args.get("orderId")
    severity = request.args.get("severity")
    since = request.args.get("since")

    results = []

    for issue in ISSUES.values():
        ok = True

        if orderId and str(issue["orderId"]) != orderId:
            ok = False
        if severity and issue["severity"] != severity:
            ok = False
        if since and issue["timestamp"] < since:
            ok = False

        if ok:
            results.append(issue)

    return jsonify(results)


# ------------------------------------------
# GET /issues/by-date?from=&to=
# ------------------------------------------

@app.route("/issues/by-date", methods=["GET"])
def issues_by_date():
    fail = simulate_failure(0.00)
    if fail:
        return fail

    date_from = request.args.get("from")
    date_to = request.args.get("to")

    results = []

    for issue in ISSUES.values():
        ts = issue["timestamp"]

        ok = True
        if date_from and ts < date_from:
            ok = False
        if date_to and ts > date_to:
            ok = False

        if ok:
            results.append(issue)

    return jsonify(results)


# ------------------------------------------
# GET /issues/{id}
# ------------------------------------------

@app.route("/issues/<int:issueId>", methods=["GET"])
def get_issue(issueId):
    if issueId not in ISSUES:
        return jsonify({"error": "Issue not found"}), 404

    return jsonify(ISSUES[issueId])


# ------------------------------------------
# GET /orders/{orderId}/issues
# ------------------------------------------

@app.route("/orders/<int:orderId>/issues", methods=["GET"])
def get_issues_by_order(orderId):
    results = [i for i in ISSUES.values() if i["orderId"] == orderId]
    return jsonify(results)


# ------------------------------------------
# POST /issues
# ------------------------------------------

@app.route("/issues", methods=["POST"])
def create_issue():
    global NEXT_ID

    body = request.json
    if not body:
        return jsonify({"error": "Invalid JSON"}), 422

    required = ["orderId", "errorCode", "errorMessage", "severity"]
    for f in required:
        if f not in body:
            return jsonify({"error": f"{f} is required"}), 422

    issue = {
        "id": NEXT_ID,
        "orderId": int(body["orderId"]),
        "errorCode": body["errorCode"],
        "errorMessage": body["errorMessage"],
        "severity": body["severity"],
        "timestamp": utc_now()
    }

    ISSUES[NEXT_ID] = issue
    NEXT_ID += 1

    return jsonify(issue), 201


# ------------------------------------------
# PUT /issues/{id}
# (full replacement except id)
# ------------------------------------------

@app.route("/issues/<int:issueId>", methods=["PUT"])
def update_issue(issueId):
    if issueId not in ISSUES:
        return jsonify({"error": "Issue not found"}), 404

    body = request.json
    if not body:
        return jsonify({"error": "Invalid JSON"}), 422

    # Optional simulated conflict
    if body.get("conflict") is True:
        return jsonify({"error": "Simulated conflict"}), 409

    updated = {
        "id": issueId,
        "orderId": body.get("orderId", ISSUES[issueId]["orderId"]),
        "errorCode": body.get("errorCode", ISSUES[issueId]["errorCode"]),
        "errorMessage": body.get("errorMessage", ISSUES[issueId]["errorMessage"]),
        "severity": body.get("severity", ISSUES[issueId]["severity"]),
        "timestamp": utc_now()
    }

    ISSUES[issueId] = updated
    return jsonify(updated)


# ------------------------------------------
# PATCH /issues/{id}
# ------------------------------------------

@app.route("/issues/<int:issueId>", methods=["PATCH"])
def partial_update_issue(issueId):
    if issueId not in ISSUES:
        return jsonify({"error": "Issue not found"}), 404

    body = request.json
    if not body:
        return jsonify({"error": "Invalid JSON"}), 422

    # Optional simulated conflict
    if body.get("conflict") is True:
        return jsonify({"error": "Simulated conflict"}), 409

    for k, v in body.items():
        if k != "id":
            ISSUES[issueId][k] = v

    ISSUES[issueId]["timestamp"] = utc_now()

    return jsonify(ISSUES[issueId])


# ------------------------------------------
# DELETE /issues/{id}
# ------------------------------------------

@app.route("/issues/<int:issueId>", methods=["DELETE"])
def delete_issue(issueId):
    if issueId not in ISSUES:
        return jsonify({"error": "Issue not found"}), 404

    # Optional conflict simulation
    if request.args.get("conflict") == "true":
        return jsonify({"error": "Simulated deletion conflict"}), 409

    del ISSUES[issueId]
    return jsonify({"deleted": True})


# ------------------------------------------
# ENTRYPOINT
# ------------------------------------------

if __name__ == "__main__":
    print("Fake Order Issues API (VOLATILE, TX-FAILURES ENABLED) listening on :8081")
    app.run(host="0.0.0.0", port=8081)
