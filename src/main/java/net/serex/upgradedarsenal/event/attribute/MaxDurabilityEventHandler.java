package net.serex.upgradedarsenal.event.attribute;

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

/**
 * Event handler for the MAX_DURABILITY attribute.
 * Handles events related to item maximum durability.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class MaxDurabilityEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ModAttributes.MAX_DURABILITY.get();
    }
    
    /**
     * Event handler for item crafting.
     * Applies max durability modifications to newly crafted items.
     */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        Modifier modifier = ModifierHandler.getModifier(result);
        if (modifier != null) {
            // Apply max durability attribute if present
            double maxDurabilityMultiplier = getMaxDurabilityMultiplier(modifier);
            if (maxDurabilityMultiplier > 1.0) {
                int newMaxDurability = (int) (result.getMaxDamage() * maxDurabilityMultiplier);
                ModifierHandler.setMaxDurability(result, newMaxDurability);
            }
        }
    }
    
    /**
     * Helper method to get the max durability multiplier from a modifier.
     * 
     * @param modifier The modifier to check
     * @return The max durability multiplier, or 1.0 if not present
     */
    private static double getMaxDurabilityMultiplier(Modifier modifier) {
        // This would typically access the MAX_DURABILITY attribute from the modifier
        // For now, we'll return a default value
        return 1.0;
    }
}