package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.util.EventUtil;

import java.util.Random;

/**
 * Event handler for the CRITICAL_CHANCE and CRITICAL_DAMAGE attributes.
 * Handles events related to critical hits in combat.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class CriticalHitEventHandler extends AttributeEventHandler {
    private static final Random RANDOM = new Random();

    @Override
    public Attribute getAttribute() {
        return ModAttributes.CRITICAL_CHANCE.get();
    }

    /**
     * Event handler for entity hurt events.
     * Applies critical hit chance and damage based on the player's attributes.
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // Check if the source of the damage is a player
        if (event.getSource().getEntity() instanceof Player player) {
            // Check if a critical hit occurs
            if (EventUtil.rollForCriticalHit(player)) {
                // Apply critical damage multiplier
                float baseDamage = event.getAmount();
                float criticalDamage = baseDamage * EventUtil.getCriticalDamageMultiplier(player);

                // Set the new damage amount
                event.setAmount(criticalDamage);

                // Optionally, you could add a visual or sound effect here to indicate a critical hit
                // For example: player.level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                //              SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}
