package net.enchadd;

import net.enchadd.enchants.WaysongEnchant;
import net.enchadd.listeners.WaysongListener;
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

class WaysongMarchBehaviorTest {

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
    void waysongBoostsSelfAndNearbyPlayers() {
        PlayerMock caller = preparePlayer("waysong_caller", 0.0, 0.0);
        PlayerMock nearby = preparePlayer("waysong_nearby", 4.0, 0.0);
        PlayerMock far = preparePlayer("waysong_far", 20.0, 0.0);

        caller.getInventory().setItemInMainHand(enchantedHorn());

        WaysongListener listener = new WaysongListener(
                Enchantment.SHARPNESS,
                waysongConfig(),
                PerformanceUtils.enchaddKey("waysong_test_cooldown")
        );
        listener.onWaysong(interactEvent(caller, caller.getInventory().getItemInMainHand()));

        assertTrue(caller.hasPotionEffect(PotionEffectType.SPEED),
                "Waysong should always help the horn user travel");
        assertTrue(nearby.hasPotionEffect(PotionEffectType.SPEED),
                "Waysong should also boost nearby players");
        assertFalse(far.hasPotionEffect(PotionEffectType.SPEED),
                "Waysong should not affect distant players outside the radius");
    }

    @Test
    void waysongCooldownBlocksImmediateSecondCall() {
        PlayerMock caller = preparePlayer("waysong_cooldown", 0.0, 0.0);
        caller.getInventory().setItemInMainHand(enchantedHorn());
        NamespacedKey cooldownKey = PerformanceUtils.enchaddKey("waysong_test_cooldown");

        WaysongListener listener = new WaysongListener(Enchantment.SHARPNESS, waysongConfig(), cooldownKey);
        PlayerInteractEvent event = interactEvent(caller, caller.getInventory().getItemInMainHand());

        listener.onWaysong(event);
        assertTrue(caller.hasPotionEffect(PotionEffectType.SPEED));

        caller.removePotionEffect(PotionEffectType.SPEED);
        listener.onWaysong(event);

        assertFalse(caller.hasPotionEffect(PotionEffectType.SPEED),
                "Waysong cooldown should block an immediate second march buff");
    }

    @Test
    void waysongIgnoresNonHornItems() {
        PlayerMock caller = preparePlayer("waysong_wrong_item", 0.0, 0.0);
        ItemStack wrongItem = new ItemStack(Material.CLOCK);
        wrongItem.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        caller.getInventory().setItemInMainHand(wrongItem);

        WaysongListener listener = new WaysongListener(
                Enchantment.SHARPNESS,
                waysongConfig(),
                PerformanceUtils.enchaddKey("waysong_test_cooldown")
        );
        listener.onWaysong(interactEvent(caller, wrongItem));

        assertFalse(caller.hasPotionEffect(PotionEffectType.SPEED),
                "Waysong should stay bound to goat horns only");
    }

    private PlayerMock preparePlayer(String name, double x, double z) {
        PlayerMock player = server.addPlayer(name);
        player.teleport(new Location(world, x, 70.0, z));
        return player;
    }

    private static ItemStack enchantedHorn() {
        ItemStack horn = new ItemStack(Material.GOAT_HORN);
        horn.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        return horn;
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

    private static WaysongEnchant waysongConfig() {
        WaysongEnchant config = Mockito.mock(WaysongEnchant.class);
        when(config.getCooldownTicks()).thenReturn(900);
        when(config.getRadius()).thenReturn(8.0);
        when(config.getSpeedSeconds()).thenReturn(6);
        when(config.getSpeedAmplifier()).thenReturn(0);
        return config;
    }
}
