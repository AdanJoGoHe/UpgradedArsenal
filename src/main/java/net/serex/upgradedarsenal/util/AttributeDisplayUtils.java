package net.serex.upgradedarsenal.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.modifier.Modifier;

/**
 * Utility class for attribute display-related operations.
 * This class provides methods for getting attribute names, translation keys,
 * and other attribute display-related functionality.
 */
public class AttributeDisplayUtils {
    // Maps for attribute lookups
    public static final Map<Attribute, String> ATTRIBUTE_TRANSLATION_KEYS = new HashMap<>();
    public static final Map<Attribute, String> RANGED_WEAPON_ATTRIBUTE_NAMES = new HashMap<>();
    public static final Map<Attribute, String> BOW_ATTRIBUTE_TRANSLATION_KEYS = new HashMap<>();

    static {
        // Initialize attribute translation keys
        ATTRIBUTE_TRANSLATION_KEYS.put(ModAttributes.MOVEMENT_SPEED.get(), "attribute.upgradedarsenal.movement_speed_increase_percent");
        ATTRIBUTE_TRANSLATION_KEYS.put(ModAttributes.DOUBLE_DROP_CHANCE.get(), "attribute.upgradedarsenal.mined_drop_double_chance_percent");
        ATTRIBUTE_TRANSLATION_KEYS.put(ModAttributes.MINING_SPEED.get(), "attribute.upgradedarsenal.mining_speed_increase_percent");
        ATTRIBUTE_TRANSLATION_KEYS.put(Attributes.ATTACK_DAMAGE, "attribute.upgradedarsenal.attack_damage_percent");
        ATTRIBUTE_TRANSLATION_KEYS.put(Attributes.ATTACK_SPEED, "attribute.upgradedarsenal.attack_speed_percent");
        ATTRIBUTE_TRANSLATION_KEYS.put(Attributes.ARMOR, "attribute.name.generic.armor");
        ATTRIBUTE_TRANSLATION_KEYS.put(Attributes.ARMOR_TOUGHNESS, "attribute.name.generic.armor_toughness");
        ATTRIBUTE_TRANSLATION_KEYS.put(Attributes.MAX_HEALTH, "attribute.name.generic.max_health");

        // Initialize ranged weapon attribute names
        RANGED_WEAPON_ATTRIBUTE_NAMES.put(ModAttributes.DRAW_SPEED.get(), "Draw Speed");
        RANGED_WEAPON_ATTRIBUTE_NAMES.put(ModAttributes.PROJECTILE_VELOCITY.get(), "Arrow Velocity");
        RANGED_WEAPON_ATTRIBUTE_NAMES.put(ModAttributes.PROJECTILE_DAMAGE.get(), "Arrow Damage");
        RANGED_WEAPON_ATTRIBUTE_NAMES.put(ModAttributes.PROJECTILE_ACCURACY.get(), "Accuracy");

        // Initialize bow attribute translation keys
        BOW_ATTRIBUTE_TRANSLATION_KEYS.put(ModAttributes.DRAW_SPEED.get(), "attribute.upgradedarsenal.draw_speed");
        BOW_ATTRIBUTE_TRANSLATION_KEYS.put(ModAttributes.PROJECTILE_VELOCITY.get(), "attribute.upgradedarsenal.projectile_velocity");
        BOW_ATTRIBUTE_TRANSLATION_KEYS.put(ModAttributes.PROJECTILE_DAMAGE.get(), "attribute.upgradedarsenal.projectile_damage");
        BOW_ATTRIBUTE_TRANSLATION_KEYS.put(ModAttributes.PROJECTILE_ACCURACY.get(), "attribute.upgradedarsenal.projectile_accuracy");
    }

    /**
     * Gets the translation key for an attribute
     * 
     * @param attribute The attribute
     * @return The translation key
     */
    public static String getAttributeTranslationKey(Attribute attribute) {
        return ATTRIBUTE_TRANSLATION_KEYS.getOrDefault(attribute, attribute.getDescriptionId());
    }

    /**
     * Gets the display name for a ranged weapon attribute
     * 
     * @param attribute The attribute
     * @return The display name, or null if not a ranged weapon attribute
     */
    public static String getAttributeNameForRangedWeapon(Attribute attribute) {
        return RANGED_WEAPON_ATTRIBUTE_NAMES.getOrDefault(attribute, null);
    }

    /**
     * Gets the translation key for a bow attribute
     * 
     * @param attribute The attribute
     * @return The translation key
     */
    public static String getBowAttributeTranslationKey(Attribute attribute) {
        return BOW_ATTRIBUTE_TRANSLATION_KEYS.getOrDefault(attribute, attribute.getDescriptionId());
    }

    /**
     * Updates armor attributes in the tooltip
     * 
     * @param stack The item stack
     * @param tooltip The tooltip to update
     * @param modifier The modifier to apply
     * @param insertIndex The index to insert the attributes at
     */
    public static void updateArmorAttributes(ItemStack stack, java.util.List<net.minecraft.network.chat.Component> tooltip, Modifier modifier, int insertIndex) {
        ArmorItem armorItem = (ArmorItem) stack.getItem();

        // Define the attributes to update
        Attribute[] attributes = {Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS};
        String[] attributeNames = {"armor", "toughness"};
        double[] baseValues = {armorItem.getDefense(), armorItem.getToughness()};

        // Update each attribute
        for (int i = 0; i < attributes.length; i++) {
            double finalValue = AttributeUtils.calculateFinalAttributeValue(stack, attributes[i], baseValues[i], modifier);
            ComponentUtils.updateAttributeLine(tooltip, insertIndex, attributeNames[i], finalValue, "%.1f");
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
    public static void updateWeaponAttributes(ItemStack stack, java.util.List<net.minecraft.network.chat.Component> tooltip, Modifier modifier, int insertIndex) {
        // Define the attributes to update
        Attribute[] attributes = {Attributes.ATTACK_DAMAGE, Attributes.ATTACK_SPEED};
        String[] attributeNames = {"attack damage", "attack speed"};

        // Update each attribute
        for (int i = 0; i < attributes.length; i++) {
            double baseValue = AttributeUtils.getBaseAttributeValue(stack, attributes[i]);
            double finalValue = AttributeUtils.calculateFinalAttributeValue(stack, attributes[i], baseValue, modifier);
            ComponentUtils.updateAttributeLine(tooltip, insertIndex, attributeNames[i], finalValue, "%.1f");
        }
    }

    /**
     * Updates ranged weapon attributes in the tooltip
     * 
     * @param stack The item stack
     * @param tooltip The tooltip to update
     * @param modifier The modifier to apply
     * @param insertIndex The index to insert the attributes at
     */
    public static void updateRangedAttributes(ItemStack stack, java.util.List<net.minecraft.network.chat.Component> tooltip, Modifier modifier, int insertIndex) {
        // Remove existing attack damage and attack speed lines
        while (insertIndex < tooltip.size()) {
            String line = tooltip.get(insertIndex).getString().toLowerCase();
            if (!line.contains("attack damage") && !line.contains("attack speed")) break;
            tooltip.remove(insertIndex);
        }

        // Add ranged weapon attribute lines
        for (org.apache.commons.lang3.tuple.Pair<java.util.function.Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
            Attribute attribute = entry.getKey().get();
            Modifier.AttributeModifierSupplier supplier = entry.getValue();
            double value = supplier.amount;
            String attributeName = getAttributeNameForRangedWeapon(attribute);

            // Skip attributes that aren't relevant for ranged weapons
            if (attributeName == null) continue;

            // Format and add the attribute line
            String formattedValue = AttributeUtils.formatAttributeValue(value, supplier.operation);
            net.minecraft.network.chat.MutableComponent line = net.minecraft.network.chat.Component.literal(formattedValue + " " + attributeName)
                    .withStyle(value > 0.0 ? net.minecraft.ChatFormatting.BLUE : net.minecraft.ChatFormatting.RED);
            tooltip.add(insertIndex++, line);
        }
    }
}