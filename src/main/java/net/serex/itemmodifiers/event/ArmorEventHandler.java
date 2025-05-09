package net.serex.itemmodifiers.event;

import java.util.UUID;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;

import static net.serex.itemmodifiers.event.CombatEventHandler.getAttributeValueFromAll;

@Mod.EventBusSubscriber(modid = "itemmodifiers")
public class ArmorEventHandler {

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

    @SubscribeEvent
    public static void onXpGain(PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        double bonus = getAttributeValueFromAll(player, ModAttributes.XP_GAIN_BONUS.get());
        if (bonus > 0) {
            int extra = (int)(event.getAmount() * bonus);
            event.setAmount(event.getAmount() + extra);
        }
    }


    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        Modifier modifier = ModifierHandler.getModifier(boots);
        if (modifier == null) return;

        AttributeInstance fallRes = player.getAttribute(ModAttributes.FALL_DAMAGE_RESISTANCE.get());
        if (fallRes != null) {
            double value = fallRes.getValue();
            event.setDistance((float) (event.getDistance() * (1.0 - value)));
        }

        AttributeInstance jumpBoost = player.getAttribute(ModAttributes.JUMP_HEIGHT.get());
        if (jumpBoost != null && jumpBoost.getValue() > 0.0) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.05 * jumpBoost.getValue(), 0));
        }



        AttributeInstance healthRegen = player.getAttribute(ModAttributes.REGENERATION.get());
        if (healthRegen != null && player.getHealth() < player.getMaxHealth()) {
            player.heal((float) healthRegen.getValue());
        }
    }
}

