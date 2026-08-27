package net.enchadd;

import net.enchadd.enchants.HuntersMarkEnchant;
import net.enchadd.listeners.HuntersMarkListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.ArrowMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class HuntersMarkBehaviorTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void huntersMarkTagsArrowAndAppliesGlowAfterWeaponSwap() throws Exception {
        PlayerMock shooter = server.addPlayer("hunters_mark_shooter");
        PlayerMock victim = server.addPlayer("hunters_mark_victim");
        shooter.teleport(server.getWorld("world").getSpawnLocation());
        victim.teleport(server.getWorld("world").getSpawnLocation().add(8.0, 0.0, 0.0));
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(2));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());

        HuntersMarkListener listener = new HuntersMarkListener();
        HuntersMarkEnchant config = Mockito.mock(HuntersMarkEnchant.class);
        when(config.getCooldownTicks()).thenReturn(40);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getMarkSecondsPerLevel()).thenReturn(3);

        NamespacedKey key = new NamespacedKey("enchadd", "hunters_mark_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        ItemStack mainHand = Mockito.mock(ItemStack.class);
        when(equipment.getItemInMainHand()).thenReturn(mainHand);
        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(shooter)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(mainHand, Enchantment.SHARPNESS)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(shooter)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isPlayerValid(shooter)).thenReturn(true);
            utils.when(() -> PerformanceUtils.isOnCooldown(pdc, key, 40)).thenReturn(false);
            utils.when(() -> PerformanceUtils.rollChance(1.0)).thenReturn(true, false);
            utils.when(() -> PerformanceUtils.setCooldown(pdc, key)).thenAnswer(invocation -> null);

            listener.onShoot(new ProjectileLaunchEvent(arrow));
            assertEquals(2, arrow.getPersistentDataContainer().get(key, PersistentDataType.INTEGER));

            shooter.getInventory().setItemInMainHand(new ItemStack(Material.CROSSBOW));

            EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
            when(event.getDamager()).thenReturn(arrow);
            when(event.getEntity()).thenReturn(victim);

            listener.onHit(event);
            assertTrue(victim.hasPotionEffect(PotionEffectType.GLOWING));

            victim.removePotionEffect(PotionEffectType.GLOWING);
            listener.onHit(event);
            assertFalse(victim.hasPotionEffect(PotionEffectType.GLOWING));
        }
    }

    private static ItemStack enchantedCrossbow(int level) {
        ItemStack item = new ItemStack(Material.CROSSBOW);
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, level);
        return item;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
