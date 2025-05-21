package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the DRAW_SPEED attribute.
 * Handles events related to bow drawing speed.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class DrawSpeedEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ModAttributes.DRAW_SPEED.get();
    }
    
    /**
     * Event handler for item use start.
     * Sets up bow drawing parameters based on the DRAW_SPEED attribute.
     */
    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player && event.getItem().getItem() instanceof BowItem) {
            ItemStack bow = event.getItem();
            Modifier modifier = ModifierHandler.getModifier(bow);
            float drawSpeedMultiplier = EventUtil.getDrawSpeedMultiplier(modifier);
            int modifiedDrawTime = EventUtil.calculateModifiedDrawTime(drawSpeedMultiplier);

            CompoundTag tag = bow.getOrCreateTag();
            tag.putInt("StartUseTime", event.getDuration());
            tag.putInt("ModifiedDrawTime", modifiedDrawTime);
            tag.putFloat("DrawSpeedMultiplier", drawSpeedMultiplier);
            tag.putFloat("ElapsedTimeF", 0.0f);
            tag.putFloat("DrawProgress", 0.0f);
            tag.putBoolean("IsDrawing", true);
        }
    }
    
    /**
     * Event handler for item use tick.
     * Modifies bow drawing progress based on the DRAW_SPEED attribute.
     */
    @SubscribeEvent
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity() instanceof Player player && event.getItem().getItem() instanceof BowItem) {
            ItemStack bow = event.getItem();
            CompoundTag tag = bow.getOrCreateTag();
            if (!tag.getBoolean("IsDrawing")) return;

            int startUseTime = tag.getInt("StartUseTime");
            int modifiedDrawTime = tag.getInt("ModifiedDrawTime");
            float drawSpeedMultiplier = tag.getFloat("DrawSpeedMultiplier");
            float elapsedTimeF = tag.getFloat("ElapsedTimeF");
            elapsedTimeF += 1.0f + drawSpeedMultiplier;
            int elapsedTime = (int) elapsedTimeF;

            float drawProgress = Math.min(1.0f, elapsedTimeF / (float) modifiedDrawTime);
            int newDuration = startUseTime - elapsedTime;
            event.setDuration(newDuration);

            tag.putFloat("ElapsedTimeF", elapsedTimeF);
            tag.putFloat("DrawProgress", drawProgress);

            if (!player.getAbilities().instabuild) {
                player.releaseUsingItem();
            }
        }
    }
    
    /**
     * Event handler for item use stop.
     * Resets bow state when use is stopped.
     */
    @SubscribeEvent
    public static void onItemUseStop(LivingEntityUseItemEvent.Stop event) {
        if (event.getItem().getItem() instanceof BowItem) {
            EventUtil.resetBowState(event.getItem());
        }
    }
    
    /**
     * Event handler for item use finish.
     * Resets bow state when use is finished.
     */
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof BowItem) {
            EventUtil.resetBowState(event.getItem());
        }
    }
}