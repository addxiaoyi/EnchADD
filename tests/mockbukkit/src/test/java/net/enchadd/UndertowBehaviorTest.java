package net.enchadd;

import net.enchadd.enchants.UndertowEnchant;
import net.enchadd.listeners.UndertowListener;
import net.enchadd.listeners.support.UndertowPullSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UndertowBehaviorTest {

    @Test
    void undertowPersistsLaunchLevelAndAppliesPullOnHit() {
        UndertowEnchant config = Mockito.mock(UndertowEnchant.class);
        when(config.getCooldownTicks()).thenReturn(80);
        when(config.getTriggerChance()).thenReturn(0.3);
        when(config.getPullStrengthBase()).thenReturn(0.2);

        NamespacedKey key = new NamespacedKey("enchadd", "undertow_test");
        UndertowListener listener = new UndertowListener(
                Enchantment.UNBREAKING,
                key,
                config,
                new UndertowPullSupport()
        );

        Trident trident = Mockito.mock(Trident.class);
        PersistentDataContainer tridentPdc = Mockito.mock(PersistentDataContainer.class);
        when(trident.getPersistentDataContainer()).thenReturn(tridentPdc);
        when(tridentPdc.has(key, PersistentDataType.INTEGER)).thenReturn(false);

        Player shooter = Mockito.mock(Player.class);
        when(trident.getShooter()).thenReturn(shooter);

        EntityEquipment launchEquipment = Mockito.mock(EntityEquipment.class);
        ItemStack mainHand = Mockito.mock(ItemStack.class);
        when(launchEquipment.getItemInMainHand()).thenReturn(mainHand);

        ProjectileLaunchEvent launchEvent = Mockito.mock(ProjectileLaunchEvent.class);
        when(launchEvent.getEntity()).thenReturn(trident);

        World world = Mockito.mock(World.class);
        when(shooter.getLocation()).thenReturn(new Location(world, 3.0, 64.0, 0.0));

        LivingEntity victim = Mockito.mock(LivingEntity.class);
        when(victim.getLocation()).thenReturn(new Location(world, 0.0, 64.0, 0.0));
        when(victim.getVelocity()).thenReturn(new Vector(0.1, 0.0, 0.0));

        EntityDamageByEntityEvent hitEvent = Mockito.mock(EntityDamageByEntityEvent.class);
        when(hitEvent.getEntity()).thenReturn(victim);
        when(hitEvent.getDamager()).thenReturn(trident);
        when(tridentPdc.get(key, PersistentDataType.INTEGER)).thenReturn(2);

        PersistentDataContainer shooterPdc = Mockito.mock(PersistentDataContainer.class);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(shooter)).thenReturn(launchEquipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(mainHand, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.isPlayerValid(shooter)).thenReturn(true);
            utils.when(() -> PerformanceUtils.getPDCSafe(shooter)).thenReturn(shooterPdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(shooterPdc, key, 80)).thenReturn(false);
            utils.when(() -> PerformanceUtils.rollChance(0.6)).thenReturn(true, false);
            utils.when(() -> PerformanceUtils.setCooldown(shooterPdc, key)).thenAnswer(invocation -> null);

            listener.onLaunch(launchEvent);
            verify(tridentPdc).set(key, PersistentDataType.INTEGER, 2);

            listener.onHit(hitEvent);
            verify(victim).setVelocity(Mockito.argThat(vector -> vector.getX() > 0.1));

            clearInvocations(victim);
            listener.onHit(hitEvent);
            verify(victim, Mockito.never()).setVelocity(Mockito.any(Vector.class));
        }
    }
}
