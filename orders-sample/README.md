## Transaction Strategies Compared

This module contains the **same logical test executed in two different variants**.
The BPMN process and the service logic are identical; the difference lies entirely in how
**Kubling Soft Transactions** are configured.

The purpose of these tests is to illustrate how transaction strategy selection at the data-plane
level affects **side-effects, compensation behavior, and failure semantics**, without changing
the process model.

---

### Immediate Operation Strategy

The default test uses Kubling Soft Transactions with the
`IMMEDIATE_OPERATION` strategy.

With this strategy:

- Each operation is applied immediately to the underlying data source
- Kubling does **not retain** the operation internally
- The global transaction is still active, but side-effects may already exist

For data sources that natively support transactions (for example
`EMBEDDED_INMEM`, backed by an optimized H2-based engine), this is usually safe,
as rollback semantics are delegated to the underlying system.

For API-based data sources, however:

- Operations are executed immediately against the remote API
- Side-effects become externally visible
- In case of failure, Kubling must rely on its **internal compensation mechanism**
  to revert previously applied operations

The process itself remains unchanged; compensation is handled entirely inside
the Kubling engine.

---

### Deferred Operation Strategy

The alternative test case, `ProcessOrdersWithDeferredStrategyTest`, uses the
`DEFER_OPERATION` strategy.

With this strategy:

- All operations are captured and retained by Kubling
- Changes are stored in a local, lightweight MVCC engine
- No remote data source is affected during process execution
- Operations are materialized only during the final `commit`

As a result:

- If a failure occurs before the transaction reaches `commit`,
  no remote operation is ever executed
- If a failure occurs during `commit`, while materializing a previously
  registered operation, execution stops at that point
- Subsequent operations are never applied
- The number of side-effects is strictly bounded and deterministic

---

### Why This Distinction Matters

Both tests execute the same BPMN flow and the same service logic.
The only difference is **when** Kubling chooses to materialize operations.

This demonstrates that:

- Process complexity does not need to increase to handle non-transactional systems
- Compensation logic does not need to be modeled at the BPMN level
- Transaction semantics can be controlled entirely at the data-plane level

In practice, choosing between `IMMEDIATE_OPERATION` and `DEFER_OPERATION`
is a trade-off between early visibility of changes and strict control over
side-effects.
