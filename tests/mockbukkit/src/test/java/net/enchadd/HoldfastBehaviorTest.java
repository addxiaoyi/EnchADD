package net.enchadd;

import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import net.enchadd.enchants.HoldfastEnchant;
import net.enchadd.listeners.HoldfastListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HoldfastBehaviorTest {

    @Test
    void holdfastCanCancelAxeShieldDisableButIgnoresNonAxeBreaks() throws Exception {
        ItemStack shield = Mockito.mock(ItemStack.class);
        when(shield.getType()).thenReturn(Material.SHIELD);

        ItemStack axe = Mockito.mock(ItemStack.class);
        when(axe.getType()).thenReturn(Material.DIAMOND_AXE);

        ItemStack sword = Mockito.mock(ItemStack.class);
        when(sword.getType()).thenReturn(Material.DIAMOND_SWORD);

        EntityEquipment defenderEquipment = Mockito.mock(EntityEquipment.class);
        when(defenderEquipment.getItemInOffHand()).thenReturn(shield);

        EntityEquipment attackerEquipment = Mockito.mock(EntityEquipment.class);
        when(attackerEquipment.getItemInMainHand()).thenReturn(axe, sword);

        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);

        Player player = Mockito.mock(Player.class);
        LivingEntity attacker = Mockito.mock(LivingEntity.class);

        HoldfastListener listener = new HoldfastListener();
        HoldfastEnchant config = Mockito.mock(HoldfastEnchant.class);
        NamespacedKey key = new NamespacedKey("enchadd", "holdfast_test");
        when(config.getCooldownTicks()).thenReturn(0);
        when(config.getTriggerChance()).thenReturn(0.18);
        when(config.getMaxTriggerChance()).thenReturn(0.54);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);
        setField(listener, "key", key);

        PlayerShieldDisableEvent axeEvent = Mockito.mock(PlayerShieldDisableEvent.class);
        when(axeEvent.getPlayer()).thenReturn(player);
        when(axeEvent.getDamager()).thenReturn(attacker);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(defenderEquipment);
            utils.when(() -> PerformanceUtils.getEquipmentSafe(attacker)).thenReturn(attackerEquipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(shield, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(pdc, key, 0)).thenReturn(false);
            utils.when(() -> PerformanceUtils.rollChance(0.36)).thenReturn(true);
            utils.when(() -> PerformanceUtils.setCooldown(pdc, key)).thenAnswer(invocation -> null);

            listener.onShieldDisable(axeEvent);
            verify(axeEvent).setCancelled(true);
            verify(axeEvent).setCooldown(0);

            PlayerShieldDisableEvent swordEvent = Mockito.mock(PlayerShieldDisableEvent.class);
            when(swordEvent.getPlayer()).thenReturn(player);
            when(swordEvent.getDamager()).thenReturn(attacker);

            listener.onShieldDisable(swordEvent);
            verify(swordEvent, never()).setCancelled(true);
            verify(swordEvent, never()).setCooldown(0);
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
