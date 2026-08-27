package net.enchadd;

import net.enchadd.enchants.StonewakeEnchant;
import net.enchadd.listeners.StonewakeListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class StonewakeHasteBehaviorTest {

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
    void stonewakeGrantsBriefHasteWhenBreakingOre() {
        PlayerMock player = server.addPlayer("stonewake_miner");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));

        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        pickaxe.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(pickaxe);

        Block ore = world.getBlockAt(0, 64, 0);
        ore.setType(Material.DIAMOND_ORE, false);

        StonewakeListener listener = new StonewakeListener(Enchantment.SHARPNESS, stonewakeConfig());
        listener.onBlockBreak(new BlockBreakEvent(ore, player));

        assertTrue(player.hasPotionEffect(PotionEffectType.HASTE),
                "Breaking ore with Stonewake should grant a haste burst");
        PotionEffect haste = player.getActivePotionEffects().stream()
                .filter(effect -> effect.getType().equals(PotionEffectType.HASTE))
                .findFirst()
                .orElseThrow();
        assertEquals(20, haste.getDuration(), "Stonewake should use the configured duration");
    }

    @Test
    void stonewakeIgnoresNonOreBlocks() {
        PlayerMock player = server.addPlayer("stonewake_builder");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));

        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        pickaxe.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(pickaxe);

        Block stone = world.getBlockAt(0, 64, 0);
        stone.setType(Material.STONE, false);

        StonewakeListener listener = new StonewakeListener(Enchantment.SHARPNESS, stonewakeConfig());
        listener.onBlockBreak(new BlockBreakEvent(stone, player));

        assertTrue(player.getActivePotionEffects().isEmpty(),
                "Stonewake should not trigger on non-ore blocks");
    }

    @Test
    void stonewakeDoesNotOverwriteStrongerExistingHaste() {
        PlayerMock player = server.addPlayer("stonewake_beaconed");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));

        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        pickaxe.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(pickaxe);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 200, 1, false, false, true));

        Block ore = world.getBlockAt(0, 64, 0);
        ore.setType(Material.DIAMOND_ORE, false);

        StonewakeListener listener = new StonewakeListener(Enchantment.SHARPNESS, stonewakeConfig());
        listener.onBlockBreak(new BlockBreakEvent(ore, player));

        PotionEffect haste = player.getActivePotionEffects().stream()
                .filter(effect -> effect.getType().equals(PotionEffectType.HASTE))
                .findFirst()
                .orElseThrow();
        assertEquals(1, haste.getAmplifier(), "Stonewake should not downgrade a stronger existing haste effect");
        assertEquals(200, haste.getDuration(), "Stonewake should leave the longer existing haste untouched");
    }

    private static StonewakeEnchant stonewakeConfig() {
        StonewakeEnchant config = Mockito.mock(StonewakeEnchant.class);
        when(config.getDurationSecondsPerLevel()).thenReturn(1);
        when(config.getHasteAmplifier()).thenReturn(0);
        return config;
    }
}
