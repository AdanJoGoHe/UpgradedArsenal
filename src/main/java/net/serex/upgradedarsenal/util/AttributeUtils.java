package net.serex.upgradedarsenal.util;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import net.serex.upgradedarsenal.modifier.ModifierRegistry;


public class AttributeUtils {


    public static double getBaseAttributeValue(ItemStack stack, Attribute attribute) {
        Item item = stack.getItem();

        // Armaduras vanilla
        if ((attribute == Attributes.ARMOR || attribute == Attributes.ARMOR_TOUGHNESS) && item instanceof ArmorItem armorItem) {
            return attribute == Attributes.ARMOR ? armorItem.getDefense() : armorItem.getToughness();
        }

        // Armas vanilla de TieredItem (swords, axes, etc)
        if (attribute == Attributes.ATTACK_DAMAGE && item instanceof TieredItem tiered) {
            float base = tiered.getTier().getAttackDamageBonus();
            if (item instanceof SwordItem) return base + 4.0f;
            if (item instanceof AxeItem) return base + 7.0f;
            if (item instanceof PickaxeItem) return base + 2.0f;
            if (item instanceof ShovelItem) return base + 2.5f;
            return base + 1.0f; // por si hay un item custom de TieredItem
        }

        // Usar default attribute modifiers del item para cubrir TODO lo demás
        Multimap<Attribute, AttributeModifier> defaultMods = item.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
        for (AttributeModifier mod : defaultMods.get(attribute)) {
            if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                return mod.getAmount();
            }
        }

        // Si no, revisar los modifiers del stack por si acaso
        for (AttributeModifier mod : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(attribute)) {
            if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                return mod.getAmount();
            }
        }

        // Ataque velocidad fallback: 4.0 como vanilla
        if (attribute == Attributes.ATTACK_SPEED) {
            return 4.0;
        }

        return 0.0;
    }


    public static double calculateFinalAttributeValue(ItemStack stack, Attribute attribute, double baseValue, ModifierRegistry modifier) {
        double finalValue = baseValue;
        for (Pair<java.util.function.Supplier<Attribute>, ModifierRegistry.AttributeModifierSupplier> entry : modifier.modifiers) {
            if (entry.getKey().get() != attribute) continue;
            ModifierRegistry.AttributeModifierSupplier supplier = entry.getValue();
            switch (supplier.operation) {
                case ADDITION -> finalValue += supplier.amount;
                case MULTIPLY_BASE -> finalValue += baseValue * supplier.amount;
                case MULTIPLY_TOTAL -> finalValue *= 1.0 + supplier.amount;
            }
        }
        return finalValue;
    }
    static String formatAttributeValue(double value, AttributeModifier.Operation operation) {
        return operation == AttributeModifier.Operation.MULTIPLY_TOTAL ?
                String.format("%+d%%", (int)(value * 100.0)) :
                String.format("%+.1f", value);
    }

    public static ResourceLocation createResourceLocation(String path) {
        // This is a temporary solution until we find the correct way to create a ResourceLocation
        // in the current version of Minecraft
        return new ResourceLocation(path);
    }
}
