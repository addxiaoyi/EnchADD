package net.enchadd;

import net.enchadd.enchants.DelvesenseEnchant;
import net.enchadd.listeners.DelvesenseListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
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

class DelvesenseCavePingBehaviorTest {

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
    void delvesenseHighlightsNearbyMonstersUnderground() {
        PlayerMock player = preparePlayer("delvesense_caller", 30.0);
        Monster monster = spawnMonster(3.0, 30.0);

        DelvesenseListener listener = new DelvesenseListener(
                Enchantment.SHARPNESS,
                delvesenseConfig(),
                PerformanceUtils.enchaddKey("delvesense_test_cooldown")
        );
        listener.onDelvesense(interactEvent(player, player.getInventory().getItemInMainHand()));

        assertTrue(player.hasPotionEffect(PotionEffectType.NIGHT_VISION),
                "Delvesense should grant night vision while exploring underground");
        assertTrue(monster.hasPotionEffect(PotionEffectType.GLOWING),
                "Delvesense should highlight nearby hostile mobs in caves");
    }

    @Test
    void delvesenseDoesNotTriggerAboveActivationY() {
        PlayerMock player = preparePlayer("delvesense_surface", 70.0);
        Monster monster = spawnMonster(3.0, 70.0);

        DelvesenseListener listener = new DelvesenseListener(
                Enchantment.SHARPNESS,
                delvesenseConfig(),
                PerformanceUtils.enchaddKey("delvesense_test_cooldown")
        );
        listener.onDelvesense(interactEvent(player, player.getInventory().getItemInMainHand()));

        assertFalse(player.hasPotionEffect(PotionEffectType.NIGHT_VISION),
                "Delvesense should stay inactive above the configured cave depth");
        assertFalse(monster.hasPotionEffect(PotionEffectType.GLOWING),
                "Delvesense should not reveal mobs when used near the surface");
    }

    @Test
    void delvesenseRespectsCooldown() {
        PlayerMock player = preparePlayer("delvesense_cooldown", 28.0);
        Monster monster = spawnMonster(2.0, 28.0);
        NamespacedKey cooldownKey = PerformanceUtils.enchaddKey("delvesense_test_cooldown");

        DelvesenseListener listener = new DelvesenseListener(Enchantment.SHARPNESS, delvesenseConfig(), cooldownKey);
        PlayerInteractEvent event = interactEvent(player, player.getInventory().getItemInMainHand());

        listener.onDelvesense(event);
        assertTrue(player.hasPotionEffect(PotionEffectType.NIGHT_VISION));
        assertTrue(monster.hasPotionEffect(PotionEffectType.GLOWING));

        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        monster.removePotionEffect(PotionEffectType.GLOWING);

        listener.onDelvesense(event);

        assertFalse(player.hasPotionEffect(PotionEffectType.NIGHT_VISION),
                "Delvesense cooldown should block immediate second activation");
        assertFalse(monster.hasPotionEffect(PotionEffectType.GLOWING),
                "Delvesense cooldown should block immediate second activation");
    }

    private PlayerMock preparePlayer(String name, double y) {
        PlayerMock player = server.addPlayer(name);
        ItemStack compass = new ItemStack(Material.COMPASS);
        compass.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(compass);
        player.teleport(new Location(world, 0.0, y, 0.0));
        return player;
    }

    private Monster spawnMonster(double x, double y) {
        return (Monster) world.spawnEntity(new Location(world, x, y, 0.0), EntityType.ZOMBIE);
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

    private static DelvesenseEnchant delvesenseConfig() {
        DelvesenseEnchant config = Mockito.mock(DelvesenseEnchant.class);
        when(config.getCooldownTicks()).thenReturn(600);
        when(config.getRadius()).thenReturn(10.0);
        when(config.getGlowSeconds()).thenReturn(6);
        when(config.getNightVisionSeconds()).thenReturn(4);
        when(config.getMaxActivationY()).thenReturn(56.0);
        return config;
    }
}
