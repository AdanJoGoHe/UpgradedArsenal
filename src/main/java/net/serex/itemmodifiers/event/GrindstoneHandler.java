package net.serex.itemmodifiers.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.config.CustomConfig;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;

@Mod.EventBusSubscriber(modid = "itemmodifiers")
public class GrindstoneHandler {

    private static final String REROLL_COUNT_TAG = "itemmodifiers:reroll_count";

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        BlockState clickedBlock = event.getLevel().getBlockState(event.getPos());
        if (!(clickedBlock.getBlock() instanceof GrindstoneBlock)) return;

        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();

        if (!ModifierHandler.canHaveModifiers(heldItem) || !ModifierHandler.hasBeenProcessed(heldItem)) return;

        int rerollCount = getRerollCount(heldItem);
        int maxRerolls = CustomConfig.MAX_REROLLS.get();
        int xpCost = CustomConfig.REROLL_XP_COST.get();

        if (rerollCount >= maxRerolls) {
            player.displayClientMessage(Component.literal("This item has reached its re-roll limit."), false);
            return;
        }

        if (player.experienceLevel >= xpCost || player.isCreative()) {
            Modifier oldModifier = ModifierHandler.getModifier(heldItem);
            ModifierHandler.processNewItem(heldItem, player.getRandom().fork());
            Modifier newModifier = ModifierHandler.getModifier(heldItem);

            if (!player.isCreative()) {
                player.giveExperienceLevels(-xpCost);
            }

            setRerollCount(heldItem, ++rerollCount);

            String message = String.format(
                    "Re-rolled %s to %s (%d re-rolls remaining)",
                    oldModifier.getFormattedName().getString(),
                    newModifier.getFormattedName().getString(),
                    maxRerolls - rerollCount
            );

            player.displayClientMessage(Component.literal(message), false);
            event.setCanceled(true);
        } else {
            player.displayClientMessage(Component.literal("Not enough XP to re-roll. Need " + xpCost + " levels."), false);
        }
    }

    private static int getRerollCount(ItemStack stack) {
        return stack.getOrCreateTag().getInt(REROLL_COUNT_TAG);
    }

    private static void setRerollCount(ItemStack stack, int count) {
        stack.getOrCreateTag().putInt(REROLL_COUNT_TAG, count);
    }
}


