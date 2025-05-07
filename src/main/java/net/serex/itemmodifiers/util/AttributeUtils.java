package net.serex.itemmodifiers.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import net.serex.itemmodifiers.modifier.Modifier;

/**
 * Utility class for attribute-related operations.
 */
public class AttributeUtils {

    /**
     * Gets the base value of an attribute for an item.
     *
     * @param stack The item stack
     * @param attribute The attribute to get the base value for
     * @return The base value of the attribute
     */
    public static double getBaseAttributeValue(ItemStack stack, Attribute attribute) {
        if (attribute == Attributes.ATTACK_DAMAGE && stack.getItem() instanceof TieredItem item) {
            float base = item.getTier().getAttackDamageBonus();
            if (item instanceof SwordItem) return base + 4.0f;
            if (item instanceof AxeItem) return base + 7.0f;
            if (item instanceof PickaxeItem) return base + 2.0f;
            if (item instanceof ShovelItem) return base + 2.5f;
            return base + 1.0f;
        }
        if (attribute == Attributes.ATTACK_SPEED) {
            double baseSpeed = 4.0;
            for (AttributeModifier mod : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(attribute)) {
                if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                    baseSpeed += mod.getAmount();
                }
            }
            return baseSpeed;
        }
        if ((attribute == Attributes.ARMOR || attribute == Attributes.ARMOR_TOUGHNESS) && stack.getItem() instanceof ArmorItem armorItem) {
            return attribute == Attributes.ARMOR ? armorItem.getDefense() : armorItem.getToughness();
        }
        return 0.0;
    }

    /**
     * Calculates the final value of an attribute after applying modifiers.
     *
     * @param stack The item stack
     * @param attribute The attribute to calculate the final value for
     * @param baseValue The base value of the attribute
     * @param modifier The modifier to apply
     * @return The final value of the attribute
     */
    public static double calculateFinalAttributeValue(ItemStack stack, Attribute attribute, double baseValue, Modifier modifier) {
        double finalValue = baseValue;
        for (Pair<java.util.function.Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
            if (entry.getKey().get() != attribute) continue;
            Modifier.AttributeModifierSupplier supplier = entry.getValue();
            switch (supplier.operation) {
                case ADDITION -> finalValue += supplier.amount;
                case MULTIPLY_BASE -> finalValue += baseValue * supplier.amount;
                case MULTIPLY_TOTAL -> finalValue *= 1.0 + supplier.amount;
            }
        }
        return finalValue;
    }

    /**
     * Formats an attribute value based on the operation.
     *
     * @param value The attribute value
     * @param operation The attribute modifier operation
     * @return The formatted attribute value
     */
    public static String formatAttributeValue(double value, AttributeModifier.Operation operation) {
        return operation == AttributeModifier.Operation.MULTIPLY_TOTAL ?
                String.format("%+d%%", (int)(value * 100.0)) :
                String.format("%+.1f", value);
    }

    /**
     * Creates a ResourceLocation from a string, handling the deprecation of the constructor.
     * This method uses the string directly for now, but can be updated when a new method is available.
     *
     * @param path The path for the ResourceLocation
     * @return A new ResourceLocation
     */
    public static ResourceLocation createResourceLocation(String path) {
        // This is a temporary solution until we find the correct way to create a ResourceLocation
        // in the current version of Minecraft
        return new ResourceLocation(path);
    }
}
