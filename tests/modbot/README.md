# EnchADD modbot test suite

Mineflayer-based dual-bot harness for verifying EnchADD enchantment
behavior on production-style Minecraft servers.

## Setup

```bash
cd tools/modbot  # or wherever this directory lives
npm install mineflayer
```

Requires a running Minecraft server with RCON enabled
(`enable-rcon=true`, `rcon.password=enchadd`) and offline mode
(`online-mode=false`).

## Usage

All scripts accept positional args: `<server-port> [rcon-port]`.
Set `MC_VERSION` env var to override the protocol version (defaults
to `1.21.1`).

```bash
MC_VERSION=1.21.2 node bb-test.js 25569 25579
```

## Script index

| Script | Tests | Loaders verified |
| --- | --- | --- |
| `bb-test.js` | Bulwark resistance + Breakguard penetration | fabric/forge/neoforge 1.21.1, forge/neoforge 1.21.3, 1.21.6 |
| `disable-test.js` / `disable-nf.js` / `disable-nf2.js` | Shield disable bypass (axe strike) | forge 1.21.1, forge 1.21.6, neoforge 1.21.8 |
| `fist-test.js` | Low-damage full block boundary | forge 1.21.6, neoforge 1.21.8 |
| `immolate-single.js` / `immolate-multi.js` / `immolate-auth.js` | Immolate fire ticks via RCON Fire NBT | fabric 1.21.1 |
| `sunder-auth.js` / `sunder-single.js` / `sunder-multi.js` | Sunder armor-scaled bonus | fabric 1.21.1 |
| `check-*.js` | Diagnostic probes (inventory, effects, position) | any |
| `pre-platform.js` | RCON platform setup (stone + barrier) | any |
| `rcon.js` | Minimal RCON client library | n/a |
| `f126-final.js` / `forge1216-final2.js` | Enchant registry verification via `/enchant` | fabric 1.21.6, forge 1.21.6 |

## Key learnings

- mineflayer `activateItem()` needs `true` for offhand shield blocking
- `Player.BedSleepingProblem` is an enum (1.21.1+), not constructible
- Client-side `entity.fire` metadata is unreliable; use RCON
  `data get entity <name> Fire` for authoritative fire state
- Forge 53+ routes mod STDOUT to DEBUG level; check the log file
  for `[EnchADD]` lines rather than the console
- ItemCooldowns key type changed: `Item` (1.21.1, 1.21.11) vs
  `ItemStack` (1.21.2-1.21.10)
- `processDurabilityChange` handler needs a second `int` param on
  1.21.2-1.21.10 but not on 1.21.1 or 1.21.11
