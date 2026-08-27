package net.enchadd;

import net.enchadd.enchants.FreshcatchEnchant;
import net.enchadd.listeners.FreshcatchListener;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class FreshcatchRestoreBehaviorTest {

    @Test
    void freshcatchRestoresFoodWhenCatchingEdibleFish() {
        ServerMock server = MockBukkit.mock();
        try {
            PlayerMock player = server.addPlayer("freshcatch_angler");
            ItemStack rod = new ItemStack(Material.FISHING_ROD);
            rod.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
            player.getInventory().setItemInMainHand(rod);
            player.setFoodLevel(14);
            player.setSaturation(2.0f);

            PlayerFishEvent event = edibleCatchEvent(player, Material.COD);

            FreshcatchListener listener = new FreshcatchListener(Enchantment.SHARPNESS, freshcatchConfig());
            listener.onFish(event);

            assertEquals(15, player.getFoodLevel(), "Freshcatch should restore hunger from edible fish catches");
            assertEquals(2.5f, player.getSaturation(), 0.0001f,
                    "Freshcatch should also add a small saturation boost");
        } finally {
            MockBukkit.unmock();
        }
    }

    @Test
    void freshcatchIgnoresNonEdibleLoot() {
        ServerMock server = MockBukkit.mock();
        try {
            PlayerMock player = server.addPlayer("freshcatch_treasure");
            ItemStack rod = new ItemStack(Material.FISHING_ROD);
            rod.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
            player.getInventory().setItemInMainHand(rod);
            player.setFoodLevel(12);
            player.setSaturation(1.0f);

            PlayerFishEvent event = edibleCatchEvent(player, Material.BOW);

            FreshcatchListener listener = new FreshcatchListener(Enchantment.SHARPNESS, freshcatchConfig());
            listener.onFish(event);

            assertEquals(12, player.getFoodLevel(), "Freshcatch should ignore junk and treasure catches");
            assertEquals(1.0f, player.getSaturation(), 0.0001f,
                    "Freshcatch should not change saturation for non-edible loot");
        } finally {
            MockBukkit.unmock();
        }
    }

    @Test
    void freshcatchCapsFoodAtTwenty() {
        ServerMock server = MockBukkit.mock();
        try {
            PlayerMock player = server.addPlayer("freshcatch_full");
            ItemStack rod = new ItemStack(Material.FISHING_ROD);
            rod.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
            player.getInventory().setItemInMainHand(rod);
            player.setFoodLevel(20);
            player.setSaturation(19.5f);

            PlayerFishEvent event = edibleCatchEvent(player, Material.SALMON);

            FreshcatchListener listener = new FreshcatchListener(Enchantment.SHARPNESS, freshcatchConfig());
            listener.onFish(event);

            assertEquals(20, player.getFoodLevel(), "Freshcatch should not exceed the vanilla food cap");
            assertEquals(20.0f, player.getSaturation(), 0.0001f,
                    "Freshcatch saturation should clamp to the new food level");
        } finally {
            MockBukkit.unmock();
        }
    }

    private static PlayerFishEvent edibleCatchEvent(PlayerMock player, Material type) {
        PlayerFishEvent event = Mockito.mock(PlayerFishEvent.class);
        Item caught = Mockito.mock(Item.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getState()).thenReturn(PlayerFishEvent.State.CAUGHT_FISH);
        when(event.getCaught()).thenReturn(caught);
        when(caught.getItemStack()).thenReturn(new ItemStack(type));
        return event;
    }

    private static FreshcatchEnchant freshcatchConfig() {
        FreshcatchEnchant config = Mockito.mock(FreshcatchEnchant.class);
        when(config.getFoodLevelPerLevel()).thenReturn(1);
        when(config.getSaturationPerLevel()).thenReturn(0.5d);
        return config;
    }
}
