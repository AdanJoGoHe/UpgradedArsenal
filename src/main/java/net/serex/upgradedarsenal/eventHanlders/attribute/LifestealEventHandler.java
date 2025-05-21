package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the LIFESTEAL attribute.
 * Handles events related to healing from damage dealt.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class LifestealEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ModAttributes.LIFESTEAL.get();
    }

    /**
     * Event handler for entity hurt events.
     * Applies lifesteal based on the damage dealt by the player.
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // Check if the source of the damage is a player
        if (event.getSource().getEntity() instanceof Player player) {
            // Apply lifesteal based on the damage dealt
            float damageDealt = event.getAmount();
            EventUtil.handleLifesteal(player, damageDealt);
        }
    }
}
