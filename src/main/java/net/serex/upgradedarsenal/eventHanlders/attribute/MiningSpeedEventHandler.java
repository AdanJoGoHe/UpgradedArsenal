package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the MINING_SPEED attribute.
 * Handles events related to mining speed enhancement.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class MiningSpeedEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ModAttributes.MINING_SPEED.get();
    }
    
    /**
     * Event handler for break speed calculation.
     * Increases mining speed based on the MINING_SPEED attribute.
     */
    @SubscribeEvent
    public static void onBreakSpeedMining(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();
        Modifier modifier = ModifierHandler.getModifier(heldItem);
        if (ModifierHandler.canHaveModifiers(heldItem) && modifier != null) {
            double speedMultiplier = EventUtil.calculateMiningSpeedMultiplier(modifier);
            event.setNewSpeed((float) (event.getNewSpeed() * speedMultiplier));
        }
    }
}