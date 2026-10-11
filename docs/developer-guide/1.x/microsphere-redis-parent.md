# microsphere-redis-parent — Developer Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-parent` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Layer | Build: inheritance parent (pom) |

## Verified Facts (from `git show origin/release-1.x:pom.xml` and module poms)

- Root aggregator: `microsphere-redis`, parent `microsphere-spring-cloud-parent:0.1.26`,
  `revision=0.1.15-SNAPSHOT`.
- `microsphere-redis-parent/pom.xml` imports BOM `microsphere-spring-cloud-dependencies:0.1.26`
  (property `microsphere-spring-cloud.version`, line 23) — the **Spring Boot 2.x / Spring Data Redis
  2.7.x-era** stack, which is what pins this line to Boot 2.
- **No `java.version` override** in the 1.x root properties (unlike 0.2.x's `17`): the baseline comes from
  the 0.1.26 parent chain (Java 8 line). Consequences visible in code: no `record` in core
  (`RawValue` is a class), `javax.servlet` in redis-spring.
- Reactor: 8 unconditional modules; `microsphere-redis-generator` only under the `java9+` profile
  (`<jdk>[9,)</jdk>`).
- 1-line diff vs 0.2.x parent pom — the managed stack version (`0.1.26` vs `0.2.26`).

## Structure

Same two-role split as 0.2.x: repository-root `pom.xml` aggregates; `microsphere-redis-parent` is the
inheritance parent; `microsphere-redis-dependencies` is the consumable BOM.
Full write-up: [../current/microsphere-redis-parent.md](../current/microsphere-redis-parent.md).

## Release Line

Work merges into `release-1.x`; published latest `0.1.14`, tip `0.1.15-SNAPSHOT`. README of the branch
documents the compatibility split: main ↔ Spring Data Redis 3.0.0–3.5.x (`0.2.14`),
1.x ↔ Spring Data Redis 2.1.0–2.7.x (`0.1.14`).

## Links

- User guide: [../../user-guide/1.x/microsphere-redis-parent.md](../../user-guide/1.x/microsphere-redis-parent.md)
- Chinese: [../zh/microsphere-redis-parent.md](zh/microsphere-redis-parent.md)
