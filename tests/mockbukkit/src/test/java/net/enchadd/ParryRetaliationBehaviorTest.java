package net.enchadd;

import net.enchadd.enchants.ParryEnchant;
import net.enchadd.listeners.ParryListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class ParryRetaliationBehaviorTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void parryRewardsARealShieldCounterattackAgainstTheSameAttacker() throws Exception {
        PlayerMock backing = server.addPlayer("parry_backing");

        ItemStack shield = new ItemStack(Material.SHIELD);
        shield.addUnsafeEnchantment(Enchantment.SHARPNESS, 2);
        ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);

        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getItemInOffHand()).thenReturn(shield);
        when(equipment.getItemInMainHand()).thenReturn(weapon);

        Player defender = Mockito.mock(Player.class);
        when(defender.getEquipment()).thenReturn(equipment);
        when(defender.getPersistentDataContainer()).thenReturn(backing.getPersistentDataContainer());
        when(defender.isBlocking()).thenReturn(true);
        when(defender.getHandRaisedTime()).thenReturn(5);
        when(defender.isHandRaised()).thenReturn(true);
        when(defender.getActiveItem()).thenReturn(shield);
        when(defender.getActiveItemHand()).thenReturn(EquipmentSlot.OFF_HAND);
        Location defenderLocation = new Location(Mockito.mock(World.class), 0.0, 64.0, 0.0);
        defenderLocation.setDirection(new Vector(0.0, 0.0, 1.0));
        when(defender.getLocation()).thenReturn(defenderLocation);

        LivingEntity attacker = Mockito.mock(LivingEntity.class);
        when(attacker.getEntityId()).thenReturn(42);
        when(attacker.getLocation()).thenReturn(new Location(defenderLocation.getWorld(), 0.0, 64.0, 2.0));

        LivingEntity differentTarget = Mockito.mock(LivingEntity.class);
        when(differentTarget.getEntityId()).thenReturn(99);

        ParryListener listener = new ParryListener();
        ParryEnchant config = Mockito.mock(ParryEnchant.class);
        when(config.getCooldownTicks()).thenReturn(80);
        when(config.getRetaliationWindowTicks()).thenReturn(60);
        when(config.getBonusDamagePerLevel()).thenReturn(1.0);
        when(config.getMaxBonusDamage()).thenReturn(2.0);
        when(config.getMaxLevel()).thenReturn(2);

        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "parry_test_cooldown");
        NamespacedKey windowKey = new NamespacedKey("enchadd", "parry_test_window");
        NamespacedKey targetKey = new NamespacedKey("enchadd", "parry_test_target");
        NamespacedKey levelKey = new NamespacedKey("enchadd", "parry_test_level");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "cooldownKey", cooldownKey);
        setField(listener, "windowKey", windowKey);
        setField(listener, "targetKey", targetKey);
        setField(listener, "levelKey", levelKey);

        EntityDamageByEntityEvent blockedEvent = Mockito.mock(EntityDamageByEntityEvent.class);
        when(blockedEvent.getEntity()).thenReturn(defender);
        when(blockedEvent.getDamager()).thenReturn(attacker);
        when(blockedEvent.getDamageSource()).thenReturn(null);
        when(blockedEvent.getDamage()).thenReturn(5.0);

        listener.onBlocked(blockedEvent);

        PersistentDataContainer pdc = backing.getPersistentDataContainer();
        assertTrue(PerformanceUtils.isWindowActive(pdc, windowKey), "blocking the hit should arm a parry retaliation window");
        assertEquals(42, pdc.get(targetKey, PersistentDataType.INTEGER));

        EntityDamageByEntityEvent wrongTargetCounter = Mockito.mock(EntityDamageByEntityEvent.class);
        when(wrongTargetCounter.getDamager()).thenReturn(defender);
        when(wrongTargetCounter.getEntity()).thenReturn(differentTarget);
        when(wrongTargetCounter.getDamage()).thenReturn(5.0);
        when(wrongTargetCounter.getFinalDamage()).thenReturn(5.0);

        AtomicReference<Double> appliedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(wrongTargetCounter).setDamage(anyDouble());

        listener.onCounterattack(wrongTargetCounter);
        assertNull(appliedDamage.get(), "hitting a different target should not cash out the parry window");
        assertTrue(PerformanceUtils.isWindowActive(pdc, windowKey), "parry window should remain armed for the original attacker");

        EntityDamageByEntityEvent correctCounter = Mockito.mock(EntityDamageByEntityEvent.class);
        when(correctCounter.getDamager()).thenReturn(defender);
        when(correctCounter.getEntity()).thenReturn(attacker);
        when(correctCounter.getDamage()).thenReturn(5.0);
        when(correctCounter.getFinalDamage()).thenReturn(5.0);
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(correctCounter).setDamage(anyDouble());

        appliedDamage.set(null);
        listener.onCounterattack(correctCounter);
        assertEquals(7.0, appliedDamage.get(), 0.0001, "the next strike against the same attacker should gain parry bonus damage");
        assertFalse(PerformanceUtils.isWindowActive(pdc, windowKey), "successful parry retaliation should consume the window");
        assertNull(pdc.get(targetKey, PersistentDataType.INTEGER), "successful parry retaliation should clear the stored attacker");
    }

    @Test
    void parryUsesTheBlockedShieldLevelSnapshotInsteadOfCurrentOffhandState() throws Exception {
        PlayerMock backing = server.addPlayer("parry_snapshot_backing");

        ItemStack initialShield = new ItemStack(Material.SHIELD);
        initialShield.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        ItemStack swappedShield = new ItemStack(Material.SHIELD);
        swappedShield.addUnsafeEnchantment(Enchantment.SHARPNESS, 4);
        ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);

        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getItemInOffHand()).thenReturn(initialShield, swappedShield);
        when(equipment.getItemInMainHand()).thenReturn(weapon);

        Player defender = Mockito.mock(Player.class);
        when(defender.getEquipment()).thenReturn(equipment);
        when(defender.getPersistentDataContainer()).thenReturn(backing.getPersistentDataContainer());
        when(defender.isBlocking()).thenReturn(true);
        when(defender.getHandRaisedTime()).thenReturn(5);
        when(defender.isHandRaised()).thenReturn(true);
        when(defender.getActiveItem()).thenReturn(initialShield);
        when(defender.getActiveItemHand()).thenReturn(EquipmentSlot.OFF_HAND);
        Location defenderLocation = new Location(Mockito.mock(World.class), 0.0, 64.0, 0.0);
        defenderLocation.setDirection(new Vector(0.0, 0.0, 1.0));
        when(defender.getLocation()).thenReturn(defenderLocation);

        LivingEntity attacker = Mockito.mock(LivingEntity.class);
        when(attacker.getEntityId()).thenReturn(77);
        when(attacker.getLocation()).thenReturn(new Location(defenderLocation.getWorld(), 0.0, 64.0, 2.0));

        ParryListener listener = new ParryListener();
        ParryEnchant config = Mockito.mock(ParryEnchant.class);
        when(config.getCooldownTicks()).thenReturn(80);
        when(config.getRetaliationWindowTicks()).thenReturn(60);
        when(config.getBonusDamagePerLevel()).thenReturn(1.0);
        when(config.getMaxBonusDamage()).thenReturn(10.0);
        when(config.getMaxLevel()).thenReturn(2);

        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "parry_snapshot_cooldown");
        NamespacedKey windowKey = new NamespacedKey("enchadd", "parry_snapshot_window");
        NamespacedKey targetKey = new NamespacedKey("enchadd", "parry_snapshot_target");
        NamespacedKey levelKey = new NamespacedKey("enchadd", "parry_snapshot_level");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "cooldownKey", cooldownKey);
        setField(listener, "windowKey", windowKey);
        setField(listener, "targetKey", targetKey);
        setField(listener, "levelKey", levelKey);

        EntityDamageByEntityEvent blockedEvent = Mockito.mock(EntityDamageByEntityEvent.class);
        when(blockedEvent.getEntity()).thenReturn(defender);
        when(blockedEvent.getDamager()).thenReturn(attacker);
        when(blockedEvent.getDamageSource()).thenReturn(null);
        when(blockedEvent.getDamage()).thenReturn(5.0);
        listener.onBlocked(blockedEvent);

        EntityDamageByEntityEvent counterattack = Mockito.mock(EntityDamageByEntityEvent.class);
        when(counterattack.getDamager()).thenReturn(defender);
        when(counterattack.getEntity()).thenReturn(attacker);
        when(counterattack.getDamage()).thenReturn(5.0);
        when(counterattack.getFinalDamage()).thenReturn(5.0);

        AtomicReference<Double> appliedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(counterattack).setDamage(anyDouble());

        listener.onCounterattack(counterattack);
        assertEquals(6.0, appliedDamage.get(), 0.0001, "parry should use the shield level captured when the block happened");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
