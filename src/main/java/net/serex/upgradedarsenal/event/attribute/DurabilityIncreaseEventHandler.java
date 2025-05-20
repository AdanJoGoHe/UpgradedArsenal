package net.serex.upgradedarsenal.event.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;

/**
 * Event handler for the DURABILITY_INCREASE attribute.
 * Handles events related to item durability enhancement.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class DurabilityIncreaseEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ModAttributes.DURABILITY_INCREASE.get();
    }
    
    /**
     * Event handler for break speed calculation.
     * Increases mining speed based on the DURABILITY_INCREASE attribute.
     */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        Modifier modifier = ModifierHandler.getModifier(stack);
        if (modifier != null) {
            double durabilityIncrease = modifier.getDurabilityIncrease();
            if (durabilityIncrease > 1.0) {
                float newSpeed = event.getNewSpeed() * (float) durabilityIncrease;
                event.setNewSpeed(newSpeed);
            }
        }
    }
    
    /**
     * Event handler for living hurt.
     * Reduces damage to armor based on the DURABILITY_INCREASE attribute.
     */
    @SubscribeEvent
    public static void onLivingHurtDurability(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            for (ItemStack armorPiece : player.getArmorSlots()) {
                Modifier modifier = ModifierHandler.getModifier(armorPiece);
                if (modifier == null) continue;
                double durabilityIncrease = modifier.getDurabilityIncrease();
                if (durabilityIncrease > 1.0) {
                    int reducedDamage = (int) Math.max(1.0, event.getAmount() / durabilityIncrease);
                    armorPiece.setDamageValue(armorPiece.getDamageValue() + reducedDamage);
                }
            }
        }
    }
    
    /**
     * Event handler for item crafting.
     * Increases max durability of crafted items based on the DURABILITY_INCREASE attribute.
     */
    @SubscribeEvent
    public static void onPlayerDestroyItem(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        Modifier modifier = ModifierHandler.getModifier(result);
        if (modifier != null) {
            double durabilityIncrease = modifier.getDurabilityIncrease();
            if (durabilityIncrease > 1.0) {
                int newMaxDurability = (int) (result.getMaxDamage() * durabilityIncrease);
                ModifierHandler.setMaxDurability(result, newMaxDurability);
            }
        }
    }
}