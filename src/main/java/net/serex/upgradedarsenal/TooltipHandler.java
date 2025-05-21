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
        updateItemAttributes(stack, tooltip, modifier, whenIndex + 1);

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


    private static String formatAttributeValue(double value, AttributeModifier.Operation operation) {
        return AttributeUtils.formatAttributeValue(value, operation);
    }

    public static String getAttributeNameForRangedWeapon(Attribute attribute) {
        return RANGED_WEAPON_ATTRIBUTE_NAMES.getOrDefault(attribute, null);
    }

    private static String getAttributeTranslationKey(Attribute attribute) {
        return ATTRIBUTE_TRANSLATION_KEYS.getOrDefault(attribute, attribute.getDescriptionId());
    }

    private static void updateItemAttributes(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        // Process vanilla attributes first
        processVanillaAttributes(stack, tooltip, modifier, insertIndex);

        // Then add all additional attributes from the modifier
        addModifierAttributes(stack, tooltip, modifier, insertIndex);
    }

    private static void processVanillaAttributes(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        boolean isRangedWeapon = stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
        boolean isArmor = stack.getItem() instanceof ArmorItem;

        // For ranged weapons, remove attack damage and speed lines as they're not applicable
        if (isRangedWeapon) {
            int i = insertIndex;
            while (i < tooltip.size()) {
                String line = tooltip.get(i).getString().toLowerCase();
                if (line.contains("attack damage") || line.contains("attack speed")) {
                    tooltip.remove(i);
                } else {
                    i++;
                }
            }
        }

        // Update armor attributes if present
        if (isArmor) {
            ArmorItem armorItem = (ArmorItem) stack.getItem();
            updateAttributeLine(tooltip, insertIndex, "armor", 
                calculateFinalAttributeValue(stack, Attributes.ARMOR, armorItem.getDefense(), modifier), "%.1f");
            updateAttributeLine(tooltip, insertIndex, "toughness", 
                calculateFinalAttributeValue(stack, Attributes.ARMOR_TOUGHNESS, armorItem.getToughness(), modifier), "%.1f");
        }

        // Update weapon attributes if this is a melee weapon
        if (!isRangedWeapon && !isArmor) {
            updateAttributeLine(tooltip, insertIndex, "attack damage", 
                calculateFinalAttributeValue(stack, Attributes.ATTACK_DAMAGE, getBaseAttributeValue(stack, Attributes.ATTACK_DAMAGE), modifier), "%.1f");
            updateAttributeLine(tooltip, insertIndex, "attack speed", 
                calculateFinalAttributeValue(stack, Attributes.ATTACK_SPEED, getBaseAttributeValue(stack, Attributes.ATTACK_SPEED), modifier), "%.1f");
        }
    }

    private static void addModifierAttributes(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        // Find the end of vanilla attributes to insert additional modifier attributes
        int modifierInsertIndex = findEndOfVanillaAttributes(tooltip, insertIndex);
        boolean isRangedWeapon = stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
        boolean isArmor = stack.getItem() instanceof ArmorItem;

        for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
            Attribute attribute = entry.getKey().get();
            Modifier.AttributeModifierSupplier supplier = entry.getValue();

            // Skip attributes that are already handled by vanilla tooltip
            if (isVanillaAttribute(attribute, isArmor, isRangedWeapon)) continue;

            // Get the appropriate display name and format for this attribute
            String displayName = null;
            if (isRangedWeapon) {
                displayName = getAttributeNameForRangedWeapon(attribute);
            }

            if (displayName != null) {
                // Use ranged weapon specific formatting
                String formattedValue = formatAttributeValue(supplier.amount, supplier.operation);
                MutableComponent line = Component.literal(formattedValue + " " + displayName)
                        .withStyle(supplier.amount > 0.0 ? ChatFormatting.BLUE : ChatFormatting.RED);
                tooltip.add(modifierInsertIndex++, line);
            } else {
                // Use standard formatting for all other attributes
                String attributeKey = getAttributeTranslationKey(attribute);
                String formattedAmount = formatModifierAmount(supplier);
                ChatFormatting color = getAmountColor(supplier.amount);

                MutableComponent line = Component.literal(formattedAmount + " ")
                        .append(Component.translatable(attributeKey))
                        .withStyle(color);

                tooltip.add(modifierInsertIndex++, line);
            }
        }
    }

    private static boolean isVanillaAttribute(Attribute attribute, boolean isArmor, boolean isRangedWeapon) {
        // Check if this is a vanilla attribute that's already handled by the tooltip
        if ((attribute == Attributes.ARMOR || attribute == Attributes.ARMOR_TOUGHNESS) && isArmor) {
            return true;
        }
        if ((attribute == Attributes.ATTACK_DAMAGE || attribute == Attributes.ATTACK_SPEED) && !isRangedWeapon && !isArmor) {
            return true;
        }
        return false;
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
