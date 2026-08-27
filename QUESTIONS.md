# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
I would refactor the warehouse persistence layer first so it follows one consistent pattern instead of mixing Panache repository logic, direct JPA entity manipulation, and ad-hoc domain logic across use cases and resources. Centralizing validation and persistence behind repository/use-case boundaries makes it easier to test, reason about, and evolve. The Store and Product endpoints are simpler and already work with Panache directly, but the Warehouse side has enough business rules that it benefits from a single domain service/repository contract.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Generated API interfaces are a strong fit when the external contract is stable and shared across teams, because they reduce drift between the contract and implementation and give a consistent request/response model. The downside is that generated code can feel rigid and sometimes needs custom wrappers or manual tuning when domain logic differs from the raw schema. For this project, I would keep generated interfaces for the Warehouse API, but still keep explicit resource classes and domain validation in the service layer so the generated contract remains a boundary rather than the only source of behavior.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would prioritize unit tests for validation rules and use cases, then API-level tests for the main business workflows, and finally a small set of integration tests for persistence boundaries. For this project, the most valuable tests are location validation, warehouse creation and replacement rules, store transaction behavior, and the CRUD flows for Product and Store. To keep coverage effective, I would keep the rules near the domain/use-case layer and test them with stable, deterministic inputs, then add a small regression suite around each business rule so changes to validation or persistence are caught early.
```