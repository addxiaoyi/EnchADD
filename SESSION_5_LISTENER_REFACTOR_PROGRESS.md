# Session 5 Listener Refactor Progress

## Date
- 2026-05-20

## Goal
- Continue the 2.0.1 architecture cleanup from config and command decomposition into listener decomposition.
- Reduce large listener files without changing enchant behavior.
- Preserve MockBukkit contract coverage before moving on to the next hotspot.

## Completed in this pass

### 1. Furrow listener decomposition
- Split `FurrowListener` into a thin event entrypoint plus `FurrowHarvestSupport`.
- Moved the following responsibilities out of the listener:
  - crop target collection
  - harvestability checks
  - replant decision flow
  - seed consumption from inventory or drops
  - drop snapshot and drop emission
- Introduced `ListenerDispatchGuard` to isolate synthetic event re-entry protection.

### 2. Arborist listener decomposition
- Split `ArboristListener` into a thin event entrypoint plus `ArboristStripSupport`.
- Moved the following responsibilities out of the listener:
  - stripped variant lookup table
  - upward target collection
  - stripped block data application with axis preservation
- Reused `ListenerDispatchGuard` for synthetic interact dispatch protection.

### 3. Curse conflict decomposition
- Split `CurseConflictListener` into a thin event entrypoint plus `CurseConflictSupport`.
- Moved the following responsibilities out of the listener:
  - curse limit enforcement during enchant rolls
  - conflict filtering against existing enchants
  - deterministic mutually incompatible pair resolution
  - anvil result rejection checks

### 4. Vampirism runtime cleanup
- Rewrote `VampirismListener` out of decompiled style into standard project style.
- Added `VampirismRuntimeSupport` to isolate:
  - equipment/enchant presence checks
  - active candidate reconciliation
- Preserved the existing performance contract:
  - `activeCandidates` hot set
  - `for (UUID id : activeCandidates)` scheduler loop
  - `scheduleActiveTick`
  - `scheduleReconcile`
  - `player.getScheduler().execute`
  - global region fixed-rate refresh task

### 5. Verified behavior
- Passed targeted MockBukkit tests:
  - `FurrowHarvestBehaviorTest`
  - `ArboristStripBehaviorTest`
- Passed targeted listener/contract tests:
  - `CurseConflictListenerBehaviorTest`
  - `PerformanceOptimizationContractTest`
  - `CriticalEnchantEffectsContractTest`
- Passed full MockBukkit suite:
  - `180` tests green

### 6. Follow-up listener cleanup
- Split `IrrigationListener` into listener + `IrrigationHydrationSupport`
- Split `TrailblazerListener` into listener + `TrailblazerPathSupport`
- Split `LegacyEnchantSanitizerListener` into listener + `LegacyEnchantSanitizerSupport`
- Split `VolleyListener` into listener + `VolleySpawnSupport`
- Split `GreedListener` into listener + `GreedEffectSupport`
- Split `StarwishListener` into listener + `StarwishEffectSupport`
- Split `RicochetListener` into listener + `RicochetEffectSupport`
- Split `DelvesenseListener` into listener + `DelvesenseEffectSupport`
- Split `TideshellListener` into listener + `TideshellEffectSupport`
- Split `VampirismListener` scheduler wiring into `VampirismSchedulerSupport`
- Split `GluttonyListener` into listener + `GluttonySanitizerSupport`
- Split `WaysongListener` into listener + `WaysongEffectSupport`
- Split `BeheadingListener` into listener + `BeheadingDropSupport`
- Split `FarshotListener` into listener + `FarshotDamageSupport`
- Split `MortalWoundListener` into listener + `MortalWoundEffectSupport`
- Split `ThirstListener` into listener + `ThirstEffectSupport`
- Added a dedicated `LegacySanitizerListenerContractTest` to keep the sanitizer listener thin over time
- Added dedicated `GreedBehaviorTest` coverage for XP gain and vulnerability window state
- Added dedicated `BeheadingBehaviorTest` coverage for head-drop selection and duplicate-drop prevention
- Added dedicated `FarshotBehaviorTest` coverage for launch snapshot and distance scaling
- Added dedicated `ThirstBehaviorTest` coverage for combat window, regen penalty, and food loss

## Current structural result

### Reduced listener sizes
- `FurrowListener.java`: 88 lines
- `ArboristListener.java`: 115 lines
- `CurseConflictListener.java`: 43 lines
- `VampirismListener.java`: 159 lines
- `IrrigationListener.java`: 107 lines
- `TrailblazerListener.java`: 110 lines
- `LegacyEnchantSanitizerListener.java`: 82 lines
- `VolleyListener.java`: 114 lines
- `GreedListener.java`: 69 lines
- `StarwishListener.java`: 80 lines
- `RicochetListener.java`: 76 lines
- `DelvesenseListener.java`: 82 lines
- `TideshellListener.java`: 78 lines
- `VampirismListener.java`: 153 lines
- `GluttonyListener.java`: 95 lines
- `WaysongListener.java`: 78 lines
- `BeheadingListener.java`: 50 lines
- `FarshotListener.java`: 81 lines
- `MortalWoundListener.java`: 77 lines
- `ThirstListener.java`: 71 lines

### New helper classes
- `plugin/src/main/java/net/enchadd/listeners/ListenerDispatchGuard.java`
- `plugin/src/main/java/net/enchadd/listeners/FurrowHarvestSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/ArboristStripSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/CurseConflictSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/VampirismRuntimeSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/IrrigationHydrationSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/TrailblazerPathSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/LegacyEnchantSanitizerSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/VolleySpawnSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/GreedEffectSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/StarwishEffectSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/RicochetEffectSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/DelvesenseEffectSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/TideshellEffectSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/VampirismSchedulerSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/GluttonySanitizerSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/WaysongEffectSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/BeheadingDropSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/FarshotDamageSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/MortalWoundEffectSupport.java`
- `plugin/src/main/java/net/enchadd/listeners/ThirstEffectSupport.java`

## Why this split matters
- Listener files now focus on event gating, permissions, and orchestration.
- Stateful synthetic event recursion guards are centralized.
- Crop and log transformation rules are easier to test and evolve independently.
- The next cleanup steps can follow the same pattern across other complex listeners.

## Next recommended hotspots
1. `ArboristListener`
   - if desired, continue shrinking by moving resolve/config wiring to shared base helpers
2. `TrailblazerListener`
   - same optional follow-up as Arborist
3. `IrrigationListener`
   - same optional follow-up as Arborist
4. `EnchantListenerRegistrar`
   - if desired, continue toward a true table-driven registration model
5. `Listener support packaging`
   - if desired, move the growing `*Support` classes into a dedicated `listeners/support` package

## Validation commands
```powershell
mvn --% -pl tests/mockbukkit -am -Dsurefire.failIfNoSpecifiedTests=false -Dtest=FurrowHarvestBehaviorTest,ArboristStripBehaviorTest test
```

## Release status
- Project version remains `2.0.1`
- This pass was refactor-only and intentionally preserved external behavior

## 2026-05-22 follow-up

### 7. Listener support package consolidation
- Moved the remaining listener support helpers into `net.enchadd.listeners.support`.
- Updated all listener imports to reference the new support package.
- Promoted the moved support classes and their exported methods to `public` so the thin listeners can compile cleanly across package boundaries.
- Promoted `RicochetEffectSupport.PlayerLike` to `public` because `RicochetListener` references it directly.

### 8. Validation
- Full MockBukkit regression passed again:
  - `190` tests green
  - `0` failures
  - `0` errors

### 9. Current remaining hotspot
- `EnchantListenerRegistrar` is still the largest listener-side source file.
- It is already grouped by listener family, so any further reduction should be done carefully because contract tests assert specific registration wiring.

### 10. Telepathy listener extraction
- Split `TelepathyListener` into a thin event entrypoint plus `TelepathyDropSupport`.
- Moved the following responsibilities out of the listener:
  - dropped item filtering and pickup delay reset
  - optional owner binding for exclusive pickup mode
  - validated item teleport dispatch back to the player location
- Added `TelepathyBehaviorTest` to cover collection/owner assignment and teleport behavior without depending on `ItemMock.setOwner`.

### 11. Validation
- Full MockBukkit regression passed again:
  - `192` tests green
  - `0` failures
  - `0` errors

### 12. Homeward listener extraction
- Split `HomewardListener` into a thin event entrypoint plus `HomewardEscapeSupport`.
- Moved the following responsibilities out of the listener:
  - sprint-combat trigger gating and leggings enchant level snapshot
  - cooldown and escape-window state write/read in player PDC
  - speed burst application and one-shot fall-damage reduction consumption
- Kept existing behavior contract via `HomewardEscapeBehaviorTest` without changing external triggers.

### 13. Validation
- Full MockBukkit regression passed again:
  - `192` tests green
  - `0` failures
  - `0` errors

### 14. TideRunner listener extraction
- Split `TideRunnerListener` into a thin event entrypoint plus `TideRunnerEffectSupport`.
- Moved the following responsibilities out of the listener:
  - water-contact detection via eye block and upper block liquid checks
  - boots enchant level resolution and runtime gating
  - movement delta threshold checks for meaningful swim motion
  - dolphins-grace duration/amplifier computation per level
  - cooldown read/write through player PDC
- Kept the listener focused on:
  - move-position change guard
  - player swimming-state guard
  - dispatch to support logic
- Added dedicated `TideRunnerBehaviorTest` coverage for:
  - successful underwater swim trigger
  - dry-state non-trigger behavior
  - cooldown blocking for immediate retrigger

### 15. Validation
- Targeted regression passed:
  - `TideRunnerBehaviorTest`
  - `PerformanceOptimizationContractTest`
  - `18` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `195` tests green
  - `0` failures
  - `0` errors

### 16. Stonewake listener extraction
- Split `StonewakeListener` into a thin event entrypoint plus `StonewakeEffectSupport`.
- Moved the following responsibilities out of the listener:
  - ore material classification and lookup
  - tool enchant level resolution
  - haste duration and amplifier calculation
  - existing-haste upgrade guards (avoid downgrading stronger/longer effects)
  - final haste effect application
- Kept listener responsibility to:
  - lifecycle/config resolution
  - high-priority block-break event dispatch to support

### 17. Validation
- Targeted regression passed:
  - `StonewakeHasteBehaviorTest`
  - `PerformanceOptimizationContractTest`
  - `18` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `195` tests green
  - `0` failures
  - `0` errors

### 18. Bind listener extraction
- Split `BindListener` into a thin event entrypoint plus `BindProjectileSupport`.
- Moved the following responsibilities out of the listener:
  - projectile launch snapshot tagging (arrow level persistence)
  - hit-time snapshot recovery and shooter validation
  - cooldown/chance gate and slow-duration computation
  - slowness application and cooldown writeback
- Kept listener responsibility to:
  - event wiring (`ProjectileLaunchEvent`, `EntityDamageByEntityEvent`)
  - runtime config/enchant/key state and support dispatch
- Added dedicated `BindBehaviorTest` coverage for:
  - launch-level snapshot tagging
  - snapshot persistence after shooter weapon swap
  - cooldown blocking immediate second proc

### 19. Freshcatch listener extraction
- Split `FreshcatchListener` into a thin event entrypoint plus `FreshcatchRestoreSupport`.
- Moved the following responsibilities out of the listener:
  - edible-catch gating and caught-item validation
  - rod selection with enchant checks across hands
  - hunger and saturation restoration with vanilla cap clamping
- Kept listener responsibility to:
  - event entrypoint and support dispatch
  - runtime enchant/config resolution

### 20. Validation
- Targeted regression passed:
  - `BindBehaviorTest`
  - `FreshcatchRestoreBehaviorTest`
  - `PerformanceOptimizationContractTest`
  - `21` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `198` tests green
  - `0` failures
  - `0` errors

### 21. Barrier listener extraction
- Split `BarrierListener` into a thin event entrypoint plus `BarrierShieldSupport`.
- Moved the following responsibilities out of the listener:
  - trigger chance capping and activation roll
  - nearby target knockback vector computation and emission
- Kept listener responsibility to:
  - shield-block gate and hand-raised timing checks
  - offhand enchant level resolution
  - cooldown read/write and support dispatch
- Added dedicated `BarrierBehaviorTest` coverage for:
  - successful proc writes cooldown state
  - failed shield gate short-circuits without cooldown writes

### 22. Dispel listener extraction
- Split `DispelListener` into a thin event entrypoint plus `DispelHitContextSupport`.
- Moved the following responsibilities out of the listener:
  - attacker/victim/equipment/PDC hit context resolution
  - first-positive-effect lookup over configured positive list
- Kept listener responsibility to preserve contract anchors:
  - capped chance calculation (`Math.min(0.95, ...)`)
  - positive-effect remove call (`removePotionEffect`)
  - cooldown write (`PerformanceUtils.setCooldown`)
  - event contract signature (`EntityDamageByEntityEvent`)

### 23. Validation
- Targeted regression passed:
  - `BarrierBehaviorTest`
  - `CooldownDispelPurifyBehaviorTest`
  - `CooldownPersistenceAndExpiryTest`
  - `ListenerBehaviorTest`
  - `CriticalEnchantEffectsContractTest`
  - `ShieldBlockingContractTest`
  - `PerformanceOptimizationContractTest`
  - `36` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `200` tests green
  - `0` failures
  - `0` errors

### 24. Tenderstep listener extraction
- Split `TenderstepListener` into a thin event entrypoint plus `TenderstepProtectionSupport`.
- Moved the following responsibilities out of the listener:
  - protected block classification (`FARMLAND` / optional `TURTLE_EGG`)
  - boots enchant presence check with safe equipment access
- Kept listener responsibility to:
  - event gating and player-only dispatch
  - runtime enchant/config resolution
  - final cancel application

### 25. Purify/Dispel shared context extraction
- Introduced `PotionCleanseContextSupport` for shared potion-cleansing context building:
  - `resolveDispelContext` for hit-based cleansing flow
  - `resolvePurifyContext` for consume-based cleansing flow
  - `findFirstEffect` for ordered effect scanning
- Updated `DispelListener` to use the shared context support while preserving contract anchors:
  - `EntityDamageByEntityEvent`
  - `Math.min(0.95, ...)`
  - `positives`
  - `removePotionEffect`
  - `PerformanceUtils.setCooldown`
- Updated `PurifyListener` to use the shared context support while preserving contract anchors:
  - `PlayerItemConsumeEvent`
  - `PerformanceUtils.isOnCooldown`
  - `negatives`
  - `removePotionEffect`
  - `PerformanceUtils.setCooldown`
- Removed superseded `DispelHitContextSupport`.

### 26. Validation
- Targeted regression passed:
  - `TenderstepProtectionBehaviorTest`
  - `CooldownDispelPurifyBehaviorTest`
  - `CooldownPersistenceAndExpiryTest`
  - `ListenerBehaviorTest`
  - `ListenerBehaviorMoreTest`
  - `PurifyCooldownExpiryTest`
  - `CriticalEnchantEffectsContractTest`
  - `PerformanceOptimizationContractTest`
  - `36` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `200` tests green
  - `0` failures
  - `0` errors

### 27. Skim listener extraction
- Split `SkimListener` into a thin event entrypoint plus `SkimImpactSupport`.
- Moved the following responsibilities out of the listener:
  - chestplate enchant level check
  - flat wall-impact damage reduction computation
  - low-damage cancel path and adjusted damage writeback
- Kept listener responsibility to:
  - wall-impact cause gating
  - player/equipment safe resolution
  - support dispatch with resolved runtime enchant/config

### 28. Validation
- Targeted regression passed:
  - `SkimBehaviorTest`
  - `TenderstepProtectionBehaviorTest`
  - `CooldownDispelPurifyBehaviorTest`
  - `CriticalEnchantEffectsContractTest`
  - `PerformanceOptimizationContractTest`
  - `32` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `200` tests green
  - `0` failures
  - `0` errors

### 29. Ward/Pivot listener extraction
- Split `WardListener` into a thin event entrypoint plus `WardShieldSupport`.
- Moved the following responsibilities out of the listener:
  - offhand shield + enchant presence resolution
  - cooldown strategy branching (`HumanEntity` shield cooldown vs non-human PDC fallback)
  - shield durability damage + block sound playback + cancel application
- Kept listener responsibility to preserve contract anchors:
  - shield-block gate (`PerformanceUtils.isSuccessfulShieldBlock(...)`)
  - runtime enchant/config/key presence checks
  - support dispatch ordering
- Split `PivotListener` into a thin event entrypoint plus `PivotMomentumSupport`.
- Moved the following responsibilities out of the listener:
  - shield enchant level + player PDC runtime context resolution
  - cooldown gate evaluation
  - speed effect duration/amplifier construction and apply+cooldown writeback
- Kept listener responsibility to preserve contract anchors:
  - shield-block gate (`PerformanceUtils.isSuccessfulShieldBlock(...)`)
  - runtime enchant/config/key presence checks
  - support dispatch ordering

### 30. Validation
- Full MockBukkit regression passed again:
  - `200` tests green
  - `0` failures
  - `0` errors

### 31. Shield retaliation-chain listener extraction
- Split `BulwarkListener` into a thin event entrypoint plus `BulwarkShieldSupport`.
- Moved the following responsibilities out of the listener:
  - offhand enchant level + player PDC context resolution
  - cooldown gate evaluation
  - resistance effect construction and apply+cooldown writeback
- Split `RiposteListener` into a thin event entrypoint plus `RiposteRetaliationSupport`.
- Moved the following responsibilities out of the listener:
  - defender equipment/offhand level + PDC context resolution
  - cooldown gate evaluation
  - trigger chance roll and weakness effect construction
  - retaliation effect application and cooldown writeback
- Split `BreakguardListener` into a thin event entrypoint plus `BreakguardDamageSupport`.
- Moved the following responsibilities out of the listener:
  - attacker mainhand enchant level resolution
  - per-level bonus damage cap computation
- Split `ParryListener` into a thin event entrypoint plus `ParryRetaliationSupport`.
- Moved the following responsibilities out of the listener:
  - block-time arm context resolution (attacker snapshot + level snapshot + PDC)
  - retaliation window/cooldown state writes
  - counterattack window validation, target matching, bonus damage settlement, and state cleanup
- Preserved shield-contract anchors in listener layer:
  - `PerformanceUtils.isSuccessfulShieldBlock(...)` remains explicit in
    - `BulwarkListener`
    - `RiposteListener`
    - `BreakguardListener`
    - `ParryListener`

### 32. Validation
- Targeted regression passed:
  - `BulwarkBehaviorTest`
  - `RiposteBehaviorTest`
  - `BreakguardConditionBehaviorTest`
  - `ParryRetaliationBehaviorTest`
  - `ShieldBlockingContractTest`
  - `PerformanceOptimizationContractTest`
  - `23` tests green
  - `0` failures
  - `0` errors
- Added dedicated behavior coverage:
  - `BulwarkBehaviorTest` (shield block proc + shield-gate short-circuit)
  - `RiposteBehaviorTest` (successful weakness proc + chance-gate short-circuit)
- Full MockBukkit regression passed again:
  - `202` tests green
  - `0` failures
  - `0` errors

### 33. Defensive utility-chain listener extraction
- Split `BraceListener` into a thin event entrypoint plus `BraceKnockbackSupport`.
- Moved the following responsibilities out of the listener:
  - shield offhand enchant level resolution
  - knockback reduction ratio computation with max-cap enforcement
  - scaled knockback vector application
- Kept listener contract anchors intact:
  - `EntityKnockbackEvent.Cause.SHIELD_BLOCK`
  - `PerformanceUtils.isLikelyShieldFacingBlock(...)`
- Split `HoldfastListener` into a thin event entrypoint plus `HoldfastShieldSupport`.
- Moved the following responsibilities out of the listener:
  - axe-weapon validation for attacker
  - shield + enchant + PDC context resolution
  - cooldown/chance gate and shield-disable cancellation writeback
- Split `AirbagListener` into a thin event entrypoint plus `AirbagImpactSupport`.
- Moved the following responsibilities out of the listener:
  - eligible damage-cause classification (`FALL` / `FLY_INTO_WALL`)
  - armor enchant level aggregation
  - percentage reduction computation
  - visual/audio/stat side-effect dispatch after successful reduction
- Split `WingguardListener` into a thin event entrypoint plus `WingguardRescueSupport`.
- Moved the following responsibilities out of the listener:
  - chest enchant + PDC context resolution
  - lethal-hit determination
  - cooldown/chance gate
  - rescue effects application and cooldown writeback

### 34. Validation
- Targeted regression passed:
  - `BraceBehaviorTest`
  - `HoldfastBehaviorTest`
  - `AirbagBehaviorTest`
  - `WingguardBehaviorTest`
  - `ShieldBlockingContractTest`
  - `PerformanceOptimizationContractTest`
  - `22` tests green
  - `0` failures
  - `0` errors
- Added dedicated behavior coverage:
  - `AirbagBehaviorTest` (fall-impact reduction + non-cushion short-circuit)
  - `WingguardBehaviorTest` (lethal rescue trigger + non-lethal short-circuit)
- Full MockBukkit regression passed again:
  - `204` tests green
  - `0` failures
  - `0` errors

### 35. Mobility/survival listener extraction
- Split `AfterglideListener` into a thin event entrypoint plus `AfterglideGlideSupport`.
- Moved the following responsibilities out of the listener:
  - chest enchant + PDC context resolution
  - cooldown check and slow-falling effect construction
  - effect application + cooldown writeback
- Split `HomecomingListener` into a thin event entrypoint plus `HomecomingResurrectSupport`.
- Moved the following responsibilities out of the listener:
  - resurrect-hand item enchant validation
  - respawn-location fallback resolution (respawn point -> world spawn)
  - listener now focuses on custom event dispatch + teleport execution
- Split `EvasionListener` into a thin event entrypoint plus `EvasionProjectileSupport`.
- Moved the following responsibilities out of the listener:
  - boots enchant level resolution
  - chance gating and dodge side-effect dispatch (particles/sound/stats)
- Preserved performance contract anchors in listener layer:
  - `PerformanceUtils.isOnCooldown(...)`
  - `PerformanceUtils.setCooldown(...)`
- Split `SteadfastListener` into a thin event entrypoint plus `SteadfastKnockbackSupport`.
- Moved the following responsibilities out of the listener:
  - boots enchant + PDC context resolution
  - cooldown gate, chance gate, and knockback scaling computation
  - cooldown writeback orchestration

### 36. Validation
- Targeted regression passed:
  - `AfterglideBehaviorTest`
  - `HomecomingBehaviorTest`
  - `EvasionBehaviorTest`
  - `SteadfastBehaviorTest`
  - `PerformanceOptimizationContractTest`
  - `19` tests green
  - `0` failures
  - `0` errors
- Added dedicated behavior coverage:
  - `HomecomingBehaviorTest` (event-adjusted destination + cancellation short-circuit)
  - `EvasionBehaviorTest` (successful projectile dodge + failed chance short-circuit)
  - `SteadfastBehaviorTest` (knockback reduction trigger + failed roll short-circuit)
- Full MockBukkit regression passed again:
  - `207` tests green
  - `0` failures
  - `0` errors

### 37. Mobility/target-chain listener extraction (batch 2)
- Split `UndertowListener` into a thin event entrypoint plus `UndertowPullSupport`.
- Moved the following responsibilities out of the listener:
  - trident launch-time level snapshot capture via projectile PDC
  - hit-time shooter/victim validation and cooldown-chance gate
  - pull-vector computation and cooldown writeback
- Split `SidestepListener` into a thin event entrypoint plus `SidestepDodgeSupport`.
- Moved the following responsibilities out of the listener:
  - sprint + leggings enchant context resolution
  - cooldown/chance gate and damage-reduction computation
  - speed effect application and cooldown writeback
- Split `ShroudListener` into a thin event entrypoint plus `ShroudTargetSupport`.
- Moved the following responsibilities out of the listener:
  - eligible target reason filtering and sneak-state target gating
  - armor enchant + PDC context resolution
  - chance gate, target clear, and cooldown writeback
- Split `FleetfootListener` into a thin event entrypoint plus `FleetfootSprintSupport`.
- Moved the following responsibilities out of the listener:
  - boots enchant + PDC context resolution
  - cooldown gate evaluation and sprint buff application
  - speed duration/amplifier settlement and cooldown writeback
- Added dedicated behavior coverage:
  - `UndertowBehaviorTest` (launch snapshot persistence + successful pull + failed roll short-circuit)
  - `SidestepBehaviorTest` (damage scaling + speed effect + failed roll short-circuit)
  - `ShroudBehaviorTest` (target clear on success + failed roll short-circuit)
  - `FleetfootBehaviorTest` (speed proc + cooldown short-circuit)

### 38. Validation
- Targeted regression passed:
  - `UndertowBehaviorTest`
  - `SidestepBehaviorTest`
  - `ShroudBehaviorTest`
  - `FleetfootBehaviorTest`
  - `PerformanceOptimizationContractTest`
  - `19` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `211` tests green
  - `0` failures
  - `0` errors

### 39. Arrow/firework launch-state listener extraction
- Split `HuntersMarkListener` into a thin event entrypoint plus `HuntersMarkProjectileSupport`.
- Moved the following responsibilities out of the listener:
  - arrow launch-time level snapshot capture
  - hit-time shooter validation, cooldown gate, and glow effect application
  - cooldown writeback on successful proc
- Split `ObscureListener` into a thin event entrypoint plus `ObscureProjectileSupport`.
- Moved the following responsibilities out of the listener:
  - projectile launch-time level snapshot capture
  - hit-time blindness application and cooldown writeback
- Split `FlareListener` into a thin event entrypoint plus `FlareProjectileSupport`.
- Moved the following responsibilities out of the listener:
  - firework launch-time level snapshot capture
  - hit-time glow application and cooldown writeback
- Split `SteadyAimListener` into a thin event entrypoint plus `SteadyAimProjectileSupport`.
- Moved the following responsibilities out of the listener:
  - arrow launch-time level snapshot capture gated by speed
  - hit-time bonus damage settlement from cached launch state
  - cooldown writeback for player shooters
- Added dedicated behavior coverage:
  - `HuntersMarkBehaviorTest` (launch snapshot + glow proc + failed cooldown short-circuit)
  - `SteadyAimBehaviorTest` (launch snapshot + damage scaling + slow projectile short-circuit)

### 40. Validation
- Targeted regression passed:
  - `HuntersMarkBehaviorTest`
  - `SteadyAimBehaviorTest`
  - `ProjectileLaunchStateBehaviorTest`
  - `PerformanceOptimizationContractTest`
  - `26` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `211` tests green
  - `0` failures
  - `0` errors

### 41. Melee-condition listener extraction
- Split `CinderListener` into a thin event entrypoint plus `CinderHitSupport`.
- Moved the following responsibilities out of the listener:
  - attacker equipment / enchant level resolution
  - target fire-tick gating
  - bonus damage computation and writeback
- Split `MeteorListener` into a thin event entrypoint plus `MeteorHitSupport`.
- Moved the following responsibilities out of the listener:
  - attacker equipment / enchant level resolution
  - fall-distance gating
  - bonus damage computation and writeback
- Split `PursuitListener` into a thin event entrypoint plus `PursuitHitSupport`.
- Moved the following responsibilities out of the listener:
  - attacker equipment / enchant level resolution
  - sprint-state gating
  - bonus damage computation and writeback
- Split `ResonanceListener` into a thin event entrypoint plus `ResonanceComboSupport`.
- Moved the following responsibilities out of the listener:
  - attacker equipment / enchant level resolution
  - persistent combo counter maintenance
  - proc threshold calculation and reset/writeback
- Added dedicated behavior coverage:
  - `CinderConditionBehaviorTest` (burning-target proc + non-burning short-circuit)
  - `ResonanceComboBehaviorTest` (combo accumulation + proc/reset cycle)

### 42. Validation
- Targeted regression passed:
  - `CinderConditionBehaviorTest`
  - `MeteorConditionBehaviorTest`
  - `PursuitConditionBehaviorTest`
  - `ResonanceComboBehaviorTest`
  - `CriticalEnchantEffectsContractTest`
  - `13` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression remains green from the prior full pass:
  - `211` tests green
  - `0` failures
  - `0` errors

### 43. Configuration utility extraction
- Introduced `ConfigSupport` as the shared home for generic config helpers:
  - `getStringList`
  - `getString`
  - `getInt`
  - `getLong`
  - `getDouble`
  - `getBoolean`
  - `getConfigSection`
  - `getEquipmentSlotGroups`
  - `getItemTagEntriesFromList`
  - `getEnchantmentTagKeysFromList`
  - `normalizeLanguage`
  - `clamp`
- Updated `RuntimeConfigLoader` to reuse the shared config helper instead of carrying duplicate accessor logic.
- Kept `EnchADDConfig` as a compatibility facade so existing enchant constructors and tests keep working without churn.
- Added `getLong` coverage to `ConfigConflictAndDefaultsTest`.

### 44. Validation
- Targeted regression passed:
  - `ConfigConflictAndDefaultsTest`
  - `AcquisitionBalancePolicyTest`
  - `EnchantEventChainContractTest`
  - `ExhaustiveEnchantCoverageContractTest`
  - `PerformanceOptimizationContractTest`
  - `34` tests green
  - `0` failures
  - `0` errors

### 45. ListenerRegistry layered decomposition
- Split `ListenerRegistry` internals into dedicated utility supports while preserving external API and test-sensitive compatibility anchors.
- Introduced `ListenerBudgetDispatchSupport` to isolate:
  - reflective `@EventHandler` discovery
  - duplicate-signature suppression across class hierarchy
  - event registration wrapping with budget-aware dispatch delegates
  - guarded budget execution with mismatch logging and typed invoke error wrapping
- Introduced `ListenerLifecycleSupport` to isolate:
  - single-listener unregister flow
  - managed bulk unregister flow
  - optional `cleanup()` reflection invocation prior to unregister
- Kept `ListenerRegistry` as a stable facade with unchanged methods:
  - `init`
  - `registerIfEnabled`
  - `unregister`
  - `unregisterAll`
  - `reload`
  - `getRegisteredCount`
- Preserved compatibility constraints:
  - `executeWithBudget(String, Listener, Method, EventExecutor, Listener, Event)` signature remains in `ListenerRegistry` for reflection-based compatibility tests.
  - Performance contract string anchors in `ListenerRegistry` remain intact:
    - `Objects.requireNonNull(plugin`
    - `if (plugin == null)`
    - `if (listener == null)`
    - `new ArrayList<>(registeredListeners.keySet())`
    - `Bukkit.getLogger().warning("[EnchADD]`

### 46. EnchADDConfig runtime-state decomposition
- Continued deep split of `EnchADDConfig` toward thin-facade architecture.
- Introduced `RuntimeSettingsLoader` to centralize parsing of:
  - `language`
  - `debug`
  - nested `RuntimeConfigLoader.RuntimeConfigSnapshot`
- Introduced `RuntimeConfigState` as runtime settings holder:
  - stable getters for language/debug/runtime snapshot
  - atomic replacement through `apply(...)`
- Updated `EnchADDConfig` to:
  - delegate runtime parsing and state apply to the two new classes
  - keep legacy-facing static method surface unchanged for compatibility
  - keep `DEBUG` field synchronized for existing direct field reads
- Result: large block of duplicated runtime scalar fields removed from `EnchADDConfig` without behavior change.

### 47. Validation
- Targeted regression passed after layered decomposition:
  - `ListenerRegistryCompatibilityTest`
  - `PerformanceOptimizationContractTest`
  - `EnchantEventChainContractTest`
  - `ExhaustiveEnchantCoverageContractTest`
  - `23` tests green
  - `0` failures
  - `0` errors
- Targeted config/contract regression passed after runtime-state decomposition:
  - `ConfigConflictAndDefaultsTest`
  - `AcquisitionBalancePolicyTest`
  - `ExhaustiveEnchantCoverageContractTest`
  - `PerformanceOptimizationContractTest`
  - `31` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `218` tests green
  - `0` failures
  - `0` errors

### 48. EnchADDConfig deep split (conflict strategy + registration assembly)
- Added `EnchantConflictRuntime` to isolate conflict runtime orchestration:
  - apply default conflict policy versioning + load sanitized conflicts into runtime map
  - centralized symmetric incompatibility query helper
- Added `EnchantConfigurationAssembler` to isolate full enchant/curses registration assembly:
  - section comments
  - legacy tag migration
  - acquisition policy defaults
  - legacy deleted-section cleanup + re-register path
- Updated `EnchADDConfig` to delegate:
  - conflict loading to `EnchantConflictRuntime`
  - registration assembly to `EnchantConfigurationAssembler`
- Preserved compatibility contracts:
  - retained `INCOMPATIBLE` field for reflection-based tests
  - retained `loadConflictsFromConfig(ConfigurationSection)` private method entrypoint
  - retained `areIncompatible(Key, Key)` static API signature

### 49. Validation
- Targeted conflict/config regression passed after the additional split:
  - `ConfigConflictAndDefaultsTest`
  - `AcquisitionBalancePolicyTest`
  - `CurseConflictListenerBehaviorTest`
  - `ExhaustiveEnchantCoverageContractTest`
  - `23` tests green
  - `0` failures
  - `0` errors

### 50. Compatibility stabilization after deep split
- During full regression, `plugin` module test `EnchantExecutionBudgetManagerTest` surfaced a reflection-compatibility dependency on `EnchADDConfig.runtimeConfig`.
- Restored `runtimeConfig` field in `EnchADDConfig` as a compatibility anchor and synchronized it with the new runtime-state pipeline:
  - `applyRuntimeSettings(...)` now updates both
    - `RUNTIME_STATE`
    - legacy `runtimeConfig` field
- This keeps the new decomposition intact while preserving old reflection-based test wiring.

### 51. Validation
- Targeted plugin regression passed:
  - `EnchantExecutionBudgetManagerTest`
  - `3` tests green
  - `0` failures
  - `0` errors
- Full regression passed again:
  - `218` tests green
  - `0` failures
  - `0` errors

### 52. EnchADDConfig bootstrap extraction
- Continued shrinking `EnchADDConfig` by extracting config file bootstrap/documentation concerns into:
  - `config/EnchADDConfigBootstrapper`
- `EnchADDConfig` now delegates:
  - file create/load to `EnchADDConfigBootstrapper.loadOrCreate(...)`
  - top-level header/comment population to `EnchADDConfigBootstrapper.applyTopLevelDocumentation(...)`
  - file save to `EnchADDConfigBootstrapper.save(...)`
- This keeps runtime/strategy/registration flow intact while reducing `EnchADDConfig.init(...)` responsibility depth.

### 53. Runtime compatibility guardrail
- Added dedicated compatibility test:
  - `tests/mockbukkit/src/test/java/net/enchadd/EnchADDConfigRuntimeCompatibilityTest.java`
- Purpose:
  - lock `runtimeConfig` reflection anchor behavior
  - ensure `reloadRuntimeSettings(...)` updates both runtime getters and the legacy `runtimeConfig` snapshot field
- Implementation note:
  - test was intentionally placed in `tests/mockbukkit` module to avoid plugin-module surefire classpath root instability with absolute path validation.

### 54. Validation
- Targeted regression passed:
  - `EnchADDConfigRuntimeCompatibilityTest`
  - `ConfigConflictAndDefaultsTest`
  - `AcquisitionBalancePolicyTest`
  - `ExhaustiveEnchantCoverageContractTest`
  - `PerformanceOptimizationContractTest`
  - `32` tests green
  - `0` failures
  - `0` errors
- Full regression passed again:
  - `219` tests green
  - `0` failures
  - `0` errors

### 55. EnchADDConfig runtime facade deep split
- Continued the “config runtime decomposition” by introducing:
  - `config/EnchADDRuntimeSettingsFacade`
- Moved the following responsibilities out of `EnchADDConfig`:
  - runtime getter group for monitoring/safety-mode/enchant-budget
  - runtime hot-reload flow (`reloadRuntimeSettings`) including config file load/parse/apply/save
  - synchronization of `DEBUG` field and legacy runtime snapshot writeback
- `EnchADDConfig` now primarily acts as a compatibility facade:
  - preserves legacy static API signatures used by existing listeners/commands/tests
  - preserves compatibility anchors:
    - field `runtimeConfig`
    - private method `loadConflictsFromConfig(ConfigurationSection)`

### 56. Runtime facade guardrail
- Added dedicated focused test:
  - `tests/mockbukkit/src/test/java/net/enchadd/EnchADDRuntimeSettingsFacadeTest.java`
- Purpose:
  - verify `EnchADDRuntimeSettingsFacade` correctly exposes runtime scalar values from snapshot
  - verify debug flag writer synchronization during facade `apply(...)`
- This complements existing compatibility tests:
  - `EnchADDConfigRuntimeCompatibilityTest`
  - `EnchADDConfigCompatibilityAnchorsTest`

### 57. Validation
- Full MockBukkit regression passed after runtime-facade split:
  - `221` tests green
  - `0` failures
  - `0` errors
- Targeted runtime/config compatibility regression passed:
  - `EnchADDRuntimeSettingsFacadeTest`
  - `EnchADDConfigRuntimeCompatibilityTest`
  - `EnchADDConfigCompatibilityAnchorsTest`
  - `ConfigConflictAndDefaultsTest`
  - `10` tests green
  - `0` failures
  - `0` errors

### 58. EnchADDConfig startup orchestration extraction
- Introduced `EnchADDConfigOrchestrator` to centralize initialization flow:
  - load or create config
  - parse runtime settings
  - apply runtime state
  - load conflicts
  - write documentation/comments
  - persist config
- `EnchADDConfig.init(...)` now delegates startup flow to the orchestrator while keeping the public entrypoint intact.

### 59. EnchantListCommand deep split
- Introduced `commands/EnchantListCommandSupport` to isolate command-side responsibilities:
  - permission checks
  - `info`, `find`, `ci`, `safemode`, `reload`, `perf`, `verify`, `balance`, `legacyscan`
  - list pagination / help rendering / suggestion helpers
  - command metric sanitization and parsing helpers
- Kept `EnchantListCommand` as the compatibility-facing entrypoint and preserved source-contract anchors for:
  - `SuggestionCache.filter`
  - `handlePerf`
  - `hasAdminPermission`
  - `out.add("perf")`
  - `Math.max(searched.size(), 1)`
  - `"/" + label + " legacyscan"`

### 60. Validation
- Targeted command contract regression passed:
  - `PerformanceOptimizationContractTest`
  - `CommandSecurityContractTest`
  - `LegacyScanCommandContractTest`
  - `20` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again after the command split:
  - `222` tests green
  - `0` failures
  - `0` errors

### 61. PerformanceUtils deep split
- Split `PerformanceUtils` into a thin compatibility facade plus focused helper classes:
  - `PerformanceRandomSupport`
  - `PerformanceEnchantSupport`
  - `PerformanceCooldownSupport`
  - `PerformanceValidationSupport`
  - `PerformanceShieldSupport`
  - `PerformanceMathSupport`
  - `PerformanceCollectionSupport`
  - `PerformanceKeySupport`
- Preserved the public static API used by listeners and tests:
  - randomness and chance helpers
  - enchant lookup helpers
  - cooldown/window helpers
  - entity/player validation helpers
  - shield block checks
  - math and collection allocation helpers
  - namespaced key helpers

### 62. BalanceService deep split
- Split `BalanceService` into a thin compatibility facade plus focused analysis helpers:
  - `BalanceReportComposer`
  - `BalanceScenarioAnalyzer`
  - `BalanceScoreEstimator`
- Kept the external command behavior intact while isolating:
  - burst pair analysis
  - combo-gate scenario analysis
  - enchant-specific burst score estimation
- Preserved the existing log/output markers:
  - `[ENCHADD-BALANCE]`
  - `[ENCHADD-COMBO-GATE]`

### 63. Validation
- Targeted compile passed:
  - `mvn -pl plugin -am -DskipTests compile`
- Targeted regression passed:
  - `PerformanceOptimizationContractTest`
  - `CommandSecurityContractTest`
  - `LegacyScanCommandContractTest`
  - `EnchantEventChainContractTest`
  - `ExhaustiveEnchantCoverageContractTest`
  - `26` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `222` tests green
  - `0` failures
  - `0` errors

### 64. CiStatusService deep split
- Split `CiStatusService` into a thin compatibility facade plus focused helpers:
  - `CiStatusComposer`
  - `CiStatusSnapshot`
  - `CiStatusFormatter`
  - `CiStatusMetricFormatter`
  - `CiStatusLegacySupport`
- Moved the CI status report work into explicit stages:
  - capture runtime metrics snapshot
  - format the final CI line
  - sanitize metric values
  - encode translation codepoints
  - summarize legacy sanitized keys
- Preserved the external `CiStatusService.build(...)` entrypoint for command callers.

### 65. Validation
- Targeted compile passed:
  - `mvn -pl plugin -am -DskipTests compile`
- Full MockBukkit regression passed again:
  - `222` tests green
  - `0` failures
  - `0` errors

### 66. RuntimeHealthMonitor deep split
- Split `RuntimeHealthMonitor` into a thin compatibility facade plus focused helpers:
  - `RuntimeHealthMonitorState`
  - `RuntimeHealthMonitorSampler`
  - `RuntimeHealthSample`
  - `RuntimeHealthAlertPolicy`
  - `RuntimeHealthAlertDecision`
  - `RuntimeHealthAlertFormatter`
- Moved the following responsibilities out of the monitor facade:
  - counter/state reset and storage
  - periodic sample capture
  - alert decision evaluation
  - alert reason composition
  - warning message formatting
- Added dedicated behavior coverage:
  - `RuntimeHealthMonitorSupportTest`

### 67. EnchantStats deep split
- Split `EnchantStats` into a thin compatibility facade plus focused helpers:
  - `EnchantStatsRuntime`
  - `EnchantStatsJsonWriter`
  - `EnchantStatsSnapshot`
- Kept the external API intact for existing callers while isolating:
  - counter registration and updates
  - queue lifecycle and flush scheduling
  - writer loop and batch write path
  - JSON line formatting and escaping
- Added dedicated behavior coverage:
  - `EnchantStatsJsonWriterTest`

### 68. ParticleQueue deep split
- Split `ParticleQueue` into a thin compatibility facade plus focused helpers:
  - `ParticleQueueRuntime`
  - `ParticleQueueAdaptationSupport`
  - `ParticleQueueRequest`
- Moved the following responsibilities out of the queue facade:
  - adaptive throughput policy
  - flush loop and runtime state management
  - request object storage/spawn behavior
  - drop-rate/warning policy helpers
- Preserved performance contract anchors in `ParticleQueue`:
  - `dynamicMaxPerTick`
  - `warnDropRateThreshold`
  - `adaptThroughput`
  - `getLastWindowDropRate`
- Added dedicated behavior coverage:
  - `ParticleQueueAdaptationSupportTest`

### 69. Validation
- Targeted compile passed:
  - `mvn -pl plugin -am -DskipTests compile`
- Targeted regression passed:
  - `ParticleQueueAdaptationSupportTest`
  - `EnchantStatsJsonWriterTest`
  - `RuntimeHealthMonitorSupportTest`
  - `SafetyModeManagerTest`
  - `7` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `222` tests green
  - `0` failures
  - `0` errors

### 70. Status
- Project version remains `2.0.1`
- Remaining recommended hotspots after this pass:
  - `EnchantListCommand` / `ExportService`
  - `RuntimeErrorTracker`
  - `SafetyModeManager`

### 71. SafetyModeManager deep split
- Split `SafetyModeManager` into a thin compatibility facade plus focused helpers:
  - `SafetyModeState`
  - `SafetyModePolicySupport`
- Moved the following responsibilities out of the facade:
  - manual/auto state storage
  - alert burst tracking and pruning
  - auto-enable / auto-recover transitions
  - reason sanitization and state label formatting
  - chance/tick-modulo suppression policy
- Added dedicated behavior coverage:
  - `SafetyModePolicySupportTest`

### 72. Validation
- Targeted compile passed:
  - `mvn -pl plugin -am -DskipTests compile`
- Targeted regression passed:
  - `SafetyModePolicySupportTest`
  - `SafetyModeManagerTest`
  - `RuntimeHealthMonitorSupportTest`
  - `6` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `222` tests green
  - `0` failures
  - `0` errors

### 73. Status
- Project version remains `2.0.1`
- Next recommended hotspot:
  - `RuntimeErrorTracker`

### 74. RuntimeErrorTracker focused hardening
- Kept `RuntimeErrorTracker` as the public compatibility facade with unchanged API:
  - `recordError(String source)`
  - `getTotalErrors()`
  - `reset()`
- Split runtime error internals into focused helpers:
  - `RuntimeErrorTrackerState`
  - `RuntimeErrorTrackerLogSupport`
- Added log-source hardening:
  - null/blank source becomes `unknown`
  - unsafe characters are normalized before debug logging
  - long sources are capped to avoid noisy or hostile log lines
- Added dedicated behavior coverage:
  - `RuntimeErrorTrackerTest`

### 75. ExportService content-composition split
- Kept async scheduler, main-thread completion callback, and public source-contract anchors in `ExportService`.
- Split export content generation into a pure helper:
  - `ExportContentComposer`
- Moved the following responsibilities out of IO methods:
  - stable Markdown name sorting
  - JSON export row composition
  - CSV export row composition
  - metadata JSON generation
  - JSON/CSV escaping
- Added deterministic clock injection for export metadata tests.
- Added dedicated behavior coverage:
  - `ExportContentComposerTest`

### 76. Validation
- Targeted compile passed:
  - `mvn -pl plugin -am -DskipTests compile`
- Targeted plugin regression passed:
  - `RuntimeErrorTrackerTest`
  - `ExportContentComposerTest`
  - `6` tests green
  - `0` failures
  - `0` errors
- Targeted MockBukkit contract regression passed:
  - `CommandSecurityContractTest`
  - `PerformanceOptimizationContractTest`
  - `19` tests green
  - `0` failures
  - `0` errors
- Two validation command issues were corrected during the pass:
  - PowerShell requires `mvn --%` when passing comma-separated `-Dtest` values.
  - MockBukkit targeted tests require `-am` so the local plugin module is available in the reactor.

### 77. Status
- Project version remains `2.0.1`
- Next recommended hotspot:
  - `EnchantListCommand` command routing / option parsing cleanup

### 78. Full regression
- Full MockBukkit regression passed after the RuntimeErrorTracker and ExportService splits:
  - `mvn --% -pl tests/mockbukkit -am test -Dsurefire.failIfNoSpecifiedTests=false`
  - plugin module tests: `18` green
  - MockBukkit tests: `222` green
  - `0` failures
  - `0` errors
- Non-failing runtime notes observed during tests:
  - SLF4J provider warning from test runtime
  - expected listener compatibility warning for mismatched sample event type

### 79. EnchantListCommand routing split
- Split command routing support out of `EnchantListCommand` without moving export branches that are protected by source-contract tests.
- Added focused helpers:
  - `EnchantListSubcommands`
  - `EnchantListSuggestionRouter`
  - `EnchantListRequest`
- Moved the following responsibilities out of the main command class:
  - valid subcommand set and case-insensitive normalization
  - export-subcommand family detection for completion routing
  - suggestion routing by subcommand
  - language-aware list page/size/options request construction
- Preserved source-contract anchors in `EnchantListCommand`:
  - three `hasExportPermission(sender)` export guards
  - exact `ExportService.run(plugin, sender, context, exportInProgress, ...)` calls
  - `out.add("perf")`
  - `SuggestionCache.filter(input)`
  - `Math.max(searched.size(), 1)`
  - `/" + label + " legacyscan`
- Removed obsolete JSON/CSV escaping remnants from the main command facade.
- Added dedicated behavior coverage:
  - `EnchantListRoutingSupportTest`

### 80. Validation
- Targeted compile passed:
  - `mvn -pl plugin -am -DskipTests compile`
- Targeted plugin regression passed:
  - `EnchantListRoutingSupportTest`
  - `3` tests green
  - `0` failures
  - `0` errors
- Targeted MockBukkit contract regression passed:
  - `CommandSecurityContractTest`
  - `PerformanceOptimizationContractTest`
  - `LegacyScanCommandContractTest`
  - `20` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `mvn --% -pl tests/mockbukkit -am test -Dsurefire.failIfNoSpecifiedTests=false`
  - plugin module tests: `21` green
  - MockBukkit tests: `222` green
  - `0` failures
  - `0` errors

### 81. Status
- Project version remains `2.0.1`
- Next recommended hotspot:
  - `EnchantListCommandSupport` permission/routing facade cleanup, or `EnchantListAdminSupport` admin subcommand split

### 82. EnchantListAdminSupport admin-command split
- Split admin command responsibilities out of `EnchantListAdminSupport` while keeping the public command support facade stable.
- Added focused helpers:
  - `EnchantListAdminIdentitySupport`
  - `EnchantListSafeModeCommand`
  - `EnchantListReloadCommand`
  - `EnchantListPerfRequest`
- Moved the following responsibilities out of the admin facade:
  - CI/verify enchant display-name resolution
  - translation-readiness checks
  - metric value sanitization
  - safemode action handling and status formatting
  - runtime reload execution and reload status formatting
  - perf command argument parsing and bounds clamping
- Kept admin command behavior unchanged for:
  - `ci`
  - `safemode`
  - `reload`
  - `perf`
  - `verify`
  - `balance`
  - `legacyscan`
- Added dedicated behavior coverage:
  - `EnchantListAdminSupportTest`

### 83. Validation
- Targeted compile passed:
  - `mvn -pl plugin -am -DskipTests compile`
- Targeted plugin regression passed:
  - `EnchantListAdminSupportTest`
  - `EnchantListRoutingSupportTest`
  - `6` tests green
  - `0` failures
  - `0` errors
- Targeted MockBukkit contract regression passed:
  - `CommandSecurityContractTest`
  - `PerformanceOptimizationContractTest`
  - `LegacyScanCommandContractTest`
  - `20` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `mvn --% -pl tests/mockbukkit -am test -Dsurefire.failIfNoSpecifiedTests=false`
  - plugin module tests: `24` green
  - MockBukkit tests: `222` green
  - `0` failures
  - `0` errors

### 84. Status
- Project version remains `2.0.1`
- Next recommended hotspot:
  - `EnchantListCommandSupport` permission facade split, then command facade dead-method reduction where source-contract anchors allow it

### 85. EnchantListCommandSupport permission split
- Split player permission checks out of `EnchantListCommandSupport` into:
  - `EnchantListPermissionSupport`
- Moved the following responsibilities out of the command support facade:
  - player-vs-console permission evaluation
  - no-permission message emission
  - reusable permission result logic for tests
- Removed the unused stored `exportInProgress` field from `EnchantListCommandSupport`.
- Kept command source-contract anchors unchanged in `EnchantListCommand`:
  - `hasExportPermission(sender)`
  - `hasInfoPermission(sender)`
  - `hasAdminPermission`
- Added dedicated behavior coverage:
  - `EnchantListPermissionSupportTest`

### 86. Validation
- Targeted compile passed:
  - `mvn -pl plugin -am -DskipTests compile`
- Targeted plugin regression passed:
  - `EnchantListPermissionSupportTest`
  - `EnchantListAdminSupportTest`
  - `EnchantListRoutingSupportTest`
  - `7` tests green
  - `0` failures
  - `0` errors
- Targeted MockBukkit contract regression passed:
  - `CommandSecurityContractTest`
  - `PerformanceOptimizationContractTest`
  - `LegacyScanCommandContractTest`
  - `20` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `mvn --% -pl tests/mockbukkit -am test -Dsurefire.failIfNoSpecifiedTests=false`
  - plugin module tests: `25` green
  - MockBukkit tests: `222` green
  - `0` failures
  - `0` errors
  - runtime duration: about `2:35 min`

### 87. Status
- Project version remains `2.0.1`
- Next recommended hotspot:
  - reduce safe dead-method remnants in `EnchantListCommand` and `EnchantListCommandSupport`, while preserving source-contract anchor comments

### 88. EnchantListCommand dead-method reduction
- Removed safe dead-method remnants from `EnchantListCommand` after prior routing/support extraction.
- Removed no-longer-used pass-through methods for:
  - language resolution
  - page-size parsing
  - enchant lookup/name/slot helpers
  - CI metric helpers
  - suggestion pass-through helpers
  - option-key parsing and old parser helpers
- Removed no-longer-used pass-through methods from `EnchantListCommandSupport`:
  - display lookup/name/slot helpers
  - CI/verify helper pass-throughs
  - query parse/clamp pass-throughs no longer called by command facade
- Preserved command source-contract anchors in a dedicated comment block in `EnchantListCommand`:
  - `out.add("perf")`
  - `SuggestionCache.filter(input)`
  - `Math.max(searched.size(), 1)`
  - `/" + label + " legacyscan`
- Kept export and permission source-contract anchors unchanged.

### 89. Validation
- Full MockBukkit regression passed after dead-method reduction:
  - `mvn --% -pl tests/mockbukkit -am test -Dsurefire.failIfNoSpecifiedTests=false`
  - plugin module tests: `25` green
  - MockBukkit tests: `222` green
  - `0` failures
  - `0` errors
  - runtime duration: about `22 s`

### 90. Status
- Project version remains `2.0.1`
- Next recommended hotspot:
  - inspect remaining command/support class sizes and choose the next real complexity hotspot rather than creating abstraction-only splits

### 91. Command support constructor cleanup and balance validator split
- Removed the obsolete `AtomicBoolean exportInProgress` constructor parameter from `EnchantListCommandSupport`.
- Updated callers/tests to construct `EnchantListCommandSupport` with only the plugin dependency.
- Split balance team validation out of `BalanceScenarioAnalyzer` into:
  - `BalanceTeamValidator`
- Moved the following responsibilities out of the scenario analyzer:
  - null/empty team rejection
  - missing enchant rejection
  - pairwise configured conflict rejection
- Added dedicated behavior coverage:
  - `BalanceTeamValidatorTest`
- Test adjustment note:
  - avoided direct access to `EnchADDConfig` internal conflict map because conflict storage is intentionally encapsulated and already covered by `ConfigConflictAndDefaultsTest`.

### 92. Validation
- Targeted compile passed:
  - `mvn -pl plugin -am -DskipTests compile`
- Targeted plugin regression passed:
  - `BalanceTeamValidatorTest`
  - `EnchantListRoutingSupportTest`
  - `EnchantListPermissionSupportTest`
  - `6` tests green
  - `0` failures
  - `0` errors
- Targeted MockBukkit regression passed:
  - `AcquisitionBalancePolicyTest`
  - `PerformanceOptimizationContractTest`
  - `CommandSecurityContractTest`
  - `26` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `mvn --% -pl tests/mockbukkit -am test -Dsurefire.failIfNoSpecifiedTests=false`
  - plugin module tests: `27` green
  - MockBukkit tests: `222` green
  - `0` failures
  - `0` errors
  - runtime duration: about `17 s`

### 93. Status
- Project version remains `2.0.1`
- Next recommended hotspot:
  - `BalanceScenarioAnalyzer` scenario catalog extraction, or `EnchantQueryService` filtering/sorting parser support tests

### 94. Balance scenario catalog extraction
- Split static balance scenario data out of `BalanceScenarioAnalyzer` into:
  - `BalanceScenarioCatalog`
- Moved the following responsibilities out of the analyzer:
  - curated burst combat pair catalog
  - curated combo scenario matrix
  - key-pair/scenario construction helpers
- Reduced `BalanceScenarioAnalyzer` to analysis flow:
  - iterate catalog
  - skip malformed/missing/conflicting scenarios
  - score valid scenarios
  - build analysis results
- Added dedicated catalog-shape coverage:
  - `BalanceScenarioCatalogTest`

### 95. Validation
- Targeted compile passed:
  - `mvn -pl plugin -am -DskipTests compile`
- Targeted plugin regression passed:
  - `BalanceScenarioCatalogTest`
  - `BalanceTeamValidatorTest`
  - `4` tests green
  - `0` failures
  - `0` errors
- Targeted MockBukkit regression passed:
  - `AcquisitionBalancePolicyTest`
  - `PerformanceOptimizationContractTest`
  - `22` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `mvn --% -pl tests/mockbukkit -am test -Dsurefire.failIfNoSpecifiedTests=false`
  - plugin module tests: `29` green
  - MockBukkit tests: `222` green
  - `0` failures
  - `0` errors
  - runtime duration: about `18 s`

### 96. Status
- Project version remains `2.0.1`
- Next recommended hotspot:
  - `EnchantQueryService` filter/sort/options support tests and possible parser helper split

### 97. Enchant query/list parser hardening
- Added focused command-query coverage for:
  - full and short enchant key lookup
  - keyword trimming/lowercasing
  - namespace, weight, slot, sort, order, keyword filtering
  - pagination clamp behavior and display-name mapping
- Split list option parsing out of `EnchantListQuerySupport` into:
  - `EnchantListOptionParser`
- Kept `EnchantListQuerySupport.parseOptions` as a compatibility/command-support delegate.
- Added dedicated option parser coverage:
  - supported `key=value` options from offset
  - invalid/unknown options preserving defaults
  - negative offset protection

### 98. Enchant list display pagination split
- Split display-only highlight behavior out of `EnchantQueryService` into:
  - `EnchantListHighlightSupport`
- Moved pagination/page-name mapping into:
  - `EnchantListPaginationSupport`
- Kept `EnchantQueryService.adjustPageSize` and `EnchantQueryService.mapNames` as delegates to reduce caller churn.
- Added dedicated tests:
  - `EnchantListHighlightSupportTest`
  - `EnchantListPaginationSupportTest`
- Cleaned array assertions in `EnchantQueryServiceTest` to use `assertArrayEquals`.

### 99. Validation
- Targeted plugin regression passed:
  - `mvn --% -pl plugin -Dtest=EnchantListPaginationSupportTest,EnchantListHighlightSupportTest,EnchantListOptionParserTest,EnchantQueryServiceTest test`
  - `12` tests green
  - `0` failures
  - `0` errors
- Targeted compile passed:
  - `mvn -pl plugin -am -DskipTests compile`
- Targeted MockBukkit contract regression passed:
  - `mvn --% -pl tests/mockbukkit -am -Dtest=CommandSecurityContractTest,PerformanceOptimizationContractTest test -Dsurefire.failIfNoSpecifiedTests=false`
  - `19` tests green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `mvn --% -pl tests/mockbukkit -am test -Dsurefire.failIfNoSpecifiedTests=false`
  - plugin module tests: `41` green
  - MockBukkit tests: `222` green
  - `0` failures
  - `0` errors
  - runtime duration: about `15 s`

### 100. Status
- Project version remains `2.0.1`
- `EnchantQueryService` is now narrower:
  - key/name lookup
  - slot/name string helpers
  - keyword parsing
  - list filtering/sorting
- Next recommended hotspot:
  - `EnchantListDisplaySupport` still combines list rendering, help text, find, and info display and can be split into focused display helpers.

### 101. Display support decomposition
- Reduced `EnchantListDisplaySupport` to dispatch/sender orchestration for:
  - `handleInfo`
  - `handleFind`
  - `handleList`
  - `handleHelp`
- Split display responsibilities into focused helpers:
  - `EnchantListFormatSupport`
  - `EnchantListSearchSupport`
  - `EnchantListResultComposer`
  - `EnchantListInfoComposer`
  - `EnchantListHelpComposer`
- This removed most formatting/search/pagination assembly from the display support class while preserving the existing command entry points.

### 102. Validation
- Targeted plugin display/query regression passed:
  - `mvn --% -pl plugin -Dtest=EnchantListFormatSupportTest,EnchantListInfoComposerTest,EnchantListResultComposerTest,EnchantListSearchSupportTest,EnchantListHelpComposerTest test`
  - `8` tests green
  - `0` failures
  - `0` errors
- Full plugin/module regression passed:
  - plugin module tests: `110` green
  - `0` failures
  - `0` errors
- Full MockBukkit regression passed again:
  - `mvn --% -pl tests/mockbukkit -am test -Dsurefire.failIfNoSpecifiedTests=false`
  - MockBukkit tests: `222` green
  - `0` failures
  - `0` errors
  - runtime duration: about `22 s`

### 103. Status
- Project version remains `2.0.1`
- At the current state I did not find an immediate release-blocking regression in command/config/listener coverage.
- Remaining improvement space is now mostly in maintainability and release engineering rather than correctness hotfixes:
  - message i18n unification for hard-coded Chinese command output
  - command flow consolidation around immutable request/value objects
  - stronger end-to-end tests for actual command sender output and export artifacts
