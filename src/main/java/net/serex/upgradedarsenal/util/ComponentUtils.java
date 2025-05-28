package net.serex.upgradedarsenal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.serex.upgradedarsenal.modifier.ModifierRegistry;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Utility class for creating and formatting text components.
 * This class provides methods for creating formatted components for item tooltips,
 * attribute displays, and other text-based UI elements.
 */
public class ComponentUtils {

    /**
     * Creates formatted lines for displaying modifier attributes in tooltips
     * 
     * @param modifier The modifier to get attribute lines for
     * @param isBow Whether the item is a bow (affects formatting)
     * @param attributeKeyFunction Function to get the translation key for an attribute
     * @return A list of formatted component lines
     */
    public static List<Component> getFormattedAttributeLines(
            ModifierRegistry modifier,
            boolean isBow, 
            java.util.function.Function<Attribute, String> attributeKeyFunction) {
        
        List<Component> lines = new ArrayList<>();
        for (Pair<Supplier<Attribute>, ModifierRegistry.AttributeModifierSupplier> entry : modifier.modifiers) {
            Attribute attribute = entry.getKey().get();
            ModifierRegistry.AttributeModifierSupplier supplier = entry.getValue();
            double amount = supplier.amount;

            // Get the appropriate attribute key
            String attributeKey = attributeKeyFunction.apply(attribute);

            // Format the amount based on the operation
            String formattedAmount;
            if (isBow || supplier.operation == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                formattedAmount = String.format("%+d%%", (int)(amount * 100.0));
            } else {
                formattedAmount = String.format("%+.1f", amount);
            }

            // Create the component line
            MutableComponent line = Component.literal(formattedAmount + " ")
                    .append(Component.translatable(attributeKey));
            lines.add(line.withStyle(amount > 0.0 ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        return lines;
    }

    /**
     * Creates a rarity component for display in tooltips
     * 
     * @param rarity The rarity to create a component for
     * @return A formatted component for the rarity
     */
    public static Component createRarityComponent(ModifierRegistry.Rarity rarity) {
        return Component.translatable("rarity." + rarity.name().toLowerCase())
                .withStyle(style -> style.withColor(rarity.getColor()).withItalic(false));
    }

    /**
     * Updates an attribute line in a tooltip with a new value
     * 
     * @param tooltip The tooltip to update
     * @param startIndex The index to start searching from
     * @param attributeName The name of the attribute to update
     * @param value The new value for the attribute
     * @param format The format string for the value
     */
    public static void updateAttributeLine(List<Component> tooltip, int startIndex, String attributeName, double value, String format) {
        for (int i = startIndex; i < tooltip.size(); ++i) {
            String line = tooltip.get(i).getString();
            if (!line.toLowerCase().contains(attributeName)) continue;

            // Replace the numeric value in the line
            String newLine = line.replaceFirst("\\d+(\\.\\d+)?", String.format(format, value));
            MutableComponent newComponent = Component.literal(newLine).withStyle(tooltip.get(i).getStyle());
            tooltip.set(i, newComponent);
            break;
        }
    }
}