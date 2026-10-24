# Minecraft 1.21.1 review fixes

- Fabric Breakguard now changes absorbed shield damage and the remaining incoming damage inside the vanilla shield branch, rather than reducing the initial attack amount. Injection slots were checked against the mapped 1.21.1 LivingEntity.hurt bytecode.
- Fabric Bulwark now runs inside the positive shield-block branch, rather than the reusable isDamageSourceBlocked predicate. Absorption is checked after accounting for Breakguard.
- Armor item expansion now combines the four armor slot allowlists. Unknown or empty named tags fail generation instead of silently falling back to all durable items.
- All three 1.21.1 adapter validators inspect their compiled common-1.21.1 sources and use the full required adapter lists. Player-head profile assertions accept the 1.21.1 constructor API.
- The Firebreak attribute uses minecraft:generic.burning_time on 1.21.1. The earlier run failed registry loading with minecraft:burning_time.

## Evidence and remaining checks

Three 1.21.1 builds, including resource and adapter validation, passed after the fixes. See validation-logs/compat-review-fixes-20261024.txt.

Fabric development run reached Done and loaded enchantment resources without registry or Mixin errors. See validation-logs/compat-review-fabric-startup-20261024.log. This is not a packaged-JAR isolation or real shield combat test. Breakguard damage, Bulwark cooldown and canceled/zero damage cases still require gameplay verification.

## Packaged Fabric shield verification

Final JAR SHA-256: `86E7A25AD6BDA730C6EAC4B4C0E828A90650944F632367045403556A21227C92`.

A production Fabric launcher in a separate ASCII directory loaded only EnchADD and Fabric API from mods/. No development class directories were supplied. The protocol player used an offhand shield; RCON /damage applied a 10-point mob_attack attributed to an equipped zombie. Breakguard 0/I/II/III left health at 20/19.25/18.5/17.75. Bulwark III: zero damage produced no effect, positive shield block produced Resistance I for 120 ticks, clearing the effect and retrying inside cooldown produced no effect, retrying after cooldown restored it.

The first packaged test exposed a zeroed argument read in the Breakguard injection; the final implementation captures vanilla's saved incoming-damage local. Results are in validation-logs/shield-packaged-results-20261024.json; probe source and final build log are adjacent. The protocol client reported an inventory SlotComponent PartialReadError, so observations use authoritative server NBT and commands. These tests do not establish natural player melee attacks or cancellation interoperability. The isolated server was stopped after testing. Startup emitted a flat-world generator settings warning; no enchantment registry or Mixin injection failure occurred.
