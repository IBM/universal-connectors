# Universal Guardium Filter — Architecture

## Problem

The repository contains **54 nearly-identical Logstash filter plugins**. Every plugin
follows the same structure: register with `@LogstashPlugin`, implement `Filter`,
iterate over events, delegate to a parser, serialize the result as JSON, and tag errors.
This boilerplate is copy-pasted across every single plugin.

The result is:
- A security fix or common-utility improvement must be applied to 54 files
- Onboarding a new datasource requires scaffolding a full Logstash plugin (build, gem, etc.)
- 54 separate gem artifacts to build, test, and ship

---

## Old Architecture (54 plugins)

```
┌─────────────────────────────────────────────────────────────────────┐
│ filter-plugin/                                                      │
│                                                                     │
│  logstash-filter-mysql-guardium/                                    │
│  ├── build.gradle          ◄── 190 lines, mostly identical          │
│  ├── MySqlFilterGuardium.java                                       │
│  │   ├── @LogstashPlugin annotation          ┐                      │
│  │   ├── implements Filter                   │                      │
│  │   ├── static Log4j init block             │ ~200 lines of        │
│  │   ├── filter() loop + try/catch           │ boilerplate          │
│  │   ├── GSON serialization                  │ repeated in          │
│  │   ├── correctIPs()                        │ every plugin         │
│  │   ├── logEvent()                          │                      │
│  │   ├── configSchema() / getId()            ┘                      │
│  │   └── parseRecord() logic  ◄── only unique part                  │
│  └── MySQLOverSyslogPackage/MySQL/filter.conf                       │
│      └─  mysql_filter_guardium {}                                   │
│                                                                     │
│  logstash-filter-mongodb-guardium/   (same structure)               │
│  logstash-filter-snowflake-guardium/ (same structure)               │
│  logstash-filter-postgres-guardium/  (same structure)               │
│  ... × 54                                                           │
└─────────────────────────────────────────────────────────────────────┘

Each datasource = 1 Logstash plugin gem
Adding a datasource = copy-paste a full plugin scaffold
```

---

## New Architecture (1 plugin + thin parsers)

```
┌─────────────────────────────────────────────────────────────────────┐
│ filter-plugin/                                                      │
│                                                                     │
│  logstash-filter-guardium-universal/   ◄── ONE plugin gem           │
│  ├── build.gradle                                                   │
│  │                                                                  │
│  ├── GuardiumUniversalFilter.java      ◄── all Logstash boilerplate │
│  │   ├── @LogstashPlugin(name="guardium_universal_filter")          │
│  │   ├── implements Filter                                          │
│  │   ├── static Log4j init                                          │
│  │   ├── filter() loop + try/catch + GSON serialization             │
│  │   └── delegates to ──────────────────────────────────────┐       │
│  │                                                          │       │
│  ├── parser/                                                │       │
│  │   ├── IGuardiumParser (interface)  ◄── parseRecord(Event)│       │
│  │   ├── AbstractGuardiumParser       ◄── correctIPs, utils │       │
│  │   └── ParserRegistry               ◄── "mysql" → parser ◄┘       │
│  │                                                                  │
│  └── datasources/                                                   │
│      ├── mysql/MySqlParser.java        ◄── ~150 lines, pure logic   │
│      ├── mongodb/MongoDbParser.java    ◄──  ~60 lines, pure logic   │
│      ├── snowflake/SnowflakeParser.java◄──  ~40 lines, pure logic   │
│      └── ... (one class per datasource)                             │
│                                                                     │
│  MySQLOverSyslogPackage/MySQL/filter.conf                           │
│  └─  guardium_universal_filter { datasource => "mysql" }           │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘

All datasources = 1 Logstash plugin gem
Adding a datasource = write 1 parser class + 1 line in ParserRegistry
```

---

## Component Responsibilities

| Component | Responsibility |
|---|---|
| `GuardiumUniversalFilter` | Logstash lifecycle, event loop, GSON serialization, error tagging, Log4j init |
| `IGuardiumParser` | Contract: `parseRecord(Event) → Record \| null` |
| `AbstractGuardiumParser` | Shared utilities: `correctIPs()`, IP validation |
| `ParserRegistry` | Case-insensitive map of `datasource-name → IGuardiumParser` |
| `MySqlParser` (etc.) | Datasource-specific logic only — no Logstash dependency |

---

## Adding a New Datasource

**Before** — required:
1. Copy an entire plugin directory (~8 files)
2. Modify `build.gradle`, `VERSION`, `@LogstashPlugin` annotation, class name
3. Re-implement the same `filter()`, `configSchema()`, `getId()`, `correctIPs()`, etc.
4. Build and ship a new `.gem` artifact

**After** — requires:
1. Write one class implementing `IGuardiumParser` (parsing logic only)
2. Add one line to `ParserRegistry`: `register("newdb", new NewDbParser());`
3. Write a `filter.conf` with `guardium_universal_filter { datasource => "newdb" }`

No new plugin registration. No new gem. No new build file.

---

## Migration Path

The migration is incremental and non-breaking:

```
Phase 1 (done):  Framework + MySQL fully migrated
Phase 2:         Migrate remaining 51 parsers (mechanical extraction)
Phase 3:         Move MongoDB/Snowflake inner parser hierarchies into this plugin
Phase 4:         Deprecate individual filter plugin directories
```

Old plugins continue to work during migration — pipelines can be switched to
`guardium_universal_filter` one datasource at a time by updating `filter.conf` and
`manifest.json`.

---

## File Size Comparison

| | Old (per plugin) | New (per datasource) |
|---|---|---|
| Logstash plugin class | ~300 lines | 0 lines |
| Parser logic | mixed in | ~50–200 lines (pure Java) |
| `build.gradle` | ~190 lines | 0 lines (shared) |
| Gem artifact | 1 per datasource | 0 (shared gem) |
| **Total new code for a datasource** | **~500 lines** | **~100 lines** |
