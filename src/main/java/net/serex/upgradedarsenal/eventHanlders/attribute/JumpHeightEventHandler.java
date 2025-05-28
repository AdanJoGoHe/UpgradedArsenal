package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ArsenalAttributes;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the JUMP_HEIGHT attribute.
 * Handles events related to jump height enhancement.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class JumpHeightEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ArsenalAttributes.JUMP_HEIGHT.get();
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        double jumpHeightBonus = EventUtil.getAttributeValueFromAll(player, ArsenalAttributes.JUMP_HEIGHT.get());
        final double EPSILON = 0.0001;
        if (Math.abs(jumpHeightBonus) > EPSILON) {
            Vec3 currentMotion = player.getDeltaMovement();
            double additionalUpwardVelocity = 0.1 * jumpHeightBonus;

            player.setDeltaMovement(currentMotion.x, currentMotion.y + additionalUpwardVelocity, currentMotion.z);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double jumpBoost = EventUtil.getAttributeValueFromAll(player, ArsenalAttributes.JUMP_HEIGHT.get());
        if (jumpBoost > 1.0) {
            float damageReduction = (float)(jumpBoost - 1.0) * 0.5f;
            event.setDistance(Math.max(0, event.getDistance() - damageReduction));
        }
    }
}
