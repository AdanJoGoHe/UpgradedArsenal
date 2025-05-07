package net.serex.itemmodifiers.event;

import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;

@Mod.EventBusSubscriber(modid="itemmodifiers")
public class ArrowVelocityHandler {

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
                        float velocityMultiplier = getVelocityMultiplier(modifier);
                        if (velocityMultiplier != 1.0f) {
                            Vec3 motion = arrow.getDeltaMovement();
                            arrow.setDeltaMovement(motion.scale(velocityMultiplier));
                        }
                    }
                }
            }
        }
    }

    private static float getVelocityMultiplier(Modifier modifier) {
        return (float)modifier.modifiers.stream().filter(pair -> ((Supplier)pair.getKey()).get() == ModAttributes.PROJECTILE_VELOCITY.get()).mapToDouble(pair -> 1.0 + ((Modifier.AttributeModifierSupplier)pair.getValue()).amount).findFirst().orElse(1.0);
    }
}

