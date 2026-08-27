package net.enchadd;

import net.enchadd.enchants.FurrowEnchant;
import net.enchadd.listeners.FurrowListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
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

class FurrowHarvestBehaviorTest {

    private ServerMock server;
    private World world;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        TestPluginSupport.unregisterAllHandlers();
        MockBukkit.unmock();
    }

    @Test
    void furrowHarvestsMatureNeighborCropsInThreeByThree() {
        PlayerMock player = server.addPlayer("furrow_farmer");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));

        ItemStack hoe = new ItemStack(Material.DIAMOND_HOE);
        hoe.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(hoe);

        Block center = world.getBlockAt(0, 64, 0);
        fillMatureWheat(center);

        FurrowListener listener = new FurrowListener(Enchantment.SHARPNESS, null, furrowConfig());
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "furrow-harvest"));
        server.getPluginManager().callEvent(new BlockBreakEvent(center, player));

        int harvestedNeighbors = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                if (world.getBlockAt(dx, 64, dz).getType() == Material.AIR) {
                    harvestedNeighbors++;
                }
            }
        }
        assertEquals(8, harvestedNeighbors, "Furrow should clear all mature neighbor crops in a 3x3 area");
        assertEquals(Material.WHEAT, center.getType(), "The original block is handled by vanilla break flow, not by Furrow directly");
    }

    @Test
    void furrowRespectsSneakBypassForSingleCropHarvest() {
        PlayerMock player = server.addPlayer("furrow_precise");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));
        player.setSneaking(true);

        ItemStack hoe = new ItemStack(Material.DIAMOND_HOE);
        hoe.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(hoe);

        Block center = world.getBlockAt(0, 64, 0);
        fillMatureWheat(center);

        FurrowListener listener = new FurrowListener(Enchantment.SHARPNESS, null, furrowConfig());
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "furrow-sneak"));
        server.getPluginManager().callEvent(new BlockBreakEvent(center, player));

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block block = world.getBlockAt(dx, 64, dz);
                assertEquals(Material.WHEAT, block.getType(), "Sneaking should prevent Furrow from harvesting neighbor crops");
            }
        }
    }

    @Test
    void furrowReplantsNeighborCropsWhenCombinedWithReplanting() {
        PlayerMock player = server.addPlayer("furrow_replant");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));

        ItemStack hoe = new ItemStack(Material.DIAMOND_HOE);
        hoe.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        hoe.addUnsafeEnchantment(Enchantment.FORTUNE, 1);
        player.getInventory().setItemInMainHand(hoe);
        player.getInventory().addItem(new ItemStack(Material.WHEAT_SEEDS, 16));

        Block center = world.getBlockAt(0, 64, 0);
        fillMatureWheat(center);

        FurrowListener listener = new FurrowListener(Enchantment.SHARPNESS, Enchantment.FORTUNE, furrowConfig());
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "furrow-replant"));
        server.getPluginManager().callEvent(new BlockBreakEvent(center, player));

        int replantedNeighbors = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                Block block = world.getBlockAt(dx, 64, dz);
                if (block.getType() != Material.WHEAT) {
                    continue;
                }
                Ageable ageable = (Ageable) block.getBlockData();
                assertEquals(0, ageable.getAge(), "Replanted Furrow crops should reset to age 0");
                replantedNeighbors++;
            }
        }
        assertEquals(8, replantedNeighbors, "Replanting should restore all harvested Furrow neighbors when seeds are available");
        assertTrue(player.getInventory().contains(Material.WHEAT_SEEDS), "Replanting should not consume every spare seed stack");
    }

    private static FurrowEnchant furrowConfig() {
        FurrowEnchant config = Mockito.mock(FurrowEnchant.class);
        when(config.getRadius()).thenReturn(1);
        when(config.isBypassWhenSneaking()).thenReturn(true);
        when(config.isMatureOnly()).thenReturn(true);
        return config;
    }

    private static void fillMatureWheat(Block center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setMatureCrop(center.getRelative(dx, 0, dz), Material.WHEAT);
            }
        }
    }

    private static void setMatureCrop(Block block, Material material) {
        block.setType(material, false);
        Ageable data = (Ageable) material.createBlockData();
        data.setAge(data.getMaximumAge());
        block.setBlockData(data, false);
    }
}
