package net.enchadd.mod;

import net.enchadd.mod.common.NourishRules;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

final class EnchADDNeoForgeNourishBehavior {
    private static final ResourceKey<Enchantment> NOURISH = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "nourish"));
    private EnchADDNeoForgeNourishBehavior() {}
    static void register() { NeoForge.EVENT_BUS.addListener(LivingEntityUseItemEvent.Finish.class, EnchADDNeoForgeNourishBehavior::onFinish); }
    private static void onFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) return;
        ItemStack food = event.getItem();
        if (food.isEmpty() || !food.has(net.minecraft.core.component.DataComponents.FOOD)) return;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(NOURISH);
        int level = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getItemBySlot(EquipmentSlot.CHEST)));
        int duration = NourishRules.durationTicks(level);
        if (duration > 0) player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, NourishRules.amplifier(), false, false, true));
    }
}
