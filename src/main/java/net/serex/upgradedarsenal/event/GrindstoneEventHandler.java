package net.serex.upgradedarsenal.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.config.CustomConfig;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for grindstone interaction events.
 * Handles events related to using grindstones to re-roll modifiers.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class GrindstoneEventHandler {
    
    /**
     * Event handler for right-clicking a block.
     * Handles grindstone interactions for re-rolling modifiers.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();
        BlockState clickedBlock = event.getLevel().getBlockState(event.getPos());

        if (clickedBlock.getBlock() instanceof GrindstoneBlock
                && ModifierHandler.canHaveModifiers(heldItem)
                && ModifierHandler.hasBeenProcessed(heldItem)) {

            int rerollCount = EventUtil.getRerollCount(heldItem);
            int maxRerolls = CustomConfig.MAX_REROLLS.get();
            int xpCost = CustomConfig.REROLL_XP_COST.get();

            if (rerollCount >= maxRerolls) {
                player.displayClientMessage(Component.literal("This item has reached its re-roll limit."), false);
                return;
            }

            if (player.experienceLevel >= xpCost || player.isCreative()) {
                Modifier oldModifier = ModifierHandler.getModifier(heldItem);
                ModifierHandler.processNewItem(heldItem, player, player.getRandom());
                Modifier newModifier = ModifierHandler.getModifier(heldItem);

                if (!player.isCreative()) {
                    player.giveExperienceLevels(-xpCost);
                }

                EventUtil.setRerollCount(heldItem, ++rerollCount);
                String message = String.format("Re-rolled %s to %s (%d re-rolls remaining)",
                        oldModifier.getFormattedName().getString(),
                        newModifier.getFormattedName().getString(),
                        maxRerolls - rerollCount);
                player.displayClientMessage(Component.literal(message), false);
                event.setCanceled(true);
            } else {
                player.displayClientMessage(Component.literal("Not enough XP to re-roll. Need " + xpCost + " levels."), false);
            }
        }
    }
}