# EnchADD 2.0.5 Security Audit

Audit date: 2026-08-25

This release reviewed 100 security controls across 414 production Java sources. A
control is not counted as a vulnerability unless there is a concrete source,
trigger, and impact. Four confirmed defects were fixed; the remaining controls
were either not present in the plugin or require deployment-specific validation.

## Confirmed and fixed

| ID | Risk | Fix |
| --- | --- | --- |
| SEC-205-001 | Maven coordinate metadata could remain in a distributable jar and trigger the protection check. | The shaded artifact now excludes `META-INF/maven/**`; the protected-release verifier still rejects it. |
| SEC-205-002 | A forged PDC timestamp far in the future could create a permanent cooldown or effect window. | Future cooldowns and windows beyond 24 hours are discarded; all duration arithmetic now saturates instead of overflowing. |
| SEC-205-003 | A malformed stored location could contain an oversized payload, non-finite coordinates, or coordinates beyond the Minecraft world limit. | The decoder limits payload shape and length, rejects non-finite values, and enforces the X/Z world boundary. |
| SEC-205-004 | `/enchadd ci` exposed heap, TPS, error-rate, and safety diagnostics to any sender with the default `enchadd.list` permission. | The CI diagnostic path now requires `enchadd.admin`, matching the other administrative subcommands. |

## 100 reviewed controls

| Controls | Area | Result |
| --- | --- | --- |
| ART-01..10 | Jar layout, manifest, descriptors, source leakage, Maven metadata, hash binding, artifact origin | One defect fixed (SEC-205-001). |
| CMD-01..10 | Brigadier routing, subcommand permissions, console behavior, command blocks, argument bounds, completion | One permission bypass fixed (SEC-205-004); remaining administrative paths are server-operator paths. |
| CFG-01..10 | YAML loading, defaults, invalid key handling, numeric settings, reload and configuration persistence | No arbitrary object deserialization or user-selected filesystem path found. |
| FS-01..10 | Export, statistics, language extraction, report paths, path traversal, writes and failures | Output paths are fixed below the plugin data folder. |
| PDC-01..10 | Item/entity/player PDC type checks, levels, UUIDs, locations, cooldowns and expiry windows | Two defects fixed (SEC-205-002 and SEC-205-003). |
| EVT-01..10 | Listener dispatch, reflective registration, cancellation, cleanup, exceptions and event budgets | Reflection is limited to plugin-owned listener methods; no externally supplied class or method name is invoked. |
| DOS-01..10 | Particle queue, scheduler work, export serialization, scans, execution budgets and rate limits | Existing queues and command bounds were reviewed; production TPS/load testing remains deployment-specific. |
| DATA-01..10 | Item metadata, enchantment levels, legacy conversion, NBT/PDC validation and inventory traversal | No Java serialization, XML decoder, or unsafe YAML object loading found. |
| NET-01..10 | HTTP, sockets, URL handling, SQL, remote code loading, PlaceholderAPI boundary and packets | No plugin-owned HTTP, socket, SQL, URL, or dynamic class loading path found. |
| DEP-01..10 | Packaged dependency tree, build plugins, Paper API, PlaceholderAPI, Java baseline and release reproducibility | Plugin runtime dependency tree is empty. Server-provided Paper and PlaceholderAPI must be scanned by the server operator. |

## Verification required for release

1. Run the plugin module unit tests with JDK 21.
2. Build with `-Pprotected-release` and verify the protected jar contains no `META-INF/maven/**` entries.
3. Run the Paper smoke suite and load the protected jar on the target Paper version.
4. Scan the actual server distribution, including Paper, PlaceholderAPI and every installed plugin, because they are outside this artifact.

## Scope limits

This is a source and artifact audit, not a claim that 100 exploitable
vulnerabilities exist. Network permissions, hostile client behavior, installed
plugins, server configuration, JVM flags, and operating-system file permissions
require a production-like server test to validate.
