package net.enchadd;

import net.enchadd.enchants.TrailblazerEnchant;
import net.enchadd.listeners.TrailblazerListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class TrailblazerPathBehaviorTest {

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
    void trailblazerSpreadsPathsAcrossThreeByThreeArea() {
        PlayerMock player = server.addPlayer("trailblazer_builder");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));

        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
        shovel.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(shovel);

        Block center = world.getBlockAt(0, 64, 0);
        fillFlattenableGround(center, Material.GRASS_BLOCK);

        TrailblazerListener listener = new TrailblazerListener(Enchantment.SHARPNESS, trailblazerConfig());
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "trailblazer-path"));
        server.getPluginManager().callEvent(new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_BLOCK,
                shovel,
                center,
                BlockFace.UP,
                EquipmentSlot.HAND
        ));

        int converted = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                if (world.getBlockAt(dx, 64, dz).getType() == Material.DIRT_PATH) {
                    converted++;
                }
            }
        }
        assertEquals(8, converted, "Trailblazer should spread a shovel path to all valid neighboring blocks");
        assertEquals(Material.GRASS_BLOCK, center.getType(), "The center block is still left to vanilla shovel handling");
    }

    @Test
    void trailblazerRespectsSneakBypass() {
        PlayerMock player = server.addPlayer("trailblazer_precise");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));
        player.setSneaking(true);

        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
        shovel.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(shovel);

        Block center = world.getBlockAt(0, 64, 0);
        fillFlattenableGround(center, Material.GRASS_BLOCK);

        TrailblazerListener listener = new TrailblazerListener(Enchantment.SHARPNESS, trailblazerConfig());
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "trailblazer-sneak"));
        server.getPluginManager().callEvent(new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_BLOCK,
                shovel,
                center,
                BlockFace.UP,
                EquipmentSlot.HAND
        ));

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                assertEquals(Material.GRASS_BLOCK, world.getBlockAt(dx, 64, dz).getType(),
                        "Sneaking should disable Trailblazer spread");
            }
        }
    }

    @Test
    void trailblazerSkipsSyntheticTargetsCancelledByProtectionListeners() {
        PlayerMock player = server.addPlayer("trailblazer_protected");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));

        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
        shovel.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(shovel);

        Block center = world.getBlockAt(0, 64, 0);
        fillFlattenableGround(center, Material.GRASS_BLOCK);

        Block protectedNeighbor = world.getBlockAt(1, 64, 0);
        server.getPluginManager().registerEvents(new SyntheticProtectListener(protectedNeighbor), TestPluginSupport.enabledPlugin(server, "trailblazer-protect"));

        TrailblazerListener listener = new TrailblazerListener(Enchantment.SHARPNESS, trailblazerConfig());
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "trailblazer-listener"));
        server.getPluginManager().callEvent(new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_BLOCK,
                shovel,
                center,
                BlockFace.UP,
                EquipmentSlot.HAND
        ));

        assertEquals(Material.GRASS_BLOCK, protectedNeighbor.getType(),
                "Cancelled synthetic interact targets should remain unchanged");
        assertEquals(Material.DIRT_PATH, world.getBlockAt(-1, 64, 0).getType(),
                "Unprotected neighbors should still be converted");
    }

    private static TrailblazerEnchant trailblazerConfig() {
        TrailblazerEnchant config = Mockito.mock(TrailblazerEnchant.class);
        when(config.getRadius()).thenReturn(1);
        when(config.isBypassWhenSneaking()).thenReturn(true);
        return config;
    }

    private static void fillFlattenableGround(Block center, Material material) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                center.getRelative(dx, 0, dz).setType(material, false);
            }
        }
    }

    private static final class SyntheticProtectListener implements Listener {
        private final Block protectedBlock;

        private SyntheticProtectListener(Block protectedBlock) {
            this.protectedBlock = protectedBlock;
        }

        @EventHandler(priority = EventPriority.LOWEST)
        void onRightClick(PlayerInteractEvent event) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            if (event.getClickedBlock() == null) {
                return;
            }
            if (!event.getClickedBlock().getLocation().equals(protectedBlock.getLocation())) {
                return;
            }
            event.setCancelled(true);
        }
    }
}
