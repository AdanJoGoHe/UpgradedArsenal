package net.serex.itemmodifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;
import net.serex.itemmodifiers.util.AttributeDisplayUtils;
import net.serex.itemmodifiers.util.AttributeUtils;
import net.serex.itemmodifiers.util.ComponentUtils;
import net.serex.itemmodifiers.util.TooltipUtils;
import org.apache.commons.lang3.tuple.Pair;

import static net.serex.itemmodifiers.util.AttributeDisplayUtils.*;

/**
 * Handles the display of tooltips for items with modifiers.
 * This class is responsible for updating item tooltips to show modifier information,
 * including attribute changes, rarity, and other modifier-specific details.
 * It handles different types of items (armor, weapons, bows) differently.
 */
@Mod.EventBusSubscriber(modid="itemmodifiers", bus=Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        if (stack.hasTag() && stack.getTag().contains("itemmodifiers:modifier")) {
            Modifier modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                if (stack.getItem() instanceof BowItem) {
                    updateItemTooltip(stack, tooltip, modifier, true);
                } else if (!(stack.getItem() instanceof CrossbowItem)) {
                    updateItemTooltip(stack, tooltip, modifier, false);
                }
            }
        }
    }

    /**
     * Updates the tooltip for an item with modifier information
     *
     * @param stack The item stack
     * @param tooltip The tooltip to update
     * @param modifier The modifier to apply
     * @param isBow Whether the item is a bow
     */
    private static void updateItemTooltip(ItemStack stack, List<Component> tooltip, Modifier modifier, boolean isBow) {
        if (isBow) {
            // Componer el nombre del modificador + nombre del ítem
            MutableComponent itemName = Component.translatable(stack.getItem().getDescriptionId());
            MutableComponent fullName = Component.literal(modifier.getFormattedName().getString() + " ")
                    .append(itemName)
                    .withStyle(style -> style.withColor(modifier.rarity.getColor()));
            tooltip.set(0, fullName);

            // Añadir la línea de rareza
            tooltip.add(1, Component.translatable("rarity." + modifier.rarity.name().toLowerCase())
                    .withStyle(style -> style.withColor(modifier.rarity.getColor()).withItalic(false)));

            // Añadir atributos especiales de arcos
            tooltip.add(2, Component.empty());
            TooltipUtils.addBowAttributes(stack, tooltip, modifier, 3);
            tooltip.add(Component.empty());

            // Añadir el nombre del modificador en negrita
            tooltip.add(modifier.getFormattedName()
                    .copy()
                    .withStyle(style -> style.withColor(modifier.rarity.getColor()).withBold(true)));

            // Añadir las líneas descriptivas del modificador
            for (Component line : ComponentUtils.getFormattedAttributeLines(modifier, true, AttributeDisplayUtils::getBowAttributeTranslationKey)) {
                tooltip.add(line);
            }
        } else {
            // Standard handling for other items
            tooltip.add(1, Component.translatable("rarity." + modifier.rarity.name().toLowerCase())
                    .withStyle(style -> style.withColor(modifier.rarity.getColor()).withItalic(false)));

            int whenIndex = findWhenIndex(tooltip);
            if (stack.getItem() instanceof ArmorItem) {
                updateArmorAttributes(stack, tooltip, modifier, whenIndex + 1);
            } else if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) {
                updateRangedAttributes(stack, tooltip, modifier, whenIndex + 1);
            } else {
                updateWeaponAttributes(stack, tooltip, modifier, whenIndex + 1);
            }
            int modifierIndex = findEndOfVanillaAttributes(tooltip, whenIndex + 1);
            addModifierLines(stack, tooltip, modifier, modifierIndex);
        }
    }

    /**
     * Finds the index of the "When on" or "When in" line in the tooltip
     *
     * @param tooltip The tooltip to search
     * @return The index of the line, or -1 if not found
     */
    private static int findWhenIndex(List<Component> tooltip) {
        for (int i = 0; i < tooltip.size(); ++i) {
            String line = tooltip.get(i).getString();
            if (!line.startsWith("When on") && !line.startsWith("When in")) continue;
            return i;
        }
        return -1;
    }

    /**
     * Adds modifier information lines to the tooltip
     *
     * @param stack The item stack
     * @param tooltip The tooltip to update
     * @param modifier The modifier to apply
     * @param insertIndex The index to insert the modifier lines at
     */
    private static void addModifierLines(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        // Add a blank line
        tooltip.add(insertIndex++, Component.empty());

        // Add the modifier name
        tooltip.add(insertIndex++, modifier.getFormattedName().copy()
                .withStyle(modifier.rarity.getColor(), ChatFormatting.UNDERLINE));

        // Add the modifier attribute lines
        for (Component line : getFormattedInfoLines(modifier, false)) {
            tooltip.add(insertIndex++, line);
        }
    }

    /**
     * Creates formatted lines for displaying modifier attributes in tooltips
     *
     * @param modifier The modifier to get attribute lines for
     * @param isBow Whether the item is a bow (affects formatting)
     * @return A list of formatted component lines
     */
    private static List<Component> getFormattedInfoLines(Modifier modifier, boolean isBow) {
        List<Component> lines = new ArrayList<>();
        for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
            Attribute attribute = entry.getKey().get();
            Modifier.AttributeModifierSupplier supplier = entry.getValue();
            double amount = supplier.amount;

            // Get the appropriate attribute key based on whether it's a bow or not
            String attributeKey = isBow ?
                getBowAttributeTranslationKey(attribute) :
                getAttributeTranslationKey(attribute);

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
     * Updates ranged weapon attributes in the tooltip
     *
     * @param stack The item stack
     * @param tooltip The tooltip to update
     * @param modifier The modifier to apply
     * @param insertIndex The index to insert the attributes at
     */
    private static void updateRangedAttributes(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        // Remove existing attack damage and attack speed lines
        while (insertIndex < tooltip.size()) {
            String line = tooltip.get(insertIndex).getString().toLowerCase();
            if (!line.contains("attack damage") && !line.contains("attack speed")) break;
            tooltip.remove(insertIndex);
        }

        // Add ranged weapon attribute lines
        for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
            Attribute attribute = entry.getKey().get();
            Modifier.AttributeModifierSupplier supplier = entry.getValue();
            double value = supplier.amount;
            String attributeName = getAttributeNameForRangedWeapon(attribute);

            // Skip attributes that aren't relevant for ranged weapons
            if (attributeName == null) continue;

            // Format and add the attribute line
            String formattedValue = formatAttributeValue(value, supplier.operation);
            MutableComponent line = Component.literal(formattedValue + " " + attributeName)
                    .withStyle(value > 0.0 ? ChatFormatting.BLUE : ChatFormatting.RED);
            tooltip.add(insertIndex++, line);
        }
    }

    /**
     * Adds bow-specific attributes to the tooltip
     *
     * @param stack The item stack
     * @param tooltip The tooltip to update
     * @param modifier The modifier to apply
     * @param insertIndex The index to insert the attributes at
     */
    private static void addBowAttributes(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
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
            double finalValue = calculateFinalAttributeValue(stack, attributes[i], baseValues[i], modifier);
            tooltip.add(insertIndex++, Component.literal(String.format("+%.1f %s", finalValue, attributeNames[i]))
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    /**
     * Gets the translation key for a bow attribute
     *
     * @param attribute The attribute
     * @return The translation key
     */
    private static String getBowAttributeTranslationKey(Attribute attribute) {
        return BOW_ATTRIBUTE_TRANSLATION_KEYS.getOrDefault(attribute, attribute.getDescriptionId());
    }

    /**
     * Formats an attribute value based on the operation
     *
     * @param value The attribute value
     * @param operation The attribute modifier operation
     * @return The formatted attribute value
     */
    private static String formatAttributeValue(double value, AttributeModifier.Operation operation) {
        return AttributeUtils.formatAttributeValue(value, operation);
    }

    /**
     * Gets the display name for a ranged weapon attribute
     *
     * @param attribute The attribute
     * @return The display name, or null if not a ranged weapon attribute
     */
    private static String getAttributeNameForRangedWeapon(Attribute attribute) {
        return RANGED_WEAPON_ATTRIBUTE_NAMES.getOrDefault(attribute, null);
    }

    /**
     * Gets the translation key for an attribute
     *
     * @param attribute The attribute
     * @return The translation key
     */
    private static String getAttributeTranslationKey(Attribute attribute) {
        return ATTRIBUTE_TRANSLATION_KEYS.getOrDefault(attribute, attribute.getDescriptionId());
    }

    /**
     * Updates armor attributes in the tooltip
     *
     * @param stack The item stack
     * @param tooltip The tooltip to update
     * @param modifier The modifier to apply
     * @param insertIndex The index to insert the attributes at
     */
    private static void updateArmorAttributes(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        ArmorItem armorItem = (ArmorItem) stack.getItem();

        // Define the attributes to update
        Attribute[] attributes = {Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS};
        String[] attributeNames = {"armor", "toughness"};
        double[] baseValues = {armorItem.getDefense(), armorItem.getToughness()};

        // Update each attribute
        for (int i = 0; i < attributes.length; i++) {
            double finalValue = calculateFinalAttributeValue(stack, attributes[i], baseValues[i], modifier);
            updateAttributeLine(tooltip, insertIndex, attributeNames[i], finalValue, "%.1f");
        }
    }

    /**
     * Updates weapon attributes in the tooltip
     *
     * @param stack The item stack
     * @param tooltip The tooltip to update
     * @param modifier The modifier to apply
     * @param insertIndex The index to insert the attributes at
     */
    private static void updateWeaponAttributes(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        // Define the attributes to update
        Attribute[] attributes = {Attributes.ATTACK_DAMAGE, Attributes.ATTACK_SPEED};
        String[] attributeNames = {"attack damage", "attack speed"};

        // Update each attribute
        for (int i = 0; i < attributes.length; i++) {
            double baseValue = getBaseAttributeValue(stack, attributes[i]);
            double finalValue = calculateFinalAttributeValue(stack, attributes[i], baseValue, modifier);
            updateAttributeLine(tooltip, insertIndex, attributeNames[i], finalValue, "%.1f");
        }
    }

    /**
     * Updates an attribute line in the tooltip with a new value
     *
     * @param tooltip The tooltip to update
     * @param startIndex The index to start searching from
     * @param attributeName The name of the attribute to update
     * @param value The new value for the attribute
     * @param format The format string for the value
     */
    private static void updateAttributeLine(List<Component> tooltip, int startIndex, String attributeName, double value, String format) {
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

    /**
     * Finds the end of vanilla attribute lines in the tooltip
     *
     * @param tooltip The tooltip to search
     * @param startIndex The index to start searching from
     * @return The index of the first non-attribute line
     */
    private static int findEndOfVanillaAttributes(List<Component> tooltip, int startIndex) {
        for (int i = startIndex; i < tooltip.size(); ++i) {
            String line = tooltip.get(i).getString().toLowerCase();
            if (line.contains("armor") || line.contains("toughness") || line.contains("knockback resistance") ||
                    line.contains("attack damage") || line.contains("attack speed")) continue;
            return i;
        }
        return tooltip.size();
    }

    /**
     * Calculates the final value of an attribute after applying modifiers
     *
     * @param stack The item stack
     * @param attribute The attribute to calculate
     * @param baseValue The base value of the attribute
     * @param modifier The modifier to apply
     * @return The final value of the attribute
     */
    private static double calculateFinalAttributeValue(ItemStack stack, Attribute attribute, double baseValue, Modifier modifier) {
        return AttributeUtils.calculateFinalAttributeValue(stack, attribute, baseValue, modifier);
    }

    /**
     * Gets the base value of an attribute for an item
     *
     * @param stack The item stack
     * @param attribute The attribute to get the base value for
     * @return The base value of the attribute
     */
    private static double getBaseAttributeValue(ItemStack stack, Attribute attribute) {
        return AttributeUtils.getBaseAttributeValue(stack, attribute);
    }
}
