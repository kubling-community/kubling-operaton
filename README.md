# kubling-operaton

The focus of this repository is to explore and document architectural patterns that emerge
when Kubling is used as the primary data source for an Operaton engine.

Rather than presenting a single prescriptive integration, the project is structured to 
highlight data-plane design choices, transactional semantics, and behavioral trade-offs
that become visible when Operaton is embedded on top of Kubling.

---

## Motivation

Kubling can play **two complementary roles** when used as the main data source for Operaton:

### 1. Kubling as the Operaton Data Plane

Operaton assumes a single relational database for its internal state (runtime, history, metadata, jobs, etc.).

Kubling makes it possible to:

- Split the **internal Operaton schema** across multiple physical data sources
- Keep logical groups of data, like runtime, history, and metadata in different backends
- Compose them into a **single logical schema**
- Expose that schema to Operaton through **one JDBC connection**

From Operaton’s perspective, it is still interacting with **a single database**, 
while Kubling transparently federates and routes the data underneath.

This allows experimentation with:
- Physical separation of Operaton tables without modifying the engine
- Different storage strategies per concern (runtime vs history)
- Data-plane-level architecture changes without touching engine internals.

---

### 2. Kubling as a Unified Transactional Data Source

Kubling can also be used as a **single logical database** that represents multiple heterogeneous systems:

- Relational databases
- External APIs
- Legacy systems

When modeled through Kubling, all these sources appear as **tables in one schema** and can participate in a **soft transaction**.

From the process perspective:
- All operations happen against **one database**
- There is no need to implement the SAGA pattern
- Transactional consistency is handled at the data plane level

This allows BPMN processes to remain simple and expressive, while integration complexity is pushed down into Kubling.

---

## Repository Structure
```
kubling-operaton
├── test-helpers
├── operaton-dataplane
├── orders-sample
└── vdb
```

### `test-helpers`

Common utilities and helpers used to bootstrap **Testcontainers-based environments**, shared across the examples.

---

### `operaton-dataplane`

A focused example showing how **Kubling can be used as the data source for the Operaton engine itself**.

In this module:

- Operaton’s internal model is physically split across **three databases**:
    - metadata
    - runtime
    - history
- Kubling composes those sources into a **single virtual schema**
- Operaton is configured to use Kubling as its only JDBC data source

The goal is to demonstrate how Operaton can remain unchanged while its data plane is fully virtualized.

---

### `orders-sample`

A simple but realistic transactional process example.

This module demonstrates:

- A BPMN process that touches multiple data sources
- No SAGA pattern
- No compensating processes
- No orchestration glue code

All involved systems (orders, inventory, payments, etc.) are modeled in Kubling, so from the process point of view everything happens against **a single database**.

The Operaton instance used in this module **also runs on top of Kubling for its internal data plane**, making this a full end-to-end example.

---

### `vdb`

Contains the **Kubling VDB bundles** used by the examples.

They are kept in a separate directory to make:
- Exploration easier
- Data modeling clearer
- Reuse across modules simpler

---

## Running the Examples

Both `operaton-dataplane` and `orders-sample` include **simple integration tests** that can be executed directly.

### Important notes

It is recommended to run the tests **one test class at a time**.

Spring’s test context caching may otherwise reuse application contexts across classes, 
which can lead to misleading results when working with embedded engines and containerized infrastructure.

`orders-sample` module tests start multiple containers 
(2 for the Operaton data plane plus 3 additional containers for the order flow).  
Make sure your environment has sufficient CPU and memory resources before running them.

---

### Future work

From Camunda 8 onwards, the engine architecture moved decisively toward an event-based model,
primarily to address scalability concerns.

While event-driven architectures are a proven approach, many of the challenges they address
are rooted not only in execution semantics, but also in the structure and scalability of the
data plane itself.

A natural next step for this project is to explore whether similar scalability characteristics
can be achieved with embedded engines by rethinking the data plane: separating
straight-through processing, wait states, and historical data at the storage level.

The hypothesis is that, by pushing these concerns into the data plane, it may be possible to
retain embedded execution and ephemeral instances without sacrificing scalability.

That said, contributions and discussions are always welcome.
