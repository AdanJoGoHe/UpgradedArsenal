package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the XP_GAIN_BONUS attribute.
 * Handles events related to XP gain bonuses.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class XpGainBonusEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ModAttributes.XP_GAIN_BONUS.get();
    }
    
    /**
     * Event handler for XP gain.
     * Increases the amount of XP gained based on the XP_GAIN_BONUS attribute.
     */
    @SubscribeEvent
    public static void onXpGain(PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        double bonus = EventUtil.getAttributeValueFromAll(player, ModAttributes.XP_GAIN_BONUS.get());
        if (bonus > 0) {
            int extra = (int)(event.getAmount() * bonus);
            event.setAmount(event.getAmount() + extra);
        }
    }
}