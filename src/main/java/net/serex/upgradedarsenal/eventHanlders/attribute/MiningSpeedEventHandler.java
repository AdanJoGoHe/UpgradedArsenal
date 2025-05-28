package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ArsenalAttributes;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.EventUtil;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class MiningSpeedEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ArsenalAttributes.MINING_SPEED.get();
    }

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