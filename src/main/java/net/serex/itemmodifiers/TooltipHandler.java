/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.ArmorItem
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.PickaxeItem
 *  net.minecraft.world.item.ShovelItem
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.TieredItem
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.apache.commons.lang3.tuple.Pair
 */
package net.serex.itemmodifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid="itemmodifiers", bus=Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        if (stack.hasTag() && stack.getTag().contains("itemmodifiers:modifier")) {
            Modifier modifier = ModifierHandler.getModifier(stack);
            if (modifier != null && stack.getItem() instanceof BowItem) {
                updateBowTooltip(stack, tooltip, modifier);
            } else if (modifier != null && !(stack.getItem() instanceof CrossbowItem)) {
                updateTooltip(stack, tooltip, modifier);
            }
        }
    }

    private static void updateTooltip(ItemStack stack, List<Component> tooltip, Modifier modifier) {
        if (modifier.rarity != Modifier.Rarity.UNCHANGED) {
            tooltip.add(1, Component.translatable("rarity." + modifier.rarity.name().toLowerCase())
                    .withStyle(style -> style.withColor(modifier.rarity.getColor()).withItalic(false)));
        }
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


    private static void updateBowTooltip(ItemStack stack, List<Component> tooltip, Modifier modifier) {
        MutableComponent newName = modifier.getFormattedName().append(" ")
                .append(Component.translatable(stack.getItem().getDescriptionId()));
        tooltip.set(0, newName.withStyle(style -> style.withColor(modifier.rarity.getColor())));
        tooltip.add(1, Component.translatable("rarity." + modifier.rarity.name().toLowerCase())
                .withStyle(style -> style.withColor(modifier.rarity.getColor()).withItalic(false)));
        tooltip.add(2, Component.empty());
        addBowAttributes(stack, tooltip, modifier, 3);
        tooltip.add(Component.empty());
        tooltip.add(modifier.getFormattedName().withStyle(style -> style.withColor(modifier.rarity.getColor()).withBold(true)));
        for (Component line : getBowFormattedInfoLines(modifier)) {
            tooltip.add(line);
        }
    }

    private static int findWhenIndex(List<Component> tooltip) {
        for (int i = 0; i < tooltip.size(); ++i) {
            String line = tooltip.get(i).getString();
            if (!line.startsWith("When on") && !line.startsWith("When in")) continue;
            return i;
        }
        return -1;
    }

    private static void addModifierLines(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        tooltip.add(insertIndex++, Component.empty());
        if (modifier.rarity == Modifier.Rarity.UNCHANGED) {
            tooltip.add(insertIndex++, modifier.getFormattedName().copy().withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(insertIndex++, modifier.getFormattedName().copy()
                    .withStyle(modifier.rarity.getColor(), ChatFormatting.UNDERLINE));
            for (Component line : getFormattedInfoLines(modifier)) {
                tooltip.add(insertIndex++, line);
            }
        }
    }

    private static List<Component> getFormattedInfoLines(Modifier modifier) {
        List<Component> lines = new ArrayList<>();
        for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
            Attribute attribute = entry.getKey().get();
            Modifier.AttributeModifierSupplier supplier = entry.getValue();
            double amount = supplier.amount;
            String attributeKey = getAttributeTranslationKey(attribute);
            String formattedAmount = supplier.operation == AttributeModifier.Operation.MULTIPLY_TOTAL ?
                    String.format("%+d%%", (int)(amount * 100.0)) :
                    String.format("%+.1f", amount);
            MutableComponent line = Component.literal(formattedAmount + " ")
                    .append(Component.translatable(attributeKey));
            lines.add(line.withStyle(amount > 0.0 ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        return lines;
    }

    private static void updateRangedAttributes(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        while (insertIndex < tooltip.size()) {
            String line = tooltip.get(insertIndex).getString().toLowerCase();
            if (!line.contains("attack damage") && !line.contains("attack speed")) break;
            tooltip.remove(insertIndex);
        }
        for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
            Attribute attribute = entry.getKey().get();
            Modifier.AttributeModifierSupplier supplier = entry.getValue();
            double value = supplier.amount;
            String attributeName = getAttributeNameForRangedWeapon(attribute);
            if (attributeName == null) continue;
            String formattedValue = formatAttributeValue(value, supplier.operation);
            MutableComponent line = Component.literal(formattedValue + " " + attributeName)
                    .withStyle(value > 0.0 ? ChatFormatting.BLUE : ChatFormatting.RED);
            tooltip.add(insertIndex++, line);
        }
    }

    private static void addBowAttributes(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        double finalDamage = calculateFinalAttributeValue(stack, ModAttributes.PROJECTILE_DAMAGE.get(), 2.0, modifier);
        double finalDrawSpeed = calculateFinalAttributeValue(stack, ModAttributes.DRAW_SPEED.get(), 1.0, modifier);
        double finalVelocity = calculateFinalAttributeValue(stack, ModAttributes.PROJECTILE_VELOCITY.get(), 1.0, modifier);
        double finalAccuracy = calculateFinalAttributeValue(stack, ModAttributes.PROJECTILE_ACCURACY.get(), 1.0, modifier);

        tooltip.add(insertIndex++, Component.literal(String.format("+%.1f Damage", finalDamage)).withStyle(ChatFormatting.BLUE));
        tooltip.add(insertIndex++, Component.literal(String.format("+%.1f Draw Speed", finalDrawSpeed)).withStyle(ChatFormatting.BLUE));
        tooltip.add(insertIndex++, Component.literal(String.format("+%.1f Velocity", finalVelocity)).withStyle(ChatFormatting.BLUE));
        tooltip.add(insertIndex++, Component.literal(String.format("+%.1f Accuracy", finalAccuracy)).withStyle(ChatFormatting.BLUE));
    }

    private static List<Component> getBowFormattedInfoLines(Modifier modifier) {
        List<Component> lines = new ArrayList<>();
        for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
            Attribute attribute = entry.getKey().get();
            double amount = entry.getValue().amount;
            String attributeKey = getBowAttributeTranslationKey(attribute);
            String formattedAmount = String.format("%+d%%", (int)(amount * 100.0));
            MutableComponent line = Component.literal(formattedAmount + " ")
                    .append(Component.translatable(attributeKey));
            lines.add(line.withStyle(amount > 0.0 ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        return lines;
    }

    private static String getBowAttributeTranslationKey(Attribute attribute) {
        if (attribute == ModAttributes.DRAW_SPEED.get()) return "attribute.itemmodifiers.draw_speed";
        if (attribute == ModAttributes.PROJECTILE_VELOCITY.get()) return "attribute.itemmodifiers.projectile_velocity";
        if (attribute == ModAttributes.PROJECTILE_DAMAGE.get()) return "attribute.itemmodifiers.projectile_damage";
        if (attribute == ModAttributes.PROJECTILE_ACCURACY.get()) return "attribute.itemmodifiers.projectile_accuracy";
        return attribute.getDescriptionId();
    }

    private static String formatAttributeValue(double value, AttributeModifier.Operation operation) {
        return operation == AttributeModifier.Operation.MULTIPLY_TOTAL ?
                String.format("%+d%%", (int)(value * 100.0)) :
                String.format("%+.1f", value);
    }

    private static String getAttributeNameForRangedWeapon(Attribute attribute) {
        if (attribute == ModAttributes.DRAW_SPEED.get()) return "Draw Speed";
        if (attribute == ModAttributes.PROJECTILE_VELOCITY.get()) return "Arrow Velocity";
        if (attribute == ModAttributes.PROJECTILE_DAMAGE.get()) return "Arrow Damage";
        if (attribute == ModAttributes.PROJECTILE_ACCURACY.get()) return "Accuracy";
        return null;
    }

    private static String getAttributeTranslationKey(Attribute attribute) {
        if (attribute == ModAttributes.MOVEMENT_SPEED.get()) return "attribute.itemmodifiers.movement_speed_increase_percent";
        if (attribute == ModAttributes.DOUBLE_DROP_CHANCE.get()) return "attribute.itemmodifiers.mined_drop_double_chance_percent";
        if (attribute == ModAttributes.MINING_SPEED.get()) return "attribute.itemmodifiers.mining_speed_increase_percent";
        if (attribute == Attributes.ATTACK_DAMAGE) return "attribute.itemmodifiers.attack_damage_percent";
        if (attribute == Attributes.ATTACK_SPEED) return "attribute.itemmodifiers.attack_speed_percent";
        if (attribute == Attributes.ARMOR) return "attribute.name.generic.armor";
        if (attribute == Attributes.ARMOR_TOUGHNESS) return "attribute.name.generic.armor_toughness";
        if (attribute == Attributes.MAX_HEALTH) return "attribute.name.generic.max_health";
        return attribute.getDescriptionId();
    }

    private static void updateArmorAttributes(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        ArmorItem armorItem = (ArmorItem) stack.getItem();
        double totalArmor = calculateFinalAttributeValue(stack, Attributes.ARMOR, armorItem.getDefense(), modifier);
        double totalToughness = calculateFinalAttributeValue(stack, Attributes.ARMOR_TOUGHNESS, armorItem.getToughness(), modifier);
        updateAttributeLine(tooltip, insertIndex, "armor", totalArmor, "%.1f");
        updateAttributeLine(tooltip, insertIndex, "toughness", totalToughness, "%.1f");
    }

    private static void updateWeaponAttributes(ItemStack stack, List<Component> tooltip, Modifier modifier, int insertIndex) {
        double baseDamage = getBaseAttributeValue(stack, Attributes.ATTACK_DAMAGE);
        double baseAttackSpeed = getBaseAttributeValue(stack, Attributes.ATTACK_SPEED);
        double finalDamage = calculateFinalAttributeValue(stack, Attributes.ATTACK_DAMAGE, baseDamage, modifier);
        double finalAttackSpeed = calculateFinalAttributeValue(stack, Attributes.ATTACK_SPEED, baseAttackSpeed, modifier);
        updateAttributeLine(tooltip, insertIndex, "attack damage", finalDamage, "%.1f");
        updateAttributeLine(tooltip, insertIndex, "attack speed", finalAttackSpeed, "%.1f");
    }

    private static void updateAttributeLine(List<Component> tooltip, int startIndex, String attributeName, double value, String format) {
        for (int i = startIndex; i < tooltip.size(); ++i) {
            String line = tooltip.get(i).getString();
            if (!line.toLowerCase().contains(attributeName)) continue;
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
        double finalValue = baseValue;
        for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
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

    private static double getBaseAttributeValue(ItemStack stack, Attribute attribute) {
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
}

