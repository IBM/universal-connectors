# Architectural Proposal: Universal Guardium Filter Plugin

> **This is a suggestion to the project maintainers.**
> The reference implementation here is meant to illustrate the idea concretely,
> not to be merged as-is. Feedback and alternative approaches are very welcome.

---

## The Problem

Every filter plugin in this repository follows the same structure.
Opening any two plugins side-by-side reveals that they are nearly identical — the only
meaningful difference is the 50–150 lines of parsing logic specific to each datasource.

Everything else is copy-pasted boilerplate:

```
@LogstashPlugin annotation          ┐
implements Filter                   │
static Log4j init block             │  ~200 lines repeated verbatim
filter() event loop + try/catch     │  in every single plugin
GSON serialization                  │
correctIPs() utility                │
logEvent() utility                  │
configSchema() / getId()            ┘
```

This creates real maintenance costs:
- A security fix or utility improvement must be applied to **54 files**
- Adding a new datasource means scaffolding a full Logstash plugin (~500 lines, 8+ files, a new gem)
- 54 separate gem artifacts to build, test, version, and ship

---

## The Suggestion

> **Replace all 54 individual filter plugins with a single generic plugin,
> where each datasource is just a thin parser class (or ideally just a config file).**

The Logstash plugin layer should exist exactly once. The only thing that varies between
datasources — the parsing logic — should be expressed in the simplest possible form.

---

## Proposed Architecture

### Current state (54 plugins)

```
┌───────────────────────────────────────────────────────────────┐
│  logstash-filter-mysql-guardium/                              │
│  ├── build.gradle          (190 lines, ~identical across all) │
│  ├── MySqlFilterGuardium.java                                 │
│  │   ├── @LogstashPlugin, implements Filter    ┐ boilerplate  │
│  │   ├── Log4j init, GSON, correctIPs()        │ ~200 lines   │
│  │   ├── filter() loop, error tagging          ┘              │
│  │   └── parseRecord()  ← the only unique part               │
│  └── filter.conf:  mysql_filter_guardium {}                   │
│                                                               │
│  logstash-filter-mongodb-guardium/  (same structure)          │
│  logstash-filter-snowflake-guardium/ (same structure)         │
│  logstash-filter-postgres-guardium/ (same structure)          │
│  ... × 54                                                     │
└───────────────────────────────────────────────────────────────┘

54 gems  ·  54 build files  ·  54 copies of the same boilerplate
```

### Proposed state (1 plugin + thin parsers)

```
┌───────────────────────────────────────────────────────────────┐
│  logstash-filter-guardium-universal/   (ONE gem)              │
│  │                                                            │
│  ├── GuardiumUniversalFilter.java  ← all Logstash boilerplate │
│  │   └── delegates to ──────────────────────────────────┐    │
│  │                                                       │    │
│  ├── IGuardiumParser (interface)                         │    │
│  │   └── parseRecord(Event) → Record                     │    │
│  │                                                       │    │
│  ├── AbstractGuardiumParser                              │    │
│  │   └── correctIPs(), shared utilities                  │    │
│  │                                                       │    │
│  ├── ParserRegistry  ←────────────────────────────────── ┘    │
│  │   ├── "mysql"     → MySqlParser      (~150 lines)          │
│  │   ├── "mongodb"   → MongoDbParser    (~ 60 lines)          │
│  │   ├── "snowflake" → SnowflakeParser  (~ 40 lines)          │
│  │   └── ...  (one line per datasource)                       │
│  │                                                            │
│  └── filter.conf:                                             │
│      guardium_universal_filter { datasource => "mysql" }      │
└───────────────────────────────────────────────────────────────┘

1 gem  ·  1 build file  ·  boilerplate written once
```

---

## What Changes for Each Datasource

### `filter.conf` — minimal change

```diff
- mysql_filter_guardium {}
+ guardium_universal_filter { datasource => "mysql" }
```

### Adding a new datasource — before vs. after

| | Before | After |
|---|---|---|
| Files to create | 8+ (plugin class, build.gradle, VERSION, gemspec, ...) | 1 (parser class) |
| Lines of new code | ~500 | ~100 |
| New gem required | Yes | No |
| Boilerplate to copy | ~200 lines | 0 lines |

---

## Reference Implementation

This PR includes a working reference implementation to make the idea concrete:

```
filter-plugin/logstash-filter-guardium-universal/
├── GuardiumUniversalFilter.java      ← the single Logstash plugin
├── parser/
│   ├── IGuardiumParser.java          ← interface: parseRecord(Event) → Record
│   ├── AbstractGuardiumParser.java   ← shared utilities
│   └── ParserRegistry.java           ← datasource name → parser instance
├── datasources/
│   ├── mysql/MySqlParser.java        ← MySQL fully migrated (~150 lines)
│   ├── mongodb/MongoDbParser.java    ← MongoDB thin connector
│   └── snowflake/SnowflakeParser.java← Snowflake thin connector
└── [MySQL|MongoDB|Snowflake]*Package/filter.conf
```

**MySQL is fully migrated** as a concrete example — its parsing logic is identical to the
original, just extracted into a plain Java class with no Logstash dependency.
MongoDB and Snowflake are included as thin connectors to show how complex, multi-class
parser hierarchies integrate cleanly.

---

## Migration Strategy

The migration can be done incrementally with zero disruption:

```
Phase 1  Framework + 3 reference parsers (this PR)
Phase 2  Migrate remaining 51 parsers one by one (mechanical extraction)
Phase 3  Move parser class hierarchies fully into the new plugin
Phase 4  Deprecate individual filter plugin directories
```

Existing pipelines are unaffected until their `filter.conf` is updated.
Both the old and new plugin can coexist during migration.

---

## Questions for the Team

- Is this direction aligned with the project's goals?
- Should `IGuardiumParser` live in the `common` module instead, to allow
  parser JARs to be developed and deployed independently?
- Should parsers eventually be driven by config files (YAML field mappings)
  for simple datasources, with Java only needed for complex ones?

---

> Raised by [@haimofergmail](https://github.com/haimofergmail) — open to all feedback.
