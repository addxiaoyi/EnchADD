package net.enchadd;

import net.enchadd.enchants.TideshellEnchant;
import net.enchadd.listeners.TideshellListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class TideshellDiveBehaviorTest {

    private ServerMock server;
    private World world;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void tideshellGrantsDiveBuffsWhenTouchingWater() {
        PlayerMock player = preparePlayer("tideshell_swimmer");
        floodPlayerSpace(player);

        TideshellListener listener = new TideshellListener(
                Enchantment.SHARPNESS,
                tideshellConfig(),
                PerformanceUtils.enchaddKey("tideshell_test_cooldown")
        );
        listener.onTideshell(interactEvent(player, player.getInventory().getItemInMainHand()));

        assertTrue(player.hasPotionEffect(PotionEffectType.WATER_BREATHING),
                "Tideshell should grant water breathing when the shell is used in water");
        assertTrue(player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE),
                "Tideshell should also grant dolphins grace for a short underwater burst");
    }

    @Test
    void tideshellDoesNotTriggerOnDryLand() {
        PlayerMock player = preparePlayer("tideshell_dry");

        TideshellListener listener = new TideshellListener(
                Enchantment.SHARPNESS,
                tideshellConfig(),
                PerformanceUtils.enchaddKey("tideshell_test_cooldown")
        );
        listener.onTideshell(interactEvent(player, player.getInventory().getItemInMainHand()));

        assertFalse(player.hasPotionEffect(PotionEffectType.WATER_BREATHING),
                "Tideshell should stay inactive away from water");
        assertFalse(player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE),
                "Tideshell should not grant dive buffs on dry land");
    }

    @Test
    void tideshellRespectsCooldown() {
        PlayerMock player = preparePlayer("tideshell_cooldown");
        floodPlayerSpace(player);
        NamespacedKey cooldownKey = PerformanceUtils.enchaddKey("tideshell_test_cooldown");

        TideshellListener listener = new TideshellListener(Enchantment.SHARPNESS, tideshellConfig(), cooldownKey);
        PlayerInteractEvent event = interactEvent(player, player.getInventory().getItemInMainHand());

        listener.onTideshell(event);
        assertTrue(player.hasPotionEffect(PotionEffectType.WATER_BREATHING));
        assertTrue(player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE));

        player.removePotionEffect(PotionEffectType.WATER_BREATHING);
        player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);

        listener.onTideshell(event);

        assertFalse(player.hasPotionEffect(PotionEffectType.WATER_BREATHING),
                "Tideshell cooldown should block immediate second activation");
        assertFalse(player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE),
                "Tideshell cooldown should block immediate second activation");
    }

    private PlayerMock preparePlayer(String name) {
        PlayerMock player = server.addPlayer(name);
        ItemStack shell = new ItemStack(Material.NAUTILUS_SHELL);
        shell.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(shell);
        player.teleport(new Location(world, 0.0, 64.0, 0.0));
        return player;
    }

    private void floodPlayerSpace(PlayerMock player) {
        player.getLocation().getBlock().setType(Material.WATER, false);
        player.getEyeLocation().getBlock().setType(Material.WATER, false);
    }

    private static PlayerInteractEvent interactEvent(PlayerMock player, ItemStack item) {
        PlayerInteractEvent event = Mockito.mock(PlayerInteractEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
        when(event.getHand()).thenReturn(org.bukkit.inventory.EquipmentSlot.HAND);
        when(event.useInteractedBlock()).thenReturn(Event.Result.DEFAULT);
        when(event.useItemInHand()).thenReturn(Event.Result.DEFAULT);
        when(event.getItem()).thenReturn(item);
        return event;
    }

    private static TideshellEnchant tideshellConfig() {
        TideshellEnchant config = Mockito.mock(TideshellEnchant.class);
        when(config.getCooldownTicks()).thenReturn(700);
        when(config.getWaterBreathingSeconds()).thenReturn(8);
        when(config.getDolphinsGraceSeconds()).thenReturn(5);
        return config;
    }
}
