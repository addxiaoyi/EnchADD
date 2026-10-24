package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ThirstBehavior {

    private static final ResourceKey<Enchantment> THIRST = key("thirst_curse");
    private static final int MAX_LEVEL = 3;
    private static final int COMBAT_WINDOW_TICKS = 160;
    private static final double REGEN_REDUCTION_PER_LEVEL = 0.3;
    private static final int EXTRA_HUNGER_LOSS_PER_LEVEL = 1;
    private static final int MAX_EXTRA_HUNGER_LOSS = 4;
    private static final double MAX_REGEN_REDUCTION = 0.9;
    private static final Map<UUID, Long> COMBAT_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> PREVIOUS_FOOD = new ConcurrentHashMap<>();

    private ThirstBehavior() {
    }

    public static void markFromDamage(LivingEntity victim, DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f) {
            return;
        }
        if (victim instanceof ServerPlayer player) {
            markCombat(player);
        }
        markEntity(source.getEntity());
        markEntity(source.getDirectEntity());
    }

    public static float adjustHealing(ServerPlayer player, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f || !isCombatActive(player)) {
            return amount;
        }
        int level = level(player);
        if (level <= 0) {
            return amount;
        }
        double reduction = Math.min(MAX_REGEN_REDUCTION, REGEN_REDUCTION_PER_LEVEL * level);
        float adjusted = (float) (amount * (1.0 - reduction));
        return Float.isFinite(adjusted) ? Math.max(0.0f, adjusted) : amount;
    }

    public static void tick(ServerPlayer player) {
        UUID id = player.getUUID();
        int currentFood = player.getFoodData().getFoodLevel();
        Integer previousFood = PREVIOUS_FOOD.put(id, currentFood);
        if (previousFood == null || currentFood >= previousFood || !isCombatActive(player)) {
            return;
        }
        int level = level(player);
        if (level <= 0) {
            return;
        }
        int extraLoss = Math.min(MAX_EXTRA_HUNGER_LOSS, EXTRA_HUNGER_LOSS_PER_LEVEL * level);
        player.getFoodData().setFoodLevel(Math.max(0, currentFood - extraLoss));
        PREVIOUS_FOOD.put(id, player.getFoodData().getFoodLevel());
    }

    private static void markEntity(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            markCombat(player);
            return;
        }
        if (entity instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer player) {
            markCombat(player);
        }
    }

    private static void markCombat(ServerPlayer player) {
        if (level(player) <= 0 || COMBAT_WINDOW_TICKS <= 0) {
            return;
        }
        long until = player.level().getGameTime() + COMBAT_WINDOW_TICKS;
        COMBAT_UNTIL.merge(player.getUUID(), until, Math::max);
    }

    private static boolean isCombatActive(ServerPlayer player) {
        long now = player.level().getGameTime();
        Long until = COMBAT_UNTIL.get(player.getUUID());
        if (until == null) {
            return false;
        }
        if (now >= until) {
            COMBAT_UNTIL.remove(player.getUUID(), until);
            return false;
        }
        return true;
    }

    private static int level(ServerPlayer player) {
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(THIRST);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, chest));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }
}
