package net.enchadd;

import net.enchadd.enchants.FleetfootEnchant;
import net.enchadd.listeners.FleetfootListener;
import net.enchadd.listeners.support.FleetfootSprintSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FleetfootBehaviorTest {

    @Test
    void fleetfootAppliesSpeedAndRespectsCooldown() {
        FleetfootEnchant config = Mockito.mock(FleetfootEnchant.class);
        when(config.getCooldownTicks()).thenReturn(60);
        when(config.getSpeedSecondsPerLevel()).thenReturn(2);
        when(config.getSpeedAmplifier()).thenReturn(1);

        NamespacedKey key = new NamespacedKey("enchadd", "fleetfoot_test");
        FleetfootListener listener = new FleetfootListener(
                Enchantment.UNBREAKING,
                key,
                config,
                new FleetfootSprintSupport()
        );

        Player player = Mockito.mock(Player.class);
        PlayerToggleSprintEvent event = Mockito.mock(PlayerToggleSprintEvent.class);
        when(event.isSprinting()).thenReturn(true);
        when(event.getPlayer()).thenReturn(player);

        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        ItemStack boots = Mockito.mock(ItemStack.class);
        when(equipment.getBoots()).thenReturn(boots);
        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(boots, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(pdc, key, 60)).thenReturn(false, true);
            utils.when(() -> PerformanceUtils.setCooldown(pdc, key)).thenAnswer(invocation -> null);
            utils.when(() -> PerformanceUtils.calculateDurationTicksPerLevel(2, 2)).thenReturn(80);

            listener.onSprintToggle(event);
            verify(player).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.SPEED
                            && effect.getDuration() == 80
                            && effect.getAmplifier() == 1
            ));

            clearInvocations(player);
            listener.onSprintToggle(event);
            verify(player, Mockito.never()).addPotionEffect(Mockito.any(PotionEffect.class));
        }
    }
}
