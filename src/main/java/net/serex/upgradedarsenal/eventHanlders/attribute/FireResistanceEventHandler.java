package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ArsenalAttributes;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the FIRE_RESISTANCE attribute.
 * Handles events related to fire damage resistance.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class FireResistanceEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ArsenalAttributes.FIRE_RESISTANCE.get();
    }
    
    /**
     * Event handler for entity hurt.
     * Reduces fire damage based on the FIRE_RESISTANCE attribute.
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        EventUtil.applyFireResistanceModifier(player, event);
    }
}