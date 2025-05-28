package net.serex.upgradedarsenal.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.serex.upgradedarsenal.attribute.ArsenalAttributes;
import net.serex.upgradedarsenal.modifier.Modifier;

/**
 * Utility class for attribute display-related operations.
 * This class provides methods for getting attribute names, translation keys,
 * and other attribute display-related functionality.
 */
public class AttributeDisplayUtils {
    // Maps for attribute lookups
    public static final Map<Attribute, String> ATTRIBUTE_TRANSLATION_KEYS = new HashMap<>();

    static {
        // Initialize attribute translation keys
        ATTRIBUTE_TRANSLATION_KEYS.put(ArsenalAttributes.MOVEMENT_SPEED.get(), "attribute.upgradedarsenal.movement_speed_increase_percent");
        ATTRIBUTE_TRANSLATION_KEYS.put(ArsenalAttributes.DOUBLE_DROP_CHANCE.get(), "attribute.upgradedarsenal.mined_drop_double_chance_percent");
        ATTRIBUTE_TRANSLATION_KEYS.put(ArsenalAttributes.MINING_SPEED.get(), "attribute.upgradedarsenal.mining_speed_increase_percent");
        ATTRIBUTE_TRANSLATION_KEYS.put(ArsenalAttributes.MELTING_TOUCH.get(), "attribute.upgradedarsenal.melting_touch_percent");
        ATTRIBUTE_TRANSLATION_KEYS.put(ArsenalAttributes.VEIN_MINER.get(), "attribute.upgradedarsenal.vein_miner_percent");
        ATTRIBUTE_TRANSLATION_KEYS.put(Attributes.ATTACK_DAMAGE, "attribute.upgradedarsenal.attack_damage_percent");
        ATTRIBUTE_TRANSLATION_KEYS.put(Attributes.ATTACK_SPEED, "attribute.upgradedarsenal.attack_speed_percent");
        ATTRIBUTE_TRANSLATION_KEYS.put(Attributes.ARMOR, "attribute.name.generic.armor");
        ATTRIBUTE_TRANSLATION_KEYS.put(Attributes.ARMOR_TOUGHNESS, "attribute.name.generic.armor_toughness");
        ATTRIBUTE_TRANSLATION_KEYS.put(Attributes.MAX_HEALTH, "attribute.name.generic.max_health");
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
}