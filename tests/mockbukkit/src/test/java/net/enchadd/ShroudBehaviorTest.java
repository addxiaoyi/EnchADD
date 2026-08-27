package net.enchadd;

import net.enchadd.enchants.ShroudEnchant;
import net.enchadd.listeners.ShroudListener;
import net.enchadd.listeners.support.ShroudTargetSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShroudBehaviorTest {

    @Test
    void shroudClearsTargetOnSuccessfulRollAndSkipsFailedRoll() {
        ShroudEnchant config = Mockito.mock(ShroudEnchant.class);
        when(config.getCooldownTicks()).thenReturn(160);
        when(config.getTriggerChance()).thenReturn(0.25);
        when(config.getMaxTriggerChance()).thenReturn(0.6);

        NamespacedKey key = new NamespacedKey("enchadd", "shroud_test");
        ShroudListener listener = new ShroudListener(
                Enchantment.UNBREAKING,
                key,
                config,
                new ShroudTargetSupport()
        );

        Player player = Mockito.mock(Player.class);
        when(player.isSneaking()).thenReturn(true);
        Mob mob = Mockito.mock(Mob.class);

        EntityTargetLivingEntityEvent event = Mockito.mock(EntityTargetLivingEntityEvent.class);
        when(event.getTarget()).thenReturn(player);
        when(event.getEntity()).thenReturn(mob);
        when(event.getReason()).thenReturn(EntityTargetLivingEntityEvent.TargetReason.CLOSEST_PLAYER);

        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getHighestEnchantLevel(equipment, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(pdc, key, 160)).thenReturn(false);
            utils.when(() -> PerformanceUtils.rollChance(0.5)).thenReturn(true, false);
            utils.when(() -> PerformanceUtils.setCooldown(pdc, key)).thenAnswer(invocation -> null);

            listener.onTarget(event);
            verify(event).setTarget(null);

            clearInvocations(event);
            listener.onTarget(event);
            verify(event, Mockito.never()).setTarget(null);
        }
    }
}
