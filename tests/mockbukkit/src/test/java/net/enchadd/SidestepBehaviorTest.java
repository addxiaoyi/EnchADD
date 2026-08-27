package net.enchadd;

import net.enchadd.enchants.SidestepEnchant;
import net.enchadd.listeners.SidestepListener;
import net.enchadd.listeners.support.SidestepDodgeSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SidestepBehaviorTest {

    @Test
    void sidestepScalesDamageAndGrantsSpeedWhenTriggered() {
        SidestepEnchant config = Mockito.mock(SidestepEnchant.class);
        when(config.getCooldownTicks()).thenReturn(160);
        when(config.getTriggerChance()).thenReturn(0.25);
        when(config.getMaxTriggerChance()).thenReturn(0.65);
        when(config.getDamageReductionPerLevel()).thenReturn(0.12);
        when(config.getSpeedSecondsPerLevel()).thenReturn(2);

        NamespacedKey key = new NamespacedKey("enchadd", "sidestep_test");
        SidestepListener listener = new SidestepListener(
                Enchantment.UNBREAKING,
                key,
                config,
                new SidestepDodgeSupport()
        );

        Player player = Mockito.mock(Player.class);
        when(player.isSprinting()).thenReturn(true);
        Projectile projectile = Mockito.mock(Projectile.class);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getDamager()).thenReturn(projectile);
        when(event.getDamage()).thenReturn(10.0);

        final double[] scaledDamage = new double[1];
        doAnswer(invocation -> {
            scaledDamage[0] = invocation.getArgument(0);
            return null;
        }).when(event).setDamage(Mockito.anyDouble());

        ItemStack leggings = Mockito.mock(ItemStack.class);
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getLeggings()).thenReturn(leggings);
        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(leggings, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(pdc, key, 160)).thenReturn(false);
            utils.when(() -> PerformanceUtils.rollChance(0.5)).thenReturn(true, false);
            utils.when(() -> PerformanceUtils.clamp(0.24, 0.0, 0.8)).thenReturn(0.24);
            utils.when(() -> PerformanceUtils.calculateDurationTicksPerLevel(2, 2)).thenReturn(80);
            utils.when(() -> PerformanceUtils.setCooldown(pdc, key)).thenAnswer(invocation -> null);

            listener.onProjectileHit(event);
            assertEquals(7.6, scaledDamage[0], 0.0001);
            verify(player).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.SPEED
                            && effect.getDuration() == 80
                            && effect.getAmplifier() == 0
            ));

            clearInvocations(player);
            scaledDamage[0] = 0.0;
            listener.onProjectileHit(event);
            assertEquals(0.0, scaledDamage[0], 0.0001);
            verify(player, Mockito.never()).addPotionEffect(Mockito.any(PotionEffect.class));
        }
    }
}
