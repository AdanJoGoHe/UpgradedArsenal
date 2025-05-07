package net.serex.itemmodifiers.event;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;

@Mod.EventBusSubscriber(modid="itemmodifiers")
public class RangedWeaponEventHandler {
    private static final int DEFAULT_BOW_DRAW_TIME = 20;
    private static final int MIN_BOW_DRAW_TIME = 10;
    private static final int MAX_BOW_DRAW_TIME = 100;

    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player && event.getItem().getItem() instanceof BowItem) {
            ItemStack bow = event.getItem();
            Modifier modifier = ModifierHandler.getModifier(bow);
            float drawSpeedMultiplier = getDrawSpeedMultiplier(modifier);
            int modifiedDrawTime = calculateModifiedDrawTime(drawSpeedMultiplier);

            CompoundTag tag = bow.getOrCreateTag();
            tag.putInt("StartUseTime", event.getDuration());
            tag.putInt("ModifiedDrawTime", modifiedDrawTime);
            tag.putFloat("DrawSpeedMultiplier", drawSpeedMultiplier);
            tag.putFloat("ElapsedTimeF", 0.0f);
            tag.putFloat("DrawProgress", 0.0f);
            tag.putBoolean("IsDrawing", true);
        }
    }

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

    @SubscribeEvent
    public static void onItemUseStop(LivingEntityUseItemEvent.Stop event) {
        if (event.getItem().getItem() instanceof BowItem) {
            resetBowState(event.getItem());
        }
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof BowItem) {
            resetBowState(event.getItem());
        }
    }

    private static void resetBowState(ItemStack bow) {
        CompoundTag tag = bow.getOrCreateTag();
        tag.putFloat("DrawProgress", 0.0f);
        tag.putFloat("ElapsedTimeF", 0.0f);
        tag.putBoolean("IsDrawing", false);
    }

    private static float getDrawSpeedMultiplier(Modifier modifier) {
        if (modifier == null) {
            return 0.0f;
        }
        return (float)modifier.modifiers.stream().filter(pair -> ((Supplier)pair.getKey()).get() == ModAttributes.DRAW_SPEED.get()).mapToDouble(pair -> ((Modifier.AttributeModifierSupplier)pair.getValue()).amount).findFirst().orElse(0.0);
    }

    private static int calculateModifiedDrawTime(float drawSpeedMultiplier) {
        int modifiedDrawTime = drawSpeedMultiplier >= 0.0f ? Math.round(20.0f / (1.0f + drawSpeedMultiplier)) : Math.round(20.0f * (1.0f - drawSpeedMultiplier));
        return Math.max(10, Math.min(modifiedDrawTime, 100));
    }
}

