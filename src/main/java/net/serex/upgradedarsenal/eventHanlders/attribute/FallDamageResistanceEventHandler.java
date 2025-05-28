package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.ArsenalAttributes;
import net.serex.upgradedarsenal.modifier.ModifierRegistry;
import net.serex.upgradedarsenal.modifier.ModifierHandler;

/**
 * Event handler for the FALL_DAMAGE_RESISTANCE attribute.
 * Handles events related to fall damage reduction.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class FallDamageResistanceEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ArsenalAttributes.FALL_DAMAGE_RESISTANCE.get();
    }
    
    /**
     * Event handler for entity falling.
     * Reduces fall damage based on the FALL_DAMAGE_RESISTANCE attribute.
     */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        ModifierRegistry modifier = ModifierHandler.getModifier(boots);
        if (modifier == null) return;

        AttributeInstance fallRes = player.getAttribute(ArsenalAttributes.FALL_DAMAGE_RESISTANCE.get());
        if (fallRes != null) {
            double value = fallRes.getValue();
            event.setDistance((float) (event.getDistance() * (1.0 - value)));
        }
    }
}