package net.serex.upgradedarsenal.event.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the RESPIRATION_EFFICIENCY attribute.
 * Handles events related to underwater breathing efficiency.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class RespirationEfficiencyEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ModAttributes.RESPIRATION_EFFICIENCY.get();
    }
    
    /**
     * Event handler for player tick.
     * Applies respiration efficiency based on the RESPIRATION_EFFICIENCY attribute.
     */
    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        EventUtil.applyRespirationEfficiency(player);
    }
}