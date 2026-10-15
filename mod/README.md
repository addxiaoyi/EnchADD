# EnchADD Mod

The EnchADD mod compatibility release line covers Minecraft `1.21.11-26.3`.
The Paper plugin remains a separate project; the mod projects do not depend on Paper API.

## Release model

All six artifacts share the mod version `2.1.1`; loader and Minecraft compatibility are selected by the artifact name, not by an `unspecified` metadata version.

`1.21.11-26.3` is one compatibility range, not one JAR that can run across every loader and mapping.
Each EnchADD release provides six variants with the same EnchADD version:

- Fabric 1.21.11
- Fabric 26.3
- Forge 1.21.11
- Forge 26.3
- NeoForge 1.21.11
- NeoForge 26.3

This keeps each loader on its official mappings and lifecycle while allowing users to select the artifact that matches their server or client.

## Layout

- `common/`: loader-independent rules and version checks.
- `fabric-*`: Fabric entrypoints and metadata.
- `forge-*`: Forge entrypoints and metadata.
- `neoforge-*`: NeoForge entrypoints and metadata.

## Compatibility rules

The shared core accepts `1.21.11 <= version <= 26.3` and clamps invalid weights, damage bonuses, damage reduction, and effect durations.

The build generates 111 registry-compatible enchantment definitions from one catalog. The current data-driven behavior coverage includes:

- spear and defense effects: `lancer`, `reach`, `skewer`, `counterthrust`, `arrowguard`, `blastguard`, `fireguard`, `featherstep`, `frostguard`;
- combat/status effects: `debilitate`, `frostbrand`, `immolate`, `firebreak`, `evasion`;
- mining/projectile/armor effects: `insight`, `volley`, `steady_aim`, `sunder`;
- experience effects: `greed`, `misfortune`;
- durability/fishing effects: `fragility`, `shatter`, `trawler`.
- loader event behavior: `nourish` now applies absorption after a completed food-use event on Fabric, Forge, and NeoForge. The six loader builds compile this event adapter successfully.
- loader event behavior: `delvesense` now handles compass activation, Y-level gating, night vision, nearby monster glowing, and the 600-tick item cooldown on all six loader variants.
- loader event behavior: `freshcatch` restores food and saturation after an edible fishing drop on all six variants. Forge/NeoForge use `ItemFishedEvent`; Fabric uses a version-specific `FishingHook.retrieve` mixin that intercepts the real loot collection.
- loader event behavior: `trawler` increases one caught stack with shared probability, level, cap, and cooldown rules. Forge/NeoForge use `ItemFishedEvent`; Fabric modifies the same real loot collection in the FishingHook mixin.
- loader event behavior: `last_stand` intercepts lethal damage, applies the bounded rescue-health rule, and cancels death with a shared cooldown on all six variants.
- loader event behavior: `fortitude` detects non-lethal low-health damage and applies bounded regeneration/resistance buffs with a shared cooldown on all six variants.
- loader event behavior: `bulwark` detects active off-hand shield blocking and applies bounded resistance with a shared cooldown on all six variants.
- loader event behavior: `ward` cancels blocked incoming damage, applies bounded shield durability loss, and uses a shared 80-tick cooldown on all six variants.
- loader event behavior: `breakguard` adds bounded melee damage through a successful shield block on all six variants. Fabric adjusts vanilla shield absorption; Forge and NeoForge adjust the incoming damage event.
- loader event behavior: `brace` reduces knockback while a shield with Brace is actively blocking, with a 45% total reduction cap on all six variants.
- loader event behavior: `farshot` records the real arrow launch position and applies distance-scaled damage between 12 and 40 blocks, with a 75% bonus cap and shared 100-tick shooter cooldown on all six variants.
- loader event behavior: `barrier` applies a bounded radial knockback pulse after a confirmed shield block, with level-scaled chance, radius, target count, and a shared 200-tick cooldown. Fabric and NeoForge use blocked-damage signals; Forge uses its final-damage shield-state compatibility hook.
- loader event behavior: `parry` arms a 60-tick retaliation window after a shield block and adds bounded melee counterattack damage once, with an 80-tick cooldown on all six variants.
- loader event behavior: `holdfast` temporarily suppresses the vanilla axe shield-disable cooldown when its bounded chance succeeds, then restores the original shield component after the current damage call. The implementation is shared across all six variants.
- loader event behavior: `airbag` reduces fall and fly-into-wall damage using the summed armor levels, capped at 90%.
- loader event behavior: `steadfast` probabilistically reduces player knockback from the boots enchantment, with per-level scaling, a 60% cap, and an 80-tick cooldown.
- loader event behavior: `fleetfoot` applies a bounded Speed effect when sprinting starts, and `afterglide` applies a bounded Slow Falling effect when gliding ends in mid-air; both use shared player-state edge detection and cooldowns on all six variants.
- loader event behavior: `evasion` cancels a real arrow hit using the boots enchantment's level-scaled chance and 100-tick cooldown, matching the Paper listener instead of relying on unconditional data-driven immunity.
- loader event behavior: `stonewake` grants bounded Haste after breaking an ore or ancient debris block, preserving stronger/longer existing Haste effects on all six variants.
- loader event behavior: `irrigation` hydrates nearby farmland from hoe right-clicks, supports clicking the block above farmland, and bypasses while sneaking on all six variants.
- loader event behavior: `homeward` grants speed after sprinting combat damage and arms a bounded fall-protection window consumed by the next fall, on all six variants.
- loader event behavior: `tideshell` activates water-breathing and Dolphin's Grace from a nautilus shell in water, while `tide_runner` grants movement-gated Dolphin's Grace while swimming, with shared cooldowns on all six variants.
- loader event behavior: `hunters_mark` and `obscure` record projectile launch enchantment levels and apply bounded Glowing or Blindness on hit with per-shooter cooldowns on all six variants.
- loader event behavior: `ricochet` performs one bounded, probability-controlled secondary arrow toward the nearest other living target within six blocks, with speed scaling, cooldown, and bounce-loop protection on all six variants.
- loader event behavior: `homecoming` detects a successful enchanted Totem of Undying rescue and redirects the player to the normal respawn transition on all six variants.
- loader event behavior: `volley` replaces the generic data-driven projectile count/spread with one-time custom arrow duplication, preserving arrow type, shooter, base damage, spread, and pickup protection on all six variants.
- loader event behavior: `cinder` adds bounded melee damage against burning targets, and `updraft` grants short Slow Falling after an airborne melee hit on all six variants.
- loader event behavior: `tremor` applies bounded area Slowness after a successful main-hand melee hit, with level-scaled chance, radius, target cap, and cooldown on all six variants.
- loader event behavior: `smelting` converts fresh block drops through the server's smelting recipes; NeoForge uses its exact drops event, while Fabric/Forge use a same-tick, block-local fresh-drop scan.
- loader event behavior: `replanting` restores mature crops from matching inventory seeds after block breaking on all six variants; `furrow` also consumes a matching seed from its fresh drops when the inventory has none. Supported age-based crops include wheat, carrots, potatoes, beetroots, torchflower, pitcher plant, nether wart, sweet berry bush, and cocoa.
- loader event behavior: `telepathy` clears pickup delay, assigns the breaker as item owner/target, and moves fresh block drops to the breaker on all six variants.
- loader event behavior: `arborist` and `trailblazer` extend vanilla axe/shovel right-click transformations, while `furrow` harvests mature neighboring age-based crops and optionally replants them on all six variants.
- loader event behavior: `rashness` detects under-charged direct player attacks and applies bounded self-damage with recursion protection on all six variants.
- loader event behavior: `smelting` now has a drop-aware NeoForge implementation that resolves the server's smelting recipe and preserves the original stack count; Forge/Fabric drop hooks remain to be added before claiming six-variant parity.

These effects use Minecraft's native Enchantment Effects system with bounded level scaling. `Nourish` uses each loader's completed item-use event and the shared `NourishRules` duration cap. The remaining Paper-only listener behaviors are intentionally not claimed as one-to-one parity until their loader-specific event implementations are ported and tested.
