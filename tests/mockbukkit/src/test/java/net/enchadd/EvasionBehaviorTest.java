package net.enchadd;

import net.enchadd.enchants.EvasionEnchant;
import net.enchadd.listeners.EvasionListener;
import net.enchadd.listeners.support.EvasionProjectileSupport;
import net.enchadd.utils.ParticleQueue;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EvasionBehaviorTest {

    @Test
    void evasionCancelsProjectileDamageOnTriggerAndSkipsFailedChance() {
        EvasionEnchant config = Mockito.mock(EvasionEnchant.class);
        when(config.getCooldownTicks()).thenReturn(100);
        when(config.getTriggerChance()).thenReturn(0.2);

        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "evasion_test");
        EvasionListener listener = new EvasionListener(
                Enchantment.UNBREAKING,
                config,
                cooldownKey,
                new EvasionProjectileSupport()
        );

        World world = Mockito.mock(World.class);
        Player victim = Mockito.mock(Player.class);
        when(victim.getWorld()).thenReturn(world);
        when(victim.getLocation()).thenAnswer(invocation -> new Location(world, 0.0, 64.0, 0.0));

        AbstractArrow arrow = Mockito.mock(AbstractArrow.class);
        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamager()).thenReturn(arrow);

        ItemStack boots = Mockito.mock(ItemStack.class);
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getBoots()).thenReturn(boots);
        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(victim)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(boots, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(victim)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(pdc, cooldownKey, 100)).thenReturn(false);
            utils.when(() -> PerformanceUtils.rollChanceWithLevel(0.2, 2, 0.6)).thenReturn(true, false);
            utils.when(() -> PerformanceUtils.setCooldown(pdc, cooldownKey)).thenAnswer(invocation -> null);

            listener.onProjectile(event);
            verify(event).setCancelled(true);
            verify(world).playSound(Mockito.any(Location.class), Mockito.eq(Sound.ENTITY_ENDER_DRAGON_FLAP), Mockito.eq(0.5f), Mockito.eq(1.5f));

            clearInvocations(event, world);
            listener.onProjectile(event);
            verify(event, never()).setCancelled(true);
            verify(world, never()).playSound(Mockito.any(Location.class), Mockito.eq(Sound.ENTITY_ENDER_DRAGON_FLAP), Mockito.eq(0.5f), Mockito.eq(1.5f));
        }

        ParticleQueue.stop();
    }
}
