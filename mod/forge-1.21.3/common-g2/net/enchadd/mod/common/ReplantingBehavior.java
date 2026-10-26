package net.enchadd.mod.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;
import java.util.Map;

public final class ReplantingBehavior {

    private static final Map<String, Item> SEEDS = Map.of(
            "wheat", Items.WHEAT_SEEDS,
            "carrots", Items.CARROT,
            "potatoes", Items.POTATO,
            "beetroots", Items.BEETROOT_SEEDS,
            "torchflower_crop", Items.TORCHFLOWER_SEEDS,
            "pitcher_crop", Items.PITCHER_POD,
            "nether_wart", Items.NETHER_WART,
            "sweet_berry_bush", Items.SWEET_BERRIES,
            "cocoa", Items.COCOA_BEANS
    );

    private ReplantingBehavior() {
    }

    public static boolean replant(ServerLevel level, BlockPos pos, BlockState state, ItemStack tool, ItemStack inventorySeed) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        Item seed = SEEDS.get(path);
        if (!isMature(state) || seed == null) return false;
        if (inventorySeed.isEmpty() || inventorySeed.getItem() != seed) return false;
        inventorySeed.shrink(1);
        level.setBlock(pos, resetAge(state), 3);
        return true;
    }

    public static Item seedFor(BlockState state) {
        return SEEDS.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
    }

    public static boolean isMature(BlockState state) {
        IntegerProperty age = ageProperty(state);
        return age != null && state.getValue(age) >= age.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public static BlockState resetAge(BlockState state) {
        IntegerProperty age = ageProperty(state);
        return age == null ? state : setAge(state, age, 0);
    }

    public static boolean replant(ServerLevel level, BlockPos pos, BlockState state,
                                  ServerPlayer player, List<ItemStack> drops) {
        Item seed = seedFor(state);
        if (!isMature(state) || seed == null || !consumeSeed(player, drops, seed)) return false;
        level.setBlock(pos, resetAge(state), 3);
        return true;
    }

    private static boolean consumeSeed(ServerPlayer player, List<ItemStack> drops, Item seed) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.getItem() == seed) {
                stack.shrink(1);
                return true;
            }
        }
        for (ItemStack stack : drops) {
            if (!stack.isEmpty() && stack.getItem() == seed) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static IntegerProperty ageProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty && property.getName().equals("age")) {
                return integerProperty;
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState setAge(BlockState state, IntegerProperty age, int value) {
        int bounded = Math.max(0, Math.min(value, age.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0)));
        return state.setValue(age, bounded);
    }
}
