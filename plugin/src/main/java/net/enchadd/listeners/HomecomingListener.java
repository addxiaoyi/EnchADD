package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.enchants.HomecomingEnchant;
import net.enchadd.events.HomecomingEvent;
import net.enchadd.listeners.support.HomecomingResurrectSupport;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;

public class HomecomingListener implements Listener {

    private final Enchantment homecoming;
    private final HomecomingResurrectSupport resurrectSupport;

    public HomecomingListener() {
        this(resolveEnchant(HomecomingEnchant.KEY), new HomecomingResurrectSupport());
    }

    public HomecomingListener(Enchantment homecoming, HomecomingResurrectSupport resurrectSupport) {
        this.homecoming = homecoming;
        this.resurrectSupport = resurrectSupport;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHomecoming(EntityResurrectEvent event) {
        if (homecoming == null) return;

        if (!(event.getEntity() instanceof Player player)) return;

        EquipmentSlot equipmentSlot = event.getHand();
        if (equipmentSlot == null) return;

        if (!resurrectSupport.hasHomecomingItem(player, equipmentSlot, homecoming)) return;

        Location location = resurrectSupport.resolveDestination(player);
        if (location == null) return;

        HomecomingEvent homecomingEvent = new HomecomingEvent(player, location);
        player.getServer().getPluginManager().callEvent(homecomingEvent);
        if (homecomingEvent.isCancelled()) return;

        player.teleport(homecomingEvent.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);

    }

    private static Enchantment resolveEnchant(Key key) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return registry.get(key);
    }

}
