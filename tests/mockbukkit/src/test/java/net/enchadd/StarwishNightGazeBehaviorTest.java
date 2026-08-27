package net.enchadd;

import net.enchadd.enchants.StarwishEnchant;
import net.enchadd.listeners.StarwishListener;
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

class StarwishNightGazeBehaviorTest {

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
    void starwishGrantsNightVisionAndLuckWhenGazingAtNightSky() {
        PlayerMock player = preparePlayer(-80.0f, 18000L);

        StarwishListener listener = new StarwishListener(
                Enchantment.SHARPNESS,
                starwishConfig(),
                PerformanceUtils.enchaddKey("starwish_test_cooldown")
        );
        listener.onStarwish(interactEvent(player));

        assertTrue(player.hasPotionEffect(PotionEffectType.NIGHT_VISION),
                "Starwish should grant night vision while sky-gazing at night");
        assertTrue(player.hasPotionEffect(PotionEffectType.LUCK),
                "Starwish should also grant a short luck buff");
    }

    @Test
    void starwishDoesNotTriggerDuringDaytime() {
        PlayerMock player = preparePlayer(-80.0f, 6000L);

        StarwishListener listener = new StarwishListener(
                Enchantment.SHARPNESS,
                starwishConfig(),
                PerformanceUtils.enchaddKey("starwish_test_cooldown")
        );
        listener.onStarwish(interactEvent(player));

        assertFalse(player.hasPotionEffect(PotionEffectType.NIGHT_VISION),
                "Starwish should stay inactive during daytime");
        assertFalse(player.hasPotionEffect(PotionEffectType.LUCK),
                "Starwish should not grant buffs during daytime");
    }

    @Test
    void starwishRespectsCooldownAfterFirstWish() {
        PlayerMock player = preparePlayer(-80.0f, 18000L);
        NamespacedKey cooldownKey = PerformanceUtils.enchaddKey("starwish_test_cooldown");

        StarwishListener listener = new StarwishListener(Enchantment.SHARPNESS, starwishConfig(), cooldownKey);
        PlayerInteractEvent event = interactEvent(player);

        listener.onStarwish(event);
        assertTrue(player.hasPotionEffect(PotionEffectType.NIGHT_VISION));
        assertTrue(player.hasPotionEffect(PotionEffectType.LUCK));

        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.LUCK);

        listener.onStarwish(event);
        assertFalse(player.hasPotionEffect(PotionEffectType.NIGHT_VISION),
                "Starwish cooldown should block immediate re-triggering");
        assertFalse(player.hasPotionEffect(PotionEffectType.LUCK),
                "Starwish cooldown should block immediate re-triggering");
    }

    private PlayerMock preparePlayer(float pitch, long worldTime) {
        PlayerMock player = server.addPlayer("starwish_viewer_" + worldTime + "_" + (int) pitch);
        ItemStack spyglass = new ItemStack(Material.SPYGLASS);
        spyglass.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(spyglass);
        world.setTime(worldTime);
        player.teleport(new Location(world, 0.0, 80.0, 0.0, 0.0f, pitch));
        return player;
    }

    private static PlayerInteractEvent interactEvent(PlayerMock player) {
        PlayerInteractEvent event = Mockito.mock(PlayerInteractEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
        when(event.getHand()).thenReturn(org.bukkit.inventory.EquipmentSlot.HAND);
        when(event.useInteractedBlock()).thenReturn(Event.Result.DEFAULT);
        when(event.useItemInHand()).thenReturn(Event.Result.DEFAULT);
        when(event.getItem()).thenReturn(player.getInventory().getItemInMainHand());
        return event;
    }

    private static StarwishEnchant starwishConfig() {
        StarwishEnchant config = Mockito.mock(StarwishEnchant.class);
        when(config.getCooldownTicks()).thenReturn(1200);
        when(config.getNightVisionSeconds()).thenReturn(10);
        when(config.getLuckSeconds()).thenReturn(6);
        when(config.getLookUpPitchThreshold()).thenReturn(-60.0);
        return config;
    }
}
