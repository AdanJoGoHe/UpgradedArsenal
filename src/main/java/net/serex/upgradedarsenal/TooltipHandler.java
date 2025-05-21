package net.serex.upgradedarsenal;

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
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.AttributeDisplayUtils;
import net.serex.upgradedarsenal.util.AttributeUtils;
import net.serex.upgradedarsenal.util.ComponentUtils;
import net.serex.upgradedarsenal.util.TooltipUtils;
import org.apache.commons.lang3.tuple.Pair;

import static net.serex.upgradedarsenal.util.AttributeDisplayUtils.*;

/**
 * Handles the display of tooltips for items with modifiers.
 * This class is responsible for updating item tooltips to show modifier information,
 * including attribute changes, rarity, and other modifier-specific details.
 * It handles different types of items (armor, weapons, bows) differently.
 */
@Mod.EventBusSubscriber(modid="upgradedarsenal", bus=Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        if (stack.hasTag() && stack.getTag().contains("upgradedarsenal:modifier")) {
            Modifier modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                updateItemTooltip(stack, tooltip, modifier);
            }
        }
    }

    private static void updateItemTooltip(ItemStack stack, List<Component> tooltip, Modifier modifier) {
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
        addModifierLines(tooltip, modifier, modifierIndex);
    }

    private static int findWhenIndex(List<Component> tooltip) {
        for (int i = 0; i < tooltip.size(); ++i) {
            String line = tooltip.get(i).getString();
            if (!line.startsWith("When on") && !line.startsWith("When in")) continue;
            return i;
        }
        return -1;
    }

    private static void addModifierLines(List<Component> tooltip, Modifier modifier, int insertIndex) {
        tooltip.add(insertIndex++, Component.empty());
        tooltip.add(insertIndex++, formatModifierTitle(modifier));
            tooltip.addAll(insertIndex++, getFormattedInfoLines(modifier));

    }

    private static Component formatModifierTitle(Modifier modifier) {
        return modifier.getFormattedName().copy()
                .withStyle(modifier.rarity.getColor(), ChatFormatting.UNDERLINE);
    }

    private static List<Component> getFormattedInfoLines(Modifier modifier) {
        List<Component> lines = new ArrayList<>();

        for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
            Attribute attribute = entry.getKey().get();
            Modifier.AttributeModifierSupplier supplier = entry.getValue();

            String attributeKey = getAttributeTranslationKey(attribute);
            String formattedAmount = formatModifierAmount(supplier);
            ChatFormatting color = getAmountColor(supplier.amount);

            MutableComponent line = Component.literal(formattedAmount + " ")
                    .append(Component.translatable(attributeKey))
                    .withStyle(color);

            lines.add(line);
        }

        return lines;
    }

    private static String formatModifierAmount(Modifier.AttributeModifierSupplier supplier) {
        double amount = supplier.amount;
        return supplier.operation == AttributeModifier.Operation.MULTIPLY_TOTAL
                ? String.format("%+d%%", (int)(amount * 100.0))
                : String.format("%+.1f", amount);
    }

    private static ChatFormatting getAmountColor(double amount) {
        return amount > 0.0 ? ChatFormatting.GREEN : ChatFormatting.RED;
    }


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

    private static String getBowAttributeTranslationKey(Attribute attribute) {
        return BOW_ATTRIBUTE_TRANSLATION_KEYS.getOrDefault(attribute, attribute.getDescriptionId());
    }

    private static String formatAttributeValue(double value, AttributeModifier.Operation operation) {
        return AttributeUtils.formatAttributeValue(value, operation);
    }

    public static String getAttributeNameForRangedWeapon(Attribute attribute) {
        return RANGED_WEAPON_ATTRIBUTE_NAMES.getOrDefault(attribute, null);
    }

    private static String getAttributeTranslationKey(Attribute attribute) {
        return ATTRIBUTE_TRANSLATION_KEYS.getOrDefault(attribute, attribute.getDescriptionId());
    }

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

    private static int findEndOfVanillaAttributes(List<Component> tooltip, int startIndex) {
        for (int i = startIndex; i < tooltip.size(); ++i) {
            String line = tooltip.get(i).getString().toLowerCase();
            if (line.contains("armor") || line.contains("toughness") || line.contains("knockback resistance") ||
                    line.contains("attack damage") || line.contains("attack speed")) continue;
            return i;
        }
        return tooltip.size();
    }

    private static double calculateFinalAttributeValue(ItemStack stack, Attribute attribute, double baseValue, Modifier modifier) {
        return AttributeUtils.calculateFinalAttributeValue(stack, attribute, baseValue, modifier);
    }

    private static double getBaseAttributeValue(ItemStack stack, Attribute attribute) {
        return AttributeUtils.getBaseAttributeValue(stack, attribute);
    }
}
