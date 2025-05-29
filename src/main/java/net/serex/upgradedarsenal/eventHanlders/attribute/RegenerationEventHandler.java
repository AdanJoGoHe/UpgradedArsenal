package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.ArsenalAttributes;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the REGENERATION attribute.
 * Handles events related to health regeneration.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class RegenerationEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ArsenalAttributes.REGENERATION.get();
    }

    @SubscribeEvent
    public static void onPlayerTickRegen(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.isCreative()) return;

        Player player = event.player;
        if (player.tickCount % 40 != 0) return;

        EventUtil.handleRegeneration(player);
    }
}