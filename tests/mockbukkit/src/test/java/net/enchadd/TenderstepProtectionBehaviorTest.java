package net.enchadd;

import net.enchadd.enchants.TenderstepEnchant;
import net.enchadd.listeners.TenderstepListener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenderstepProtectionBehaviorTest {

    @Test
    void tenderstepCancelsFarmlandTrample() {
        ServerMock server = MockBukkit.mock();
        try {
            PlayerMock player = server.addPlayer("tenderstep_farmer");
            ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
            boots.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
            player.getInventory().setBoots(boots);

            Block farmland = server.addSimpleWorld("world").getBlockAt(0, 64, 0);
            farmland.setType(Material.FARMLAND, false);

            EntityChangeBlockEvent event = Mockito.mock(EntityChangeBlockEvent.class);
            when(event.getEntity()).thenReturn(player);
            when(event.getBlock()).thenReturn(farmland);

            TenderstepListener listener = new TenderstepListener(Enchantment.SHARPNESS, tenderstepConfig());
            listener.onFragileBlockStep(event);

            verify(event).setCancelled(true);
        } finally {
            MockBukkit.unmock();
        }
    }

    @Test
    void tenderstepCanProtectTurtleEggsToo() {
        ServerMock server = MockBukkit.mock();
        try {
            PlayerMock player = server.addPlayer("tenderstep_beachwalker");
            ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
            boots.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
            player.getInventory().setBoots(boots);

            Block egg = server.addSimpleWorld("world").getBlockAt(0, 64, 0);
            egg.setType(Material.TURTLE_EGG, false);

            EntityChangeBlockEvent event = Mockito.mock(EntityChangeBlockEvent.class);
            when(event.getEntity()).thenReturn(player);
            when(event.getBlock()).thenReturn(egg);

            TenderstepListener listener = new TenderstepListener(Enchantment.SHARPNESS, tenderstepConfig());
            listener.onFragileBlockStep(event);

            verify(event).setCancelled(true);
        } finally {
            MockBukkit.unmock();
        }
    }

    @Test
    void tenderstepDoesNothingWithoutTheEnchantment() {
        ServerMock server = MockBukkit.mock();
        try {
            PlayerMock player = server.addPlayer("plain_boots");
            player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

            Block farmland = server.addSimpleWorld("world").getBlockAt(0, 64, 0);
            farmland.setType(Material.FARMLAND, false);

            EntityChangeBlockEvent event = Mockito.mock(EntityChangeBlockEvent.class);
            when(event.getEntity()).thenReturn(player);
            when(event.getBlock()).thenReturn(farmland);

            TenderstepListener listener = new TenderstepListener(Enchantment.SHARPNESS, tenderstepConfig());
            listener.onFragileBlockStep(event);

            verify(event, never()).setCancelled(true);
        } finally {
            MockBukkit.unmock();
        }
    }

    private static TenderstepEnchant tenderstepConfig() {
        TenderstepEnchant config = Mockito.mock(TenderstepEnchant.class);
        when(config.isProtectTurtleEggs()).thenReturn(true);
        return config;
    }
}
