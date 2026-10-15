# Priority enchantment source audit

This is a source-level audit only. It does not prove a real in-game trigger.

| Enchantment | Common | Fabric 1.21.11 | Fabric 26.3 | Forge 1.21.11 | Forge 26.3 | NeoForge 1.21.11 | NeoForge 26.3 | Paper |
|---|---|---|---|---|---|---|---|---|
| bind | YES | NO | NO | NO | NO | NO | NO | YES |

### bind
- **common**: mod/common/src/main/java/net/enchadd/mod/common/ProjectileStatusBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/BindListener.java, plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java, plugin/src/main/java/net/enchadd/listeners/support/BindProjectileSupport.java

| cadence | YES | NO | NO | NO | NO | NO | NO | YES |

### cadence
- **common**: mod/common/src/main/java/net/enchadd/mod/common/MeleeEffectBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/CadenceListener.java, plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java

| resonance | YES | NO | NO | NO | NO | NO | NO | YES |

### resonance
- **common**: mod/common/src/main/java/net/enchadd/mod/common/ResonanceBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java, plugin/src/main/java/net/enchadd/listeners/ResonanceListener.java, plugin/src/main/java/net/enchadd/listeners/support/ResonanceComboSupport.java

| underdog | YES | NO | NO | NO | NO | NO | NO | YES |

### underdog
- **common**: mod/common/src/main/java/net/enchadd/mod/common/MeleeEffectBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java, plugin/src/main/java/net/enchadd/listeners/UnderdogListener.java

| poise | YES | YES | YES | YES | YES | YES | YES | YES |

### poise
- **common**: mod/common/src/main/java/net/enchadd/mod/common/PoiseBehavior.java
- **fabric-1.21.11**: mod/fabric-1.21.11/src/main/java/net/enchadd/mod/mixin/PoiseDamageMixin.java
- **fabric-26.3**: mod/fabric-26.3/src/main/java/net/enchadd/mod/mixin/PoiseDamageMixin.java
- **forge-1.21.11**: mod/forge-1.21.11/src/main/java/net/enchadd/mod/EnchADDForgePoise.java
- **forge-26.3**: mod/forge-26.3/src/main/java/net/enchadd/mod/EnchADDForgePoise.java
- **neoforge-1.21.11**: mod/neoforge-1.21.11/src/main/java/net/enchadd/mod/EnchADDNeoForgePoise.java
- **neoforge-26.3**: mod/neoforge-26.3/src/main/java/net/enchadd/mod/EnchADDNeoForgePoise.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java, plugin/src/main/java/net/enchadd/listeners/PoiseListener.java

| highground | YES | NO | NO | NO | NO | NO | NO | YES |

### highground
- **common**: mod/common/src/main/java/net/enchadd/mod/common/HighgroundBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/HighgroundListener.java, plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java

| measured | YES | NO | NO | NO | NO | NO | NO | YES |

### measured
- **common**: mod/common/src/main/java/net/enchadd/mod/common/MeasuredBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java, plugin/src/main/java/net/enchadd/listeners/MeasuredListener.java

| rally | YES | NO | NO | NO | NO | NO | NO | YES |

### rally
- **common**: mod/common/src/main/java/net/enchadd/mod/common/MeleeEffectBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java, plugin/src/main/java/net/enchadd/listeners/RallyListener.java

| lodestar | YES | NO | NO | NO | NO | NO | NO | YES |

### lodestar
- **common**: mod/common/src/main/java/net/enchadd/mod/common/LodestarBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java, plugin/src/main/java/net/enchadd/listeners/LodestarListener.java

| starwish | YES | NO | NO | NO | NO | NO | NO | YES |

### starwish
- **common**: mod/common/src/main/java/net/enchadd/mod/common/StarwishBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java, plugin/src/main/java/net/enchadd/listeners/StarwishListener.java, plugin/src/main/java/net/enchadd/listeners/support/StarwishEffectSupport.java

| waysong | YES | NO | NO | NO | NO | NO | NO | YES |

### waysong
- **common**: mod/common/src/main/java/net/enchadd/mod/common/WaysongBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java, plugin/src/main/java/net/enchadd/listeners/WaysongListener.java, plugin/src/main/java/net/enchadd/listeners/support/WaysongEffectSupport.java

| initiative | YES | NO | NO | NO | NO | NO | NO | YES |

### initiative
- **common**: mod/common/src/main/java/net/enchadd/mod/common/MeleeEffectBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/InitiativeListener.java, plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java

| wingclip | YES | NO | NO | NO | NO | NO | NO | YES |

### wingclip
- **common**: mod/common/src/main/java/net/enchadd/mod/common/MeleeEffectBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java, plugin/src/main/java/net/enchadd/listeners/WingclipListener.java

| tracer | YES | NO | NO | NO | NO | NO | NO | YES |

### tracer
- **common**: mod/common/src/main/java/net/enchadd/mod/common/MeleeEffectBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java, plugin/src/main/java/net/enchadd/listeners/TracerListener.java

| pursuit | YES | NO | NO | NO | NO | NO | NO | YES |

### pursuit
- **common**: mod/common/src/main/java/net/enchadd/mod/common/MeleeEffectBehavior.java
- **paper**: plugin/src/main/java/net/enchadd/listeners/ListenerRegistrationPlan.java, plugin/src/main/java/net/enchadd/listeners/PursuitListener.java, plugin/src/main/java/net/enchadd/listeners/support/PursuitHitSupport.java

