package net.serex.itemmodifiers.item;/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;

public class ModifierBook extends Item {
    public ModifierBook(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack heldItem = player.getItemInHand(hand);
            ItemStack offhandItem = player.getOffhandItem();
            Modifier currentModifier = ModifierHandler.getModifier(heldItem);

            if (ModifierHandler.canHaveModifiers(offhandItem)
                    && !ModifierHandler.hasBeenProcessed(offhandItem)
                    && currentModifier != null) {

                ModifierHandler.applyModifier(offhandItem, currentModifier);

                player.displayClientMessage(
                        Component.translatable("message.itemmodifiers.modifier_applied", currentModifier.getFormattedName()),
                        true
                );

                heldItem.shrink(1);
                return InteractionResultHolder.success(heldItem);
            }
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}


