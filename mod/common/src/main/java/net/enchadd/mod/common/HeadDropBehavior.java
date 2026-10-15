package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class HeadDropBehavior {

    private static final ResourceKey<Enchantment> BEHEADING = key("beheading");
    private static final ResourceKey<Enchantment> DECAPITATE = key("decapitate");

    private HeadDropBehavior() {
    }

    public static void onDeath(LivingEntity victim, DamageSource source) {
        if (victim == null || source == null || source.getDirectEntity() != source.getEntity()
                || !(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        Holder<Enchantment> beheading = victim.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(BEHEADING);
        int beheadingLevel = Math.min(3, EnchantmentHelper.getEnchantmentLevel(beheading, attacker));
        if (beheadingLevel > 0 && attacker.getRandom().nextDouble() < Math.min(0.5, 0.1 * beheadingLevel)) {
            ItemStack head = headFor(victim);
            if (!head.isEmpty()) {
                drop(victim, head);
            }
        }

        if (!(victim instanceof ServerPlayer)) {
            return;
        }
        Holder<Enchantment> decapitate = victim.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(DECAPITATE);
        int decapitateLevel = Math.min(3, EnchantmentHelper.getEnchantmentLevel(decapitate, attacker));
        if (decapitateLevel > 0 && attacker.getRandom().nextDouble() < Math.min(0.3, 0.1 * decapitateLevel)) {
            drop(victim, new ItemStack(Items.PLAYER_HEAD));
        }
    }

    private static ItemStack headFor(LivingEntity entity) {
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        if (type.equals("zombie") || type.equals("husk") || type.equals("drowned")) return new ItemStack(Items.ZOMBIE_HEAD);
        if (type.equals("skeleton") || type.equals("stray") || type.equals("bogged")) return new ItemStack(Items.SKELETON_SKULL);
        if (type.equals("wither_skeleton")) return new ItemStack(Items.WITHER_SKELETON_SKULL);
        if (type.equals("creeper")) return new ItemStack(Items.CREEPER_HEAD);
        if (type.equals("ender_dragon")) return new ItemStack(Items.DRAGON_HEAD);
        if (type.equals("piglin") || type.equals("piglin_brute") || type.equals("zombified_piglin")) return new ItemStack(Items.PIGLIN_HEAD);
        if (entity instanceof ServerPlayer) return new ItemStack(Items.PLAYER_HEAD);
        return ItemStack.EMPTY;
    }

    private static void drop(LivingEntity victim, ItemStack item) {
        victim.level().addFreshEntity(new ItemEntity(victim.level(), victim.getX(), victim.getY(), victim.getZ(), item));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }
}
