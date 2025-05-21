package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

/**
 * Base abstract class for all attribute-specific event handlers.
 * Each attribute in the mod should have its own event handler that extends this class.
 */
public abstract class AttributeEventHandler {
    
    /**
     * Gets the attribute associated with this event handler.
     * 
     * @return The attribute this handler is responsible for
     */
    public abstract Attribute getAttribute();
    
    /**
     * Gets the attribute value for a player.
     * 
     * @param player The player to get the attribute value for
     * @return The value of the attribute for the player, or 0 if the player doesn't have the attribute
     */
    protected double getAttributeValue(Player player) {
        if (player.getAttribute(getAttribute()) != null) {
            return player.getAttribute(getAttribute()).getValue();
        }
        return 0.0;
    }
    
    /**
     * Checks if the player has a non-zero value for this attribute.
     * 
     * @param player The player to check
     * @return true if the player has a value greater than 0 for this attribute
     */
    protected boolean hasAttribute(Player player) {
        return getAttributeValue(player) > 0.0;
    }
}