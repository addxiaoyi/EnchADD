package net.enchadd.mod;

import net.enchadd.mod.common.NourishRules;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

final class EnchADDNourishBehavior {

    private static final ResourceKey<Enchantment> NOURISH = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("enchadd", "nourish")
    );
    private static final Map<UUID, PendingUse> PENDING = new HashMap<>();

    private EnchADDNourishBehavior() {
    }

    static void init() {
        UseItemCallback.EVENT.register(EnchADDNourishBehavior::onUse);
        ServerTickEvents.END_SERVER_TICK.register(EnchADDNourishBehavior::onTick);
    }

    private static InteractionResult onUse(Player player, Level level, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        ItemStack stack = serverPlayer.getItemInHand(hand);
        if (stack.isEmpty() || !stack.has(DataComponents.CONSUMABLE) || !stack.has(DataComponents.FOOD)) {
            return InteractionResult.PASS;
        }
        int enchantLevel = nourishLevel(serverPlayer, serverPlayer.registryAccess());
        if (enchantLevel <= 0) {
            return InteractionResult.PASS;
        }
        PENDING.put(serverPlayer.getUUID(), new PendingUse(hand, stack.getCount(), 0, false));
        return InteractionResult.PASS;
    }

    private static void onTick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, PendingUse>> iterator = PENDING.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingUse> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            PendingUse pending = entry.getValue();
            if (player == null || pending.age > 80) {
                iterator.remove();
                continue;
            }
            ItemStack active = player.getItemInHand(pending.hand);
            if (player.isUsingItem() && player.getUsedItemHand() == pending.hand) {
                pending.sawUse = true;
                pending.age++;
                continue;
            }
            if (!pending.sawUse) {
                pending.age++;
                continue;
            }
            if (active.getCount() < pending.originalCount) {
                int level = nourishLevel(player, player.registryAccess());
                apply(player, level);
            }
            iterator.remove();
        }
    }

    private static int nourishLevel(ServerPlayer player, RegistryAccess access) {
        Holder<Enchantment> holder = access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(NOURISH);
        return Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getItemBySlot(EquipmentSlot.CHEST)));
    }

    private static void apply(ServerPlayer player, int level) {
        int duration = NourishRules.durationTicks(level);
        if (duration > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, NourishRules.amplifier(), false, false, true));
        }
    }

    private static final class PendingUse {
        private final InteractionHand hand;
        private final int originalCount;
        private int age;
        private boolean sawUse;

        private PendingUse(InteractionHand hand, int originalCount, int age, boolean sawUse) {
            this.hand = hand;
            this.originalCount = originalCount;
            this.age = age;
            this.sawUse = sawUse;
        }
    }
}
