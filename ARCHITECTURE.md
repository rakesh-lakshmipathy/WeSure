# Architecture

## Dependency rule

```
Compose UI → ViewModel → domain use cases → repository contract ← data implementation
                                                   ↓
                                      Room database + remote API
```

Dependencies point inward: presentation never imports Room or the API, and the domain layer has no Android framework dependency. Hilt modules are the composition root that connects implementations to contracts.

## Offline-first data flow

Room is the single local source of truth. Screens observe Room through `PayrollRepository`; they never render a network response directly.

1. Create, update, or delete changes Room first.
2. The same action is recorded in `sync_operations` in the Room database.
3. The repository attempts to replay queued actions against `PayrollApi`.
4. If connectivity fails, the outbox stays on disk. A unique WorkManager job retries it only when the OS reports network connectivity, with exponential backoff; `refresh()` provides a user-initiated retry.
5. Successful operations are removed from the outbox. Remote payrolls are then merged into Room.

This means the user can close or restart the app while offline without losing payroll changes.
Write commands return `WriteResult`: a local database failure is distinct from a successful local
write whose remote sync is pending. The UI can therefore continue after a locally durable action
while still honestly notifying the user that it will be retried.

## Boundaries

- `feature/payroll/domain/model`: immutable business models.
- `feature/payroll/domain`: tax policy, repository contract, and use cases.
- `feature/payroll/data/local`: Room entities, DAO, relation mappings, and migrations.
- `feature/payroll/data/DefaultPayrollRepository`: offline-first repository implementation.
- `feature/payroll/data/PayrollApi`: replaceable transport contract; `InMemoryPayrollApi` is the mock.
- `di`: Hilt bindings and storage providers. The mock API is injected through the same contract a Retrofit implementation will use.
- `feature/payroll/sync`: WorkManager worker that replays the persistent outbox under network constraints.
- `feature/payroll/presentation`: payroll ViewModel and presentation state.
- `feature/payroll/ui`: Compose screens and feature UI coordination.
- Firebase Analytics records payroll lifecycle events; Crashlytics receives unexpected local-write
  failures with an operation key. Neither is used for employee names or wage values.

## Presentation-state rule

The ViewModel exposes long-lived observable state (`payrolls` and `error`) and a separate
one-off event stream for completed commands. The UI owns navigation and reacts to those events;
the ViewModel never receives a UI callback or imports a navigation type. Form values and the
current destination use saveable Compose state, so configuration changes preserve the user's
in-progress work without putting UI objects into the database.

## Production evolution

The in-memory API can be replaced with Retrofit without changing presentation or domain code. For a multi-team application, these boundaries can become Gradle modules (`:core:domain`, `:core:data`, `:feature:payroll`). The current single-module layout keeps the assessment easy to run while preserving those module seams.
