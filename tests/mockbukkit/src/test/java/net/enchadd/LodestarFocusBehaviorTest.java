package net.enchadd;

import net.enchadd.enchants.LodestarEnchant;
import net.enchadd.listeners.LodestarListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class LodestarFocusBehaviorTest {

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
    void lodestarRewardsStayingOnTheSameTarget() throws Exception {
        PlayerMock attacker = server.addPlayer("lodestar_attacker");
        PlayerMock firstTarget = server.addPlayer("lodestar_first");
        PlayerMock secondTarget = server.addPlayer("lodestar_second");

        ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
        weapon.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        attacker.getInventory().setItemInMainHand(weapon);

        LodestarListener listener = new LodestarListener();
        LodestarEnchant config = Mockito.mock(LodestarEnchant.class);
        when(config.getFocusWindowTicks()).thenReturn(80);
        when(config.getBonusDamagePerLevel()).thenReturn(0.8);
        when(config.getMaxBonusDamage()).thenReturn(2.0);

        NamespacedKey windowKey = new NamespacedKey("enchadd", "lodestar_test_window");
        NamespacedKey targetKey = new NamespacedKey("enchadd", "lodestar_test_target");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "windowKey", windowKey);
        setField(listener, "targetKey", targetKey);

        EntityDamageByEntityEvent firstHit = Mockito.mock(EntityDamageByEntityEvent.class);
        when(firstHit.getDamager()).thenReturn(attacker);
        when(firstHit.getEntity()).thenReturn(firstTarget);
        when(firstHit.getDamage()).thenReturn(5.0);

        AtomicReference<Double> appliedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(firstHit).setDamage(anyDouble());

        listener.onMeleeHit(firstHit);
        assertNull(appliedDamage.get(), "first hit should only establish lodestar focus");

        PersistentDataContainer pdc = attacker.getPersistentDataContainer();
        assertEquals(firstTarget.getEntityId(), pdc.get(targetKey, PersistentDataType.INTEGER));

        EntityDamageByEntityEvent secondHitSameTarget = Mockito.mock(EntityDamageByEntityEvent.class);
        when(secondHitSameTarget.getDamager()).thenReturn(attacker);
        when(secondHitSameTarget.getEntity()).thenReturn(firstTarget);
        when(secondHitSameTarget.getDamage()).thenReturn(5.0);
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(secondHitSameTarget).setDamage(anyDouble());

        appliedDamage.set(null);
        listener.onMeleeHit(secondHitSameTarget);
        assertEquals(7.0, appliedDamage.get(), 0.0001, "second hit on the same target should gain lodestar bonus");

        EntityDamageByEntityEvent hitDifferentTarget = Mockito.mock(EntityDamageByEntityEvent.class);
        when(hitDifferentTarget.getDamager()).thenReturn(attacker);
        when(hitDifferentTarget.getEntity()).thenReturn(secondTarget);
        when(hitDifferentTarget.getDamage()).thenReturn(5.0);
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(hitDifferentTarget).setDamage(anyDouble());

        appliedDamage.set(null);
        listener.onMeleeHit(hitDifferentTarget);
        assertNull(appliedDamage.get(), "switching targets should consume the old focus and not bonus immediately");
        assertEquals(secondTarget.getEntityId(), pdc.get(targetKey, PersistentDataType.INTEGER));
        assertEquals(true, PerformanceUtils.isWindowActive(pdc, windowKey));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
