package net.serex.upgradedarsenal.event.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the JUMP_HEIGHT attribute.
 * Handles events related to jump height enhancement.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class JumpHeightEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ModAttributes.JUMP_HEIGHT.get();
    }
    
    /**
     * Event handler for entity jumping.
     * Increases jump height based on the JUMP_HEIGHT attribute.
     */
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double jumpBoost = EventUtil.getAttributeValueFromAll(player, ModAttributes.JUMP_HEIGHT.get());
        if (jumpBoost > 1.0) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, motion.y + (0.1 * (jumpBoost - 1.0)), motion.z);
        }
    }
    
    /**
     * Additional jump boost effect during fall.
     * This is called from the fall event to provide additional upward momentum.
     */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        AttributeInstance jumpBoost = player.getAttribute(ModAttributes.JUMP_HEIGHT.get());
        if (jumpBoost != null && jumpBoost.getValue() > 0.0) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.05 * jumpBoost.getValue(), 0));
        }
    }
}