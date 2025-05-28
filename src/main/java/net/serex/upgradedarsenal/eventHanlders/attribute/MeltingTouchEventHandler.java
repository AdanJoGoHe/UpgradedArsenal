package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.ArsenalAttributes;
import net.serex.upgradedarsenal.modifier.ModifierRegistry;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.EventUtil;
import net.serex.upgradedarsenal.util.PlayerPlacedBlocks;

import java.util.List;
import java.util.Optional;

/**
 * Event handler for the MELTING_TOUCH attribute.
 * Handles events related to auto-smelting blocks when broken.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class MeltingTouchEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ArsenalAttributes.MELTING_TOUCH.get();
    }

    /**
     * Event handler for block breaking.
     * Auto-smelts drops based on the MELTING_TOUCH attribute.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.isCreative()) return;

        BlockState state = event.getState();
        LevelAccessor level = event.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (!EventUtil.getAllowedBlocks().contains(state.getBlock())) return;

        PlayerPlacedBlocks tracker = PlayerPlacedBlocks.get(serverLevel);
        if (tracker.isPlayerPlaced(event.getPos())) return;

        ItemStack heldItem = player.getMainHandItem();
        ModifierRegistry modifier = ModifierHandler.getModifier(heldItem);
        if (modifier == null) return;

        double meltingTouchChance = EventUtil.getMeltingTouchChance(modifier);
        if (meltingTouchChance <= 0 || serverLevel.getRandom().nextDouble() >= meltingTouchChance) return;

        event.setCanceled(true);

        List<ItemStack> originalDrops = Block.getDrops(state, serverLevel, event.getPos(), null, player, heldItem);
        if (originalDrops.isEmpty()) {
            serverLevel.removeBlock(event.getPos(), false);
            return;
        }

        for (ItemStack drop : originalDrops) {
            if (drop.isEmpty()) continue;
            Container container = new SimpleContainer(drop.copy());

            Optional<SmeltingRecipe> recipeOptional = serverLevel.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, container, serverLevel);

            if (recipeOptional.isPresent()) {
                SmeltingRecipe recipe = recipeOptional.get();
                ItemStack recipeOutput = recipe.getResultItem(serverLevel.registryAccess());

                if (!recipeOutput.isEmpty()) {
                    ItemStack smeltedItemResult = recipeOutput.copy();
                    smeltedItemResult.setCount(drop.getCount() * recipeOutput.getCount());
                    Block.popResource(serverLevel, event.getPos(), smeltedItemResult);
                } else {
                    Block.popResource(serverLevel, event.getPos(), drop.copy());
                }
            } else {
                Block.popResource(serverLevel, event.getPos(), drop.copy());
            }
        }
        serverLevel.removeBlock(event.getPos(), false);
    }

    private static class SingleItemContainer implements net.minecraft.world.Container {
        private final ItemStack stack;

        public SingleItemContainer(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot == 0 ? stack : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
        }
    }
}
