package net.enchadd.mod.common;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.HolderLookup;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.List;

public final class SmeltingBehavior {
    private static final ResourceKey<Enchantment> SMELTING = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "smelting"));

    private SmeltingBehavior() {
    }

    public static ItemStack smelt(ServerLevel level, ItemStack input) {
        if (input.isEmpty()) return ItemStack.EMPTY;
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager().getRecipeFor(
                RecipeType.SMELTING,
                new SingleRecipeInput(input.copyWithCount(1)),
                level
        );
        if (recipe.isEmpty()) return ItemStack.EMPTY;
        SingleRecipeInput single = new SingleRecipeInput(input.copyWithCount(1));
        try {
            Object value = recipe.get().value();
            Method assemble;
            Object result;
            try {
                assemble = value.getClass().getMethod("assemble", SingleRecipeInput.class, HolderLookup.Provider.class);
                result = assemble.invoke(value, single, level.registryAccess());
            } catch (NoSuchMethodException ignored) {
                assemble = value.getClass().getMethod("assemble", SingleRecipeInput.class);
                result = assemble.invoke(value, single);
            }
            if (!(result instanceof ItemStack stack) || stack.isEmpty()) return ItemStack.EMPTY;
            return stack.copyWithCount(Math.min(input.getCount(), stack.getMaxStackSize()));
        } catch (ReflectiveOperationException ignored) {
            return ItemStack.EMPTY;
        }
    }

    public static List<ItemStack> convertDrops(ServerLevel level, BlockPos pos, BlockEntity blockEntity,
                                               Entity breaker, ItemStack tool, List<ItemStack> drops) {
        if (blockEntity != null || tool.isEmpty()) return drops;
        var holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SMELTING);
        if (EnchantmentHelper.getItemEnchantmentLevel(holder, tool) <= 0) return drops;
        drops.replaceAll(drop -> {
            ItemStack converted = smelt(level, drop);
            return converted.isEmpty() ? drop : converted;
        });
        return drops;
    }

}

