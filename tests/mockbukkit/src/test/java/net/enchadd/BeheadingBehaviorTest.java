package net.enchadd;

import net.enchadd.enchants.BeheadingEnchant;
import net.enchadd.events.EntityBeheadEvent;
import net.enchadd.listeners.BeheadingListener;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BeheadingBehaviorTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        TestPluginSupport.unregisterAllHandlers();
        MockBukkit.unmock();
    }

    @Test
    void beheadingDropsMobHeadWhenRollSucceeds() throws Exception {
        PlayerMock killer = server.addPlayer("beheading_killer");
        killer.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_AXE));

        var zombie = (org.bukkit.entity.LivingEntity) server.getWorld("world").spawnEntity(killer.getLocation(), EntityType.ZOMBIE);
        List<ItemStack> drops = new ArrayList<>();

        BeheadingListener listener = new BeheadingListener();
        setField(listener, "beheading", org.bukkit.enchantments.Enchantment.SHARPNESS);
        setField(listener, "config", beheadingConfig());

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        var damageSource = TestDamageSources.directPlayerDamage(killer);
        when(event.getDamageSource()).thenReturn(damageSource);
        when(event.getEntity()).thenReturn(zombie);
        when(event.getDrops()).thenReturn(drops);

        killer.getInventory().getItemInMainHand().addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 1);

        try (MockedStatic<net.enchadd.utils.PerformanceUtils> utils =
                     Mockito.mockStatic(net.enchadd.utils.PerformanceUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<org.bukkit.Bukkit> bukkit = Mockito.mockStatic(org.bukkit.Bukkit.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> net.enchadd.utils.PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);

            listener.onBeheading(event);
        }

        assertEquals(1, drops.size());
        assertEquals(Material.ZOMBIE_HEAD, drops.getFirst().getType());
    }

    @Test
    void beheadingSkipsDuplicateHeadDrops() throws Exception {
        PlayerMock killer = server.addPlayer("beheading_duplicate");
        killer.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_AXE));
        killer.getInventory().getItemInMainHand().addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 1);

        var skeleton = (org.bukkit.entity.LivingEntity) server.getWorld("world").spawnEntity(killer.getLocation(), EntityType.SKELETON);
        List<ItemStack> drops = new ArrayList<>(List.of(new ItemStack(Material.SKELETON_SKULL)));

        BeheadingListener listener = new BeheadingListener();
        setField(listener, "beheading", org.bukkit.enchantments.Enchantment.SHARPNESS);
        setField(listener, "config", beheadingConfig());

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        var damageSource = TestDamageSources.directPlayerDamage(killer);
        when(event.getDamageSource()).thenReturn(damageSource);
        when(event.getEntity()).thenReturn(skeleton);
        when(event.getDrops()).thenReturn(drops);

        try (MockedStatic<net.enchadd.utils.PerformanceUtils> utils =
                     Mockito.mockStatic(net.enchadd.utils.PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> net.enchadd.utils.PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            listener.onBeheading(event);
        }

        assertEquals(1, drops.size(), "Existing skull drop should prevent duplicate beheading drops");
        assertEquals(Material.SKELETON_SKULL, drops.getFirst().getType());
    }

    private static BeheadingEnchant beheadingConfig() {
        BeheadingEnchant config = Mockito.mock(BeheadingEnchant.class);
        when(config.getChanceToDropHeadPerLevel()).thenReturn(1.0d);
        return config;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
