package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ArsenalAttributes;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the MOVEMENT_SPEED attribute.
 * Handles events related to player movement speed.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class MovementSpeedEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ArsenalAttributes.MOVEMENT_SPEED.get();
    }

    @SubscribeEvent
    public static void onPlayerUpdate(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.tickCount % 20 == 0) {
            EventUtil.updatePlayerMovementSpeed(player);
        }
    }
}