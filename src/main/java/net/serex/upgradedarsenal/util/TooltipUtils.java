package net.serex.upgradedarsenal.util;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.modifier.Modifier;

/**
 * Utility class for tooltip-related operations.
 * This class provides methods for finding positions in tooltips,
 * adding attribute information, and other tooltip-specific operations.
 */
public class TooltipUtils {

    /**
     * Finds the index of the "When on" or "When in" line in the tooltip
     * 
     * @param tooltip The tooltip to search
     * @return The index of the line, or -1 if not found
     */
    public static int findWhenIndex(List<Component> tooltip) {
        for (int i = 0; i < tooltip.size(); ++i) {
            String line = tooltip.get(i).getString();
            if (!line.startsWith("When on") && !line.startsWith("When in")) continue;
            return i;
        }
        return -1;
    }

    /**
     * Finds the end of vanilla attribute lines in the tooltip
     * 
     * @param tooltip The tooltip to search
     * @param startIndex The index to start searching from
     * @return The index of the first non-attribute line
     */
    public static int findEndOfVanillaAttributes(List<Component> tooltip, int startIndex) {
        for (int i = startIndex; i < tooltip.size(); ++i) {
            String line = tooltip.get(i).getString().toLowerCase();
            if (line.contains("armor") || line.contains("toughness") || line.contains("knockback resistance") ||
                    line.contains("attack damage") || line.contains("attack speed")) continue;
            return i;
        }
        return tooltip.size();
    }

    /**
     * Adds bow-specific attributes to the tooltip
     * 
     * @param stack The item stack
     * @param tooltip The tooltip to update
     * @param modifier The modifier to apply
     * @param insertIndex The index to insert the attributes at
     */
    public static void addBowAttributes(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        // Define the attributes and their base values
        Attribute[] attributes = {
            ModAttributes.PROJECTILE_DAMAGE.get(),
            ModAttributes.DRAW_SPEED.get(),
            ModAttributes.PROJECTILE_VELOCITY.get(),
            ModAttributes.PROJECTILE_ACCURACY.get()
        };
        String[] attributeNames = {"Damage", "Draw Speed", "Velocity", "Accuracy"};
        double[] baseValues = {2.0, 1.0, 1.0, 1.0};

        // Add each attribute to the tooltip
        for (int i = 0; i < attributes.length; i++) {
            double finalValue = AttributeUtils.calculateFinalAttributeValue(stack, attributes[i], baseValues[i], modifier);
            tooltip.add(insertIndex++, Component.literal(String.format("+%.1f %s", finalValue, attributeNames[i]))
                    .withStyle(net.minecraft.ChatFormatting.BLUE));
        }
    }
}