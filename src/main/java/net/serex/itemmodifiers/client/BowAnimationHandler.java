/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.api.distmarker.OnlyIn
 *  net.minecraftforge.client.event.RenderHandEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.serex.itemmodifiers.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "itemmodifiers", value = Dist.CLIENT)
public class BowAnimationHandler {

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null || !(player.getUseItem().getItem() instanceof BowItem)) {
            return;
        }

        ItemStack bowStack = player.getUseItem();
        float drawProgress = bowStack.getOrCreateTag().getFloat("DrawProgress");
        applyBowTransformations(event.getPoseStack(), drawProgress);
    }

    private static void applyBowTransformations(PoseStack poseStack, float drawProgress) {
        poseStack.pushPose();

        float verticalAdjustment = -0.005f * drawProgress;
        poseStack.translate(0.0f, verticalAdjustment, 0.0f);

        float pitchRotation = 5.0f * drawProgress;
        poseStack.mulPose(Axis.XP.rotationDegrees(pitchRotation));

        float yawRotation = 2.0f * drawProgress;
        poseStack.mulPose(Axis.YP.rotationDegrees(yawRotation));

        poseStack.popPose();
    }
}

