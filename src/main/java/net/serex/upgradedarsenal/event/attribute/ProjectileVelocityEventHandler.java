package net.serex.upgradedarsenal.event.attribute;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the PROJECTILE_VELOCITY attribute.
 * Handles events related to projectile velocity.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class ProjectileVelocityEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ModAttributes.PROJECTILE_VELOCITY.get();
    }
    
    /**
     * Event handler for arrow fired.
     * Modifies arrow velocity based on the PROJECTILE_VELOCITY attribute.
     */
    @SubscribeEvent
    public static void onArrowFired(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof AbstractArrow arrow) {
            Entity shooter = arrow.getOwner();
            if (shooter instanceof Player player) {
                ItemStack bowStack = player.getMainHandItem();
                if (bowStack.getItem() instanceof BowItem) {
                    Modifier modifier = ModifierHandler.getModifier(bowStack);
                    if (modifier != null) {
                        float velocityMultiplier = EventUtil.getVelocityMultiplier(modifier);
                        if (velocityMultiplier != 1.0f) {
                            Vec3 motion = arrow.getDeltaMovement();
                            arrow.setDeltaMovement(motion.scale(velocityMultiplier));
                        }
                    }
                }
            }
        }
    }
}