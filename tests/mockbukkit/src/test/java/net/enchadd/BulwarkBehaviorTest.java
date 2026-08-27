package net.enchadd;

import net.enchadd.enchants.BulwarkEnchant;
import net.enchadd.listeners.BulwarkListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BulwarkBehaviorTest {

    @Test
    void bulwarkGrantsResistanceAfterShieldBlockAndSkipsWhenShieldGateFails() throws Exception {
        ItemStack offhand = Mockito.mock(ItemStack.class);
        PlayerInventory inventory = Mockito.mock(PlayerInventory.class);
        when(inventory.getItemInOffHand()).thenReturn(offhand);

        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);
        Player player = Mockito.mock(Player.class);
        when(player.getInventory()).thenReturn(inventory);

        BulwarkListener listener = new BulwarkListener();
        BulwarkEnchant config = Mockito.mock(BulwarkEnchant.class);
        when(config.getCooldownTicks()).thenReturn(80);
        when(config.getResistanceSecondsPerLevel()).thenReturn(2);
        when(config.getResistanceAmplifier()).thenReturn(1);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(player);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.isSuccessfulShieldBlock(player, event)).thenReturn(true, false);
            utils.when(() -> PerformanceUtils.getEnchantLevel(offhand, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(
                    Mockito.eq(pdc),
                    Mockito.any(NamespacedKey.class),
                    Mockito.eq(80)
            )).thenReturn(false);
            utils.when(() -> PerformanceUtils.calculateDurationTicksPerLevel(2, 2)).thenReturn(40);
            utils.when(() -> PerformanceUtils.setCooldown(
                    Mockito.eq(pdc),
                    Mockito.any(NamespacedKey.class)
            )).thenAnswer(invocation -> null);

            listener.onShieldBlock(event);
            verify(player).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.RESISTANCE
                            && effect.getDuration() == 40
                            && effect.getAmplifier() == 1
            ));

            clearInvocations(player);
            listener.onShieldBlock(event);
            verify(player, never()).addPotionEffect(Mockito.any(PotionEffect.class));
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
