/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.GrindstoneBlock
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.serex.itemmodifiers.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;

@Mod.EventBusSubscriber(modid="itemmodifiers")
public class GrindstoneHandler {

    private static final int XP_COST = 5;
    private static final int MAX_REROLLS = 15;
    private static final String REROLL_COUNT_TAG = "itemmodifiers_reroll_count";

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();

        if (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof GrindstoneBlock
                && ModifierHandler.canHaveModifiers(heldItem)
                && ModifierHandler.hasBeenProcessed(heldItem)) {

            int rerollCount = getRerollCount(heldItem);
            if (rerollCount >= 15) {
                player.displayClientMessage(Component.literal("This item has reached its re-roll limit."), false);
                return;
            }

            if (player.experienceLevel >= 5 || player.isCreative()) {
                Modifier oldModifier = ModifierHandler.getModifier(heldItem);
                ModifierHandler.processNewItem(heldItem, player.getRandom().fork());
                Modifier newModifier = ModifierHandler.getModifier(heldItem);

                if (!player.isCreative()) {
                    player.giveExperienceLevels(-5);
                }

                setRerollCount(heldItem, ++rerollCount);
                String message = String.format("Re-rolled %s to %s (%d re-rolls remaining)",
                        oldModifier.getFormattedName().getString(),
                        newModifier.getFormattedName().getString(),
                        15 - rerollCount);
                player.displayClientMessage(Component.literal(message), false);
                event.setCanceled(true);
            } else {
                player.displayClientMessage(Component.literal("Not enough XP to re-roll. Need 5 levels."), false);
            }
        }
    }
    private static int getRerollCount(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt("itemmodifiers:reroll_count");
    }

    private static void setRerollCount(ItemStack stack, int count) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("itemmodifiers:reroll_count", count);
    }
}

