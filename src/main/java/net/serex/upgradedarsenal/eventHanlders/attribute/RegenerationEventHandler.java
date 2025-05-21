package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the REGENERATION attribute.
 * Handles events related to health regeneration.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class RegenerationEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ModAttributes.REGENERATION.get();
    }
    
    /**
     * Event handler for player tick.
     * Applies health regeneration based on the REGENERATION attribute.
     */
    @SubscribeEvent
    public static void onPlayerTickRegen(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.isCreative()) return;

        Player player = event.player;
        if (player.tickCount % 40 != 0) return;

        EventUtil.handleRegeneration(player);
    }
    
    /**
     * Additional regeneration effect during fall.
     * This is called from the fall event to provide immediate healing.
     */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        AttributeInstance healthRegen = player.getAttribute(ModAttributes.REGENERATION.get());
        if (healthRegen != null && player.getHealth() < player.getMaxHealth()) {
            player.heal((float) healthRegen.getValue());
        }
    }
}