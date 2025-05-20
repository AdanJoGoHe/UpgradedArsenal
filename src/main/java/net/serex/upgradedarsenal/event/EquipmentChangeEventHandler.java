package net.serex.upgradedarsenal.event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.modifier.ModifierHandler;

/**
 * Event handler for equipment change events.
 * Handles events related to equipping and unequipping armor.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class EquipmentChangeEventHandler {
    
    /**
     * Event handler for living equipment change.
     * Handles equipment changes for armor slots.
     */
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        EquipmentSlot slot = event.getSlot();
        if (slot.getType() != EquipmentSlot.Type.ARMOR) return;

        ItemStack oldItem = event.getFrom();
        ItemStack newItem = event.getTo();
        ModifierHandler.handleEquipmentChange(player, slot, oldItem, newItem);
    }
}