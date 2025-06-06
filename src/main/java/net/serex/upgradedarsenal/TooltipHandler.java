package net.serex.upgradedarsenal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.modifier.ModifierRegistry;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.AttributeUtils;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid="upgradedarsenal", bus=Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {
    // Maps for attribute lookups
    public static final Map<Attribute, String> ATTRIBUTE_TRANSLATION_KEYS = new HashMap<>();

    // Flag to track if the map has been initialized
    private static boolean attributeKeysInitialized = false;

    /**
     * Initialize the attribute translation keys map.
     * This method should be called after the attributes are registered.
     */
    private static void initAttributeTranslationKeys() {
        if (attributeKeysInitialized) return;

        try {
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

            attributeKeysInitialized = true;
        } catch (Exception e) {
            // If we get an exception (like NullPointerException), we'll try again later
            attributeKeysInitialized = false;
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        // Initialize attribute translation keys if not already done
        initAttributeTranslationKeys();

        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        if (stack.hasTag() && stack.getTag().contains("upgradedarsenal:modifier")) {
            ModifierRegistry modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                updateItemTooltip(stack, tooltip, modifier);
            }
        }
    }

    private static void updateItemTooltip(ItemStack stack, List<Component> tooltip, ModifierRegistry modifier) {
        tooltip.add(1, Component.translatable("rarity." + modifier.rarity.name().toLowerCase())
                .withStyle(style -> style.withColor(modifier.rarity.getColor()).withItalic(false)));

        int whenIndex = findWhenIndex(tooltip);
        updateItemAttributes(stack, tooltip, modifier, whenIndex + 1);

        // Add just the modifier title, not the attributes (which are already added by updateItemAttributes)
        int modifierIndex = findEndOfVanillaAttributes(tooltip, whenIndex + 1);
        tooltip.add(modifierIndex++, Component.empty());
        tooltip.add(modifierIndex, formatModifierTitle(modifier));
    }

    private static int findWhenIndex(List<Component> tooltip) {
        for (int i = 0; i < tooltip.size(); ++i) {
            String line = tooltip.get(i).getString();
            if (!line.startsWith("When on") && !line.startsWith("When in")) continue;
            return i;
        }
        return -1;
    }

    private static void addModifierLines(List<Component> tooltip, ModifierRegistry modifier, int insertIndex) {
        tooltip.add(insertIndex++, Component.empty());
        tooltip.add(insertIndex++, formatModifierTitle(modifier));
            tooltip.addAll(insertIndex++, getFormattedInfoLines(modifier));

    }

    private static Component formatModifierTitle(ModifierRegistry modifier) {
        return modifier.getFormattedName().copy()
                .withStyle(modifier.rarity.getColor(), ChatFormatting.UNDERLINE);
    }

    private static List<Component> getFormattedInfoLines(ModifierRegistry modifier) {
        List<Component> lines = new ArrayList<>();

        for (Pair<Supplier<Attribute>, ModifierRegistry.AttributeModifierSupplier> entry : modifier.modifiers) {
            Attribute attribute = entry.getKey().get();
            ModifierRegistry.AttributeModifierSupplier supplier = entry.getValue();

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

    private static String formatModifierAmount(ModifierRegistry.AttributeModifierSupplier supplier) {
        double amount = supplier.amount;
        return supplier.operation == AttributeModifier.Operation.MULTIPLY_TOTAL
                ? String.format("%+d%%", (int)(amount * 100.0))
                : String.format("%+.1f", amount);
    }

    private static ChatFormatting getAmountColor(double amount) {
        return amount > 0.0 ? ChatFormatting.GREEN : ChatFormatting.RED;
    }

    private static String getAttributeTranslationKey(Attribute attribute) {
        initAttributeTranslationKeys();
        return ATTRIBUTE_TRANSLATION_KEYS.getOrDefault(attribute, attribute.getDescriptionId());
    }

    private static void updateItemAttributes(ItemStack stack, List<Component> tooltip, ModifierRegistry modifier, int insertIndex) {
        // Process vanilla attributes first
        processVanillaAttributes(stack, tooltip, modifier, insertIndex);

        // Then add all additional attributes from the modifier
        addModifierAttributes(stack, tooltip, modifier, insertIndex);
    }

    private static final Map<Attribute, String> VANILLA_ATTRIBUTES = Map.of(
            Attributes.ARMOR, "armor",
            Attributes.ARMOR_TOUGHNESS, "toughness",
            Attributes.ATTACK_DAMAGE, "attack damage",
            Attributes.ATTACK_SPEED, "attack speed"
    );

    private static void processVanillaAttributes(ItemStack stack, List<Component> tooltip, ModifierRegistry modifier, int insertIndex) {
        boolean isArmor = stack.getItem() instanceof ArmorItem;

        for (Map.Entry<Attribute, String> entry : VANILLA_ATTRIBUTES.entrySet()) {
            Attribute attribute = entry.getKey();
            String displayName = entry.getValue();

            // Lógica vanilla: solo mostrar atributos relevantes para el tipo de ítem
            if ((isArmor && (attribute == Attributes.ATTACK_DAMAGE || attribute == Attributes.ATTACK_SPEED)) ||
                    (!isArmor && (attribute == Attributes.ARMOR || attribute == Attributes.ARMOR_TOUGHNESS))) {
                continue;
            }

            double base = getBaseAttributeValue(stack, attribute);
            double value = calculateFinalAttributeValue(stack, attribute, base, modifier);

            // Mostrar cualquier valor distinto de cero
            if (Math.abs(value) > 0.00001) {
                processAttributeLine(tooltip, insertIndex, displayName, value, "%.1f");
            }
        }
    }

    private static void addModifierAttributes(ItemStack stack, List<Component> tooltip, ModifierRegistry modifier, int insertIndex) {
        int modifierInsertIndex = findEndOfVanillaAttributes(tooltip, insertIndex);
        boolean isArmor = stack.getItem() instanceof ArmorItem;

        for (Pair<Supplier<Attribute>, ModifierRegistry.AttributeModifierSupplier> entry : modifier.modifiers) {
            Attribute attribute = entry.getKey().get();
            ModifierRegistry.AttributeModifierSupplier supplier = entry.getValue();

            boolean isVanilla = isVanillaAttribute(attribute, isArmor);

            // For vanilla attributes, check if they're already in the tooltip
            boolean attributeLineExists = false;
            if (isVanilla) {
                String attributeName = "";
                if (attribute == Attributes.ARMOR) attributeName = "armor";
                else if (attribute == Attributes.ARMOR_TOUGHNESS) attributeName = "toughness";
                else if (attribute == Attributes.ATTACK_DAMAGE) attributeName = "attack damage";
                else if (attribute == Attributes.ATTACK_SPEED) attributeName = "attack speed";

                // Check if the attribute line exists in the tooltip
                for (int i = insertIndex; i < tooltip.size(); ++i) {
                    String line = tooltip.get(i).getString().toLowerCase();
                    if (line.contains(attributeName)) {
                        attributeLineExists = true;
                        break;
                    }
                }
            }

            // Skip vanilla attributes that are already in the tooltip
            if (isVanilla && attributeLineExists) continue;

            // Use standard formatting for all attributes
            String attributeKey = getAttributeTranslationKey(attribute);
            String formattedAmount = formatModifierAmount(supplier);
            ChatFormatting color = getAmountColor(supplier.amount);

            MutableComponent line = Component.literal(formattedAmount + " ")
                    .append(Component.translatable(attributeKey))
                    .withStyle(color);

            tooltip.add(modifierInsertIndex++, line);
        }
    }

    private static boolean isVanillaAttribute(Attribute attribute, boolean isArmor) {
        // Check if this is a vanilla attribute that's already handled by the tooltip
        if ((attribute == Attributes.ARMOR || attribute == Attributes.ARMOR_TOUGHNESS) && isArmor) {
            return true;
        }
        if ((attribute == Attributes.ATTACK_DAMAGE || attribute == Attributes.ATTACK_SPEED) && !isArmor) {
            return true;
        }
        return false;
    }

    private static void processAttributeLine(List<Component> tooltip, int startIndex, String attributeName, double value, String format) {
        // First try to update an existing line
        boolean lineUpdated = false;
        for (int i = startIndex; i < tooltip.size(); ++i) {
            String line = tooltip.get(i).getString();
            if (!line.toLowerCase().contains(attributeName)) continue;

            // Replace the numeric value in the line
            String newLine = line.replaceFirst("\\d+(\\.\\d+)?", String.format(format, value));
            MutableComponent newComponent = Component.literal(newLine).withStyle(tooltip.get(i).getStyle());
            tooltip.set(i, newComponent);
            lineUpdated = true;
            break;
        }

        // If no line was updated, add a new line
        if (!lineUpdated) {
            addAttributeLine(tooltip, startIndex, attributeName, value, format);
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

    private static void addAttributeLine(List<Component> tooltip, int insertIndex, String attributeName, double value, String format) {
        // Find the end of vanilla attributes to insert the new line
        int endIndex = findEndOfVanillaAttributes(tooltip, insertIndex);

        // Create a new line with the attribute
        String formattedValue = String.format(format, value);
        String displayName = attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
        MutableComponent line = Component.literal(formattedValue + " " + displayName)
                .withStyle(ChatFormatting.BLUE);

        // Add the line at the end of vanilla attributes
        tooltip.add(endIndex, line);
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

    private static double calculateFinalAttributeValue(ItemStack stack, Attribute attribute, double baseValue, ModifierRegistry modifier) {
        return AttributeUtils.calculateFinalAttributeValue(stack, attribute, baseValue, modifier);
    }

    private static double getBaseAttributeValue(ItemStack stack, Attribute attribute) {
        return AttributeUtils.getBaseAttributeValue(stack, attribute);
    }
}
