package net.enchadd;

import net.enchadd.enchants.IrrigationEnchant;
import net.enchadd.listeners.IrrigationListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Farmland;
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

class IrrigationHydrationBehaviorTest {

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
    void irrigationHydratesDryFarmlandAcrossThreeByThreeArea() {
        PlayerMock player = server.addPlayer("irrigation_farmer");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));

        ItemStack hoe = new ItemStack(Material.DIAMOND_HOE);
        hoe.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(hoe);

        Block center = world.getBlockAt(0, 64, 0);
        fillDryFarmland(center);

        IrrigationListener listener = new IrrigationListener(Enchantment.SHARPNESS, irrigationConfig());
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "irrigation-hydrate"));
        server.getPluginManager().callEvent(new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_BLOCK,
                hoe,
                center,
                BlockFace.UP,
                EquipmentSlot.HAND
        ));

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                assertEquals(7, moistureOf(world.getBlockAt(dx, 64, dz)),
                        "Irrigation should hydrate all nearby dry farmland");
            }
        }
    }

    @Test
    void irrigationRespectsSneakBypassAndKeepsNeighborsDry() {
        PlayerMock player = server.addPlayer("irrigation_precise");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));
        player.setSneaking(true);

        ItemStack hoe = new ItemStack(Material.DIAMOND_HOE);
        hoe.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(hoe);

        Block center = world.getBlockAt(0, 64, 0);
        fillDryFarmland(center);

        IrrigationListener listener = new IrrigationListener(Enchantment.SHARPNESS, irrigationConfig());
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "irrigation-sneak"));
        server.getPluginManager().callEvent(new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_BLOCK,
                hoe,
                center,
                BlockFace.UP,
                EquipmentSlot.HAND
        ));

        assertEquals(7, moistureOf(center), "Sneaking should still allow hydrating the targeted farmland");
        assertEquals(0, moistureOf(world.getBlockAt(1, 64, 0)), "Sneaking should disable the extra spread");
    }

    @Test
    void irrigationSkipsSyntheticTargetsCancelledByProtectionListeners() {
        PlayerMock player = server.addPlayer("irrigation_protected");
        player.teleport(new Location(world, 0.0, 64.0, 0.0));

        ItemStack hoe = new ItemStack(Material.DIAMOND_HOE);
        hoe.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(hoe);

        Block center = world.getBlockAt(0, 64, 0);
        fillDryFarmland(center);

        Block protectedNeighbor = world.getBlockAt(1, 64, 0);
        server.getPluginManager().registerEvents(new SyntheticProtectListener(protectedNeighbor), TestPluginSupport.enabledPlugin(server, "irrigation-protect"));

        IrrigationListener listener = new IrrigationListener(Enchantment.SHARPNESS, irrigationConfig());
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "irrigation-listener"));
        server.getPluginManager().callEvent(new PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_BLOCK,
                hoe,
                center,
                BlockFace.UP,
                EquipmentSlot.HAND
        ));

        assertEquals(0, moistureOf(protectedNeighbor), "Cancelled synthetic targets should remain dry");
        assertEquals(7, moistureOf(world.getBlockAt(-1, 64, 0)), "Unprotected farmland should still be hydrated");
    }

    private static IrrigationEnchant irrigationConfig() {
        IrrigationEnchant config = Mockito.mock(IrrigationEnchant.class);
        when(config.getRadius()).thenReturn(1);
        when(config.isBypassWhenSneaking()).thenReturn(true);
        return config;
    }

    private static void fillDryFarmland(Block center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block block = center.getRelative(dx, 0, dz);
                block.setType(Material.FARMLAND, false);
                Farmland farmland = (Farmland) block.getBlockData();
                farmland.setMoisture(0);
                block.setBlockData(farmland, false);
            }
        }
    }

    private static int moistureOf(Block block) {
        return ((Farmland) block.getBlockData()).getMoisture();
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
