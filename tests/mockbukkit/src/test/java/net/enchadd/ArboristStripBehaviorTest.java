package net.enchadd;

import net.enchadd.enchants.ArboristEnchant;
import net.enchadd.listeners.ArboristListener;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
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

class ArboristStripBehaviorTest {

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
    void arboristStripsTwoLogsAboveClickedBlockAndPreservesAxis() {
        PlayerMock player = server.addPlayer("arborist_builder");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));

        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        axe.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(axe);

        Block center = world.getBlockAt(0, 64, 0);
        Block first = world.getBlockAt(0, 65, 0);
        Block second = world.getBlockAt(0, 66, 0);
        Block third = world.getBlockAt(0, 67, 0);
        center.setType(Material.OAK_LOG, false);
        first.setType(Material.OAK_LOG, false);
        third.setType(Material.OAK_LOG, false);

        Orientable secondData = (Orientable) Material.OAK_LOG.createBlockData();
        secondData.setAxis(Axis.X);
        second.setBlockData(secondData, false);

        ArboristListener listener = new ArboristListener(Enchantment.SHARPNESS, arboristConfig());
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "arborist-strip"));
        server.getPluginManager().callEvent(new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_BLOCK,
                axe,
                center,
                BlockFace.NORTH,
                EquipmentSlot.HAND
        ));

        assertEquals(Material.STRIPPED_OAK_LOG, first.getType(),
                "Arborist should strip the first matching log above the clicked trunk");
        assertEquals(Material.STRIPPED_OAK_LOG, second.getType(),
                "Arborist should continue stripping upward until it reaches the configured limit");
        assertEquals(Axis.X, ((Orientable) second.getBlockData()).getAxis(),
                "Arborist should preserve the axis of stripped logs");
        assertEquals(Material.OAK_LOG, third.getType(),
                "Arborist should stop after the configured number of extra blocks");
        assertEquals(Material.OAK_LOG, center.getType(),
                "The clicked block is still left to vanilla axe stripping");
    }

    @Test
    void arboristRespectsSneakBypass() {
        PlayerMock player = server.addPlayer("arborist_precise");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));
        player.setSneaking(true);

        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        axe.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(axe);

        Block center = world.getBlockAt(0, 64, 0);
        Block first = world.getBlockAt(0, 65, 0);
        Block second = world.getBlockAt(0, 66, 0);
        center.setType(Material.OAK_LOG, false);
        first.setType(Material.OAK_LOG, false);
        second.setType(Material.OAK_LOG, false);

        ArboristListener listener = new ArboristListener(Enchantment.SHARPNESS, arboristConfig());
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "arborist-sneak"));
        server.getPluginManager().callEvent(new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_BLOCK,
                axe,
                center,
                BlockFace.NORTH,
                EquipmentSlot.HAND
        ));

        assertEquals(Material.OAK_LOG, first.getType(), "Sneaking should disable Arborist spread");
        assertEquals(Material.OAK_LOG, second.getType(), "Sneaking should keep the axe action single-target");
    }

    @Test
    void arboristSkipsSyntheticTargetsCancelledByProtectionListeners() {
        PlayerMock player = server.addPlayer("arborist_protected");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));

        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        axe.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(axe);

        Block center = world.getBlockAt(0, 64, 0);
        Block first = world.getBlockAt(0, 65, 0);
        Block second = world.getBlockAt(0, 66, 0);
        center.setType(Material.OAK_LOG, false);
        first.setType(Material.OAK_LOG, false);
        second.setType(Material.OAK_LOG, false);

        server.getPluginManager().registerEvents(new SyntheticProtectListener(second), TestPluginSupport.enabledPlugin(server, "arborist-protect"));

        ArboristListener listener = new ArboristListener(Enchantment.SHARPNESS, arboristConfig());
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "arborist-listener"));
        server.getPluginManager().callEvent(new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_BLOCK,
                axe,
                center,
                BlockFace.NORTH,
                EquipmentSlot.HAND
        ));

        assertEquals(Material.STRIPPED_OAK_LOG, first.getType(),
                "Unprotected trunk segments should still be stripped");
        assertEquals(Material.OAK_LOG, second.getType(),
                "Cancelled synthetic interact targets should remain unchanged");
    }

    private static ArboristEnchant arboristConfig() {
        ArboristEnchant config = Mockito.mock(ArboristEnchant.class);
        when(config.getExtraBlocks()).thenReturn(2);
        when(config.isBypassWhenSneaking()).thenReturn(true);
        return config;
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
