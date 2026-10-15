---
title: Configuration reference
description: Comprehensive reference of all configuration options available in EnchADD
---


## Common options

Options that are available for all (or most) enchantments and are required for the enchantment to work.

### anvilCost

- **Type**: `int`

Cost that will be added to the item when adding the enchantment to it in anvil.

### weight

- **Type**: `int`

Weight of the enchantment. Used if `canGetFromEnchantingTable` is set to true. Higher numbers mean it will come up more
frequently in the enchanting table.

### minimumCost

Minimum cost that will be required for the enchantment to show up in the enchanting table. **This is NOT levels**.

#### base

- **Type**: `int`

Base cost of the enchant.

#### perLevel

- **Type**: `int`

Cost to add per level of the enchantment.

### maximumCost

Maximum cost that will be required for the enchantment to show up in the enchanting table. **This is NOT levels**.

#### base

- **Type**: `int`

Base cost of the enchant.

#### perLevel

- **Type**: `int`

Cost to add per level of the enchantment.

### enchantmentTags

- **Type**: `key[]`

This is list of [tags](/configuration/input-types#tags) that the enchantment will be tagged with. This can be used to put the enchantment in enchanting table,
mark it as a curse, etc. <a href="https://minecraft.wiki/w/Enchantment_tag_(Java_Edition)" target="_blank">All vanilla</a>
and custom tags are supported.

### supportedItemTags

- **Type**: `key[]`

[Items and/or item tags](/configuration/input-types#tags) that the enchantment can be applied to.

### activeSlots

- **Type**: `string[]`

[Slots](/configuration/input-types#slot-types) that the enchantment will be active in.


### enabled

- **Type**: `boolean`

Decides if the enchantment is registered or not.

:::caution[WARNING]
If you disable an enchantment that is already on an item, it will be removed from the item when it's loaded.
It will also come with warnings in the console. It's not recommended to disable enchantments after they are already in
use.
:::

#### maxLevel

- **Type**: `int`

Maximum level of the enchantment. If set to 1, the enchantment will be a single level enchantment. If this option is not
in the configuration section for specific enchantment, it means it's locked to a single level enchantment, because logic
of the enchantment does not support multiple levels.

## Telepathy

- **Type**: `boolean`

If set to true, items teleported by the enchant will only be able to be picked up by the player that broke the block.

## Irrigation

### radius

- **Type**: `int`

Horizontal farmland radius rehydrated around the clicked farmland block. `1` means a 3x3 area.

### bypassWhenSneaking

- **Type**: `boolean`

If set to true, sneaking keeps the hydration single-target and skips the extra spread.

## Arborist

### extraBlocks

- **Type**: `int`

Maximum number of matching trunk blocks above the clicked log that are also stripped by the enchantment.

### bypassWhenSneaking

- **Type**: `boolean`

If set to true, sneaking disables the extra stripping and keeps the axe action single-target.

## Trailblazer

### radius

- **Type**: `int`

Horizontal radius expanded around the clicked block when a shovel creates a dirt path. `1` means a 3x3 road spread.

### bypassWhenSneaking

- **Type**: `boolean`

If set to true, sneaking disables the extra road spread and keeps the shovel action single-target.

## Furrow

### radius

- **Type**: `int`

Horizontal crop radius harvested around the broken crop. `1` means a 3x3 area.

### bypassWhenSneaking

- **Type**: `boolean`

If set to true, sneaking will disable the bulk harvest so the player can break a single crop only.

### matureOnly

- **Type**: `boolean`

If set to true, only fully grown crops are harvested by the extra 3x3 sweep.

## Stonewake

### durationSecondsPerLevel

- **Type**: `int`

How many seconds of haste are granted per enchant level after breaking a qualifying ore.

### hasteAmplifier

- **Type**: `int`

Amplifier of the haste effect. `0` means Haste I.

## Executioner

### maxDamageHpThreshold

- **Type**: `double`

Health threshold under which the enchantment will deal more damage.

### damageMultiplierPerLevel

- **Type**: `double`

Multiplier that will be applied to the damage dealt by the enchantment. Value of the multiplier is added to 1.0, so if
you want to deal 2x damage, set this to 1.0. If you want to deal 3x damage, set this to 2.0 and so on. This is additive.

## Airbag

### damageReductionPerLevel

- **Type**: `double`

Damage reduction that will be applied to the damage dealt by the enchantment. Value of the multiplier is added to 1.0,
so if you want to reduce damage by 50%, set this to 0.5. If you want to reduce damage by 75%, set this to 0.25 and so
on. This is additive.

## Volley

### additionalArrowsPerLevel

- **Type**: `int`

Amount of arrows to shoot additionally per shot. This is additive, so if you want to shoot 3 arrows, set this to 2.

## Ward

### cooldownTicks

- **Type**: `int`

Cooldown in ticks that will be applied to the activating items cooldown group.

### blockSound

- **Type**: `string`

Namespaced key of the sound that will play when enchantment blocks a hit.

## Panic

### panicChancePerLevel

- **Type**: `double`

Chance of the enchantment to activate when player takes damage.

## Tenderstep

### protectTurtleEggs

- **Type**: `boolean`

If set to true, the enchantment also prevents the wearer from trampling turtle eggs.

## Freshcatch

### foodLevelPerLevel

- **Type**: `int`

How many food points are restored per enchant level when the player catches edible fish.

### saturationPerLevel

- **Type**: `double`

Additional saturation restored per enchant level alongside the hunger refill.

## Starwish

### cooldownTicks

- **Type**: `int`

Cooldown after a successful sky wish. `1200` means the enchant can trigger at most once per minute.

### nightVisionSeconds

- **Type**: `int`

How long Night Vision is granted when the player successfully triggers the enchant.

### luckSeconds

- **Type**: `int`

How long the Luck effect is granted alongside the night vision bonus.

### lookUpPitchThreshold

- **Type**: `double`

Required pitch threshold for activation. More negative values mean the player must look further upward.

## Waysong

### cooldownTicks

- **Type**: `int`

Cooldown after a successful horn call. `900` means the travel buff can trigger at most once every 45 seconds.

### radius

- **Type**: `double`

Nearby player search radius around the horn user for distributing the march buff.

### speedSeconds

- **Type**: `int`

How long the speed effect lasts on the horn user and nearby players.

### speedAmplifier

- **Type**: `int`

Amplifier of the granted speed effect. `0` means Speed I.

## Delvesense

### cooldownTicks

- **Type**: `int`

Cooldown after a successful cave ping. `600` means the compass can trigger at most once every 30 seconds.

### radius

- **Type**: `double`

Nearby hostile mob search radius around the player for applying the glowing reveal.

### glowSeconds

- **Type**: `int`

How long nearby hostile mobs stay outlined after the underground pulse.

### nightVisionSeconds

- **Type**: `int`

How long Night Vision is granted to the compass user after activation.

### maxActivationY

- **Type**: `double`

Maximum Y position where the enchant is allowed to trigger. Higher positions will not consume the cooldown or apply the reveal.

## Tideshell

### cooldownTicks

- **Type**: `int`

Cooldown after a successful shell surge. `700` means the dive burst can trigger at most once every 35 seconds.

### waterBreathingSeconds

- **Type**: `int`

How long Water Breathing is granted when the enchanted nautilus shell is used while touching water.

### dolphinsGraceSeconds

- **Type**: `int`

How long Dolphin's Grace is granted alongside the underwater breathing burst.
