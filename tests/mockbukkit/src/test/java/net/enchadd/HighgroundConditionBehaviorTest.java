package net.enchadd;

import net.enchadd.enchants.HighgroundEnchant;
import net.enchadd.listeners.HighgroundListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
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

class HighgroundConditionBehaviorTest {

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
    void highgroundOnlyTriggersWhenAttackerHasMeaningfulHeightAdvantage() throws Exception {
        PlayerMock attacker = server.addPlayer("highground_attacker");
        PlayerMock victim = server.addPlayer("highground_victim");
        World world = server.getWorld("world");

        ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
        weapon.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        attacker.getInventory().setItemInMainHand(weapon);

        HighgroundListener listener = new HighgroundListener();
        HighgroundEnchant config = Mockito.mock(HighgroundEnchant.class);
        when(config.getRequiredHeightAdvantage()).thenReturn(1.25);
        when(config.getBonusDamagePerLevel()).thenReturn(0.75);
        when(config.getMaxBonusDamage()).thenReturn(2.0);

        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamage()).thenReturn(5.0);

        AtomicReference<Double> appliedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(event).setDamage(anyDouble());

        attacker.teleport(new Location(world, 0.0, 66.0, 0.0));
        victim.teleport(new Location(world, 0.0, 64.0, 0.0));
        listener.onMeleeHit(event);
        assertEquals(7.0, appliedDamage.get(), 0.0001, "should add bonus damage from high ground");

        appliedDamage.set(null);
        attacker.teleport(new Location(world, 0.0, 64.5, 0.0));
        victim.teleport(new Location(world, 0.0, 64.0, 0.0));
        listener.onMeleeHit(event);
        assertNull(appliedDamage.get(), "should not trigger when height advantage is too small");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
