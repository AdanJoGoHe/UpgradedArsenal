/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$BreakSpeed
 *  net.minecraftforge.event.entity.player.PlayerEvent$ItemCraftedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.serex.itemmodifiers.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;

@Mod.EventBusSubscriber(modid = "itemmodifiers")
public class DurabilityEventHandler {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        Modifier modifier = ModifierHandler.getModifier(stack);
        if (modifier != null) {
            double durabilityIncrease = modifier.getDurabilityIncrease();
            if (durabilityIncrease > 1.0) {
                float newSpeed = event.getNewSpeed() * (float) durabilityIncrease;
                event.setNewSpeed(newSpeed);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            for (ItemStack armorPiece : player.getArmorSlots()) {
                Modifier modifier = ModifierHandler.getModifier(armorPiece);
                if (modifier == null) continue;
                double durabilityIncrease = modifier.getDurabilityIncrease();
                if (durabilityIncrease > 1.0) {
                    int reducedDamage = (int) Math.max(1.0, event.getAmount() / durabilityIncrease);
                    armorPiece.setDamageValue(armorPiece.getDamageValue() + reducedDamage);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDestroyItem(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        Modifier modifier = ModifierHandler.getModifier(result);
        if (modifier != null) {
            double durabilityIncrease = modifier.getDurabilityIncrease();
            if (durabilityIncrease > 1.0) {
                int newMaxDurability = (int) (result.getMaxDamage() * durabilityIncrease);
                ModifierHandler.setMaxDurability(result, newMaxDurability);
            }
        }
    }

    private static void setRerollCount(ItemStack stack, int count) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("itemmodifiers:reroll_count", count);
    }
}


