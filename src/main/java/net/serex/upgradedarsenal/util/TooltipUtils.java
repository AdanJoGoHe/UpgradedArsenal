package net.serex.upgradedarsenal.util;

import java.util.List;
import net.minecraft.network.chat.Component;

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
}