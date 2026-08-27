package net.enchadd;

import net.enchadd.enchants.TideRunnerEnchant;
import net.enchadd.listeners.TideRunnerListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class TideRunnerBehaviorTest {

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
    void tideRunnerAppliesDolphinsGraceWhenSwimmingInWater() {
        PlayerMock player = preparePlayer("tiderunner_swim");
        player.setSwimming(true);
        floodEyeSpace(player);

        NamespacedKey cooldownKey = PerformanceUtils.enchaddKey("tide_runner_test_cooldown");
        TideRunnerListener listener = new TideRunnerListener(Enchantment.SHARPNESS, config(), cooldownKey);
        listener.onMove(moveEvent(player, movedFrom(player), movedTo(player)));

        assertTrue(player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE),
                "tide runner should grant dolphins grace while swimming through water");
        assertNotNull(player.getPersistentDataContainer().get(cooldownKey, PersistentDataType.LONG),
                "successful tide runner trigger should write cooldown state");
    }

    @Test
    void tideRunnerStaysInactiveOutOfWater() {
        PlayerMock player = preparePlayer("tiderunner_dry");
        player.setSwimming(true);

        TideRunnerListener listener = new TideRunnerListener(
                Enchantment.SHARPNESS,
                config(),
                PerformanceUtils.enchaddKey("tide_runner_test_cooldown")
        );
        listener.onMove(moveEvent(player, movedFrom(player), movedTo(player)));

        assertFalse(player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE),
                "tide runner should not trigger when the swimmer is not touching water");
    }

    @Test
    void tideRunnerRespectsCooldown() {
        PlayerMock player = preparePlayer("tiderunner_cooldown");
        player.setSwimming(true);
        floodEyeSpace(player);
        NamespacedKey cooldownKey = PerformanceUtils.enchaddKey("tide_runner_test_cooldown");

        TideRunnerListener listener = new TideRunnerListener(Enchantment.SHARPNESS, config(), cooldownKey);
        PlayerMoveEvent event = moveEvent(player, movedFrom(player), movedTo(player));

        listener.onMove(event);
        assertTrue(player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE));

        player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
        listener.onMove(event);

        assertFalse(player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE),
                "active cooldown should prevent immediate tide runner retrigger");
    }

    private PlayerMock preparePlayer(String name) {
        PlayerMock player = server.addPlayer(name);
        player.teleport(new Location(world, 0.0, 64.0, 0.0));

        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        boots.addUnsafeEnchantment(Enchantment.SHARPNESS, 2);
        player.getInventory().setBoots(boots);
        return player;
    }

    private static TideRunnerEnchant config() {
        TideRunnerEnchant config = Mockito.mock(TideRunnerEnchant.class);
        when(config.getCooldownTicks()).thenReturn(200);
        when(config.getGraceTicksPerLevel()).thenReturn(60);
        when(config.getSpeedAmplifierPerLevel()).thenReturn(0.3);
        return config;
    }

    private static void floodEyeSpace(PlayerMock player) {
        player.getEyeLocation().getBlock().setType(Material.WATER, false);
        player.getEyeLocation().getBlock().getRelative(BlockFace.UP).setType(Material.WATER, false);
    }

    private static Location movedFrom(PlayerMock player) {
        return player.getLocation().clone();
    }

    private static Location movedTo(PlayerMock player) {
        return player.getLocation().clone().add(0.4, 0.0, 0.2);
    }

    private static PlayerMoveEvent moveEvent(PlayerMock player, Location from, Location to) {
        PlayerMoveEvent event = Mockito.mock(PlayerMoveEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.hasChangedPosition()).thenReturn(true);
        when(event.getFrom()).thenReturn(from);
        when(event.getTo()).thenReturn(to);
        return event;
    }
}
