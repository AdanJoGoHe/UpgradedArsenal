package net.serex.upgradedarsenal.event.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.EventUtil;

/**
 * Event handler for the PROJECTILE_DAMAGE attribute.
 * Handles events related to projectile damage.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class ProjectileDamageEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ModAttributes.PROJECTILE_DAMAGE.get();
    }

    /**
     * Event handler for living hurt.
     * Modifies damage dealt by projectiles based on the PROJECTILE_DAMAGE attribute.
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack heldItem = player.getMainHandItem();
        if (!ModifierHandler.hasBeenProcessed(heldItem)) return;

        EventUtil.applyAttackDamageModifier(player, heldItem, event);
    }
}
