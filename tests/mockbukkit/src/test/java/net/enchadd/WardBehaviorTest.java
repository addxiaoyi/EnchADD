package net.enchadd;

import net.enchadd.enchants.WardEnchant;
import net.enchadd.listeners.WardListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WardBehaviorTest {

    @Test
    void wardOnlyCancelsDamageWhileTheShieldIsActuallyRaised() throws Exception {
        ItemStack shield = new ItemStack(Material.SHIELD);
        shield.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);

        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getItemInOffHand()).thenReturn(shield);

        World world = Mockito.mock(World.class);
        Player player = Mockito.mock(Player.class);
        when(player.getEquipment()).thenReturn(equipment);
        when(player.getWorld()).thenReturn(world);
        when(player.isBlocking()).thenReturn(false, true, true);
        when(player.isHandRaised()).thenReturn(false, true);
        when(player.getHandRaisedTime()).thenReturn(6);
        when(player.getActiveItem()).thenReturn(shield);
        Location playerLocation = new Location(world, 0.0, 64.0, 0.0);
        playerLocation.setDirection(new Vector(0.0, 0.0, 1.0));
        when(player.getLocation()).thenReturn(playerLocation);

        WardListener listener = new WardListener();
        WardEnchant config = Mockito.mock(WardEnchant.class);
        when(config.getCooldownTicks()).thenReturn(0);
        when(config.getBlockSound()).thenReturn("minecraft:item.shield.block");

        setField(listener, "ward", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        Entity damager = Mockito.mock(Entity.class);
        when(damager.getLocation()).thenReturn(new Location(world, 0.0, 64.0, 2.0));
        when(event.getEntity()).thenReturn(player);
        when(event.getDamager()).thenReturn(damager);
        when(event.getDamageSource()).thenReturn(null);
        when(event.getFinalDamage()).thenReturn(3.2);

        listener.onEntityDamageWithWard(event);
        verify(event, never()).setCancelled(true);

        listener.onEntityDamageWithWard(event);
        verify(event, never()).setCancelled(true);

        listener.onEntityDamageWithWard(event);
        verify(event).setCancelled(true);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
