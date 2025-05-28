package net.serex.upgradedarsenal.modifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.serex.upgradedarsenal.attribute.ArsenalAttributes;
import org.apache.commons.lang3.tuple.Pair;

public class Modifier {
    public final ResourceLocation name;
    public final String debugName;
    public final String displayName;
    public final int weight;
    public final ModifierType type;
    public final List<Pair<Supplier<Attribute>, AttributeModifierSupplier>> modifiers;
    public final Rarity rarity;

    private Modifier(ResourceLocation name, String debugName, String displayName ,  int weight, ModifierType type, List<Pair<Supplier<Attribute>, AttributeModifierSupplier>> modifiers, Rarity rarity) {
        this.name = name;
        this.displayName = debugName;
        this.debugName = debugName;
        this.weight = weight;
        this.type = type;
        this.modifiers = modifiers;
        this.rarity = rarity;
    }

    public Component getFormattedName() {
        return getFormattedName(false);
    }

    public Component getFormattedName(boolean bold) {
        Style baseStyle = Style.EMPTY.withColor(rarity.getColor());
        if (bold) baseStyle = baseStyle.withBold(true);
        return Component.translatable("modifier.upgradedarsenal." + name.getPath()).withStyle(baseStyle);
    }

    public double getDurabilityIncrease() {
        return this.modifiers.stream().filter(pair -> ((Supplier)pair.getKey()).get() == ArsenalAttributes.DURABILITY_INCREASE.get()).mapToDouble(pair -> ((AttributeModifierSupplier)pair.getValue()).amount).sum();
    }

    public static enum ModifierType {
        EQUIPPED,
        HELD;
    }

    public static enum Rarity {
        UNCHANGED,
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY,
        MYTHIC,
        HERO;

        // Default values used only if config fails to load
        private static final Map<Rarity, ChatFormatting> DEFAULT_COLORS = Map.of(
            UNCHANGED, ChatFormatting.GRAY,
            COMMON, ChatFormatting.GRAY,
            UNCOMMON, ChatFormatting.GREEN,
            RARE, ChatFormatting.BLUE,
            EPIC, ChatFormatting.LIGHT_PURPLE,
            LEGENDARY, ChatFormatting.GOLD,
            MYTHIC, ChatFormatting.RED,
            HERO, ChatFormatting.DARK_RED
        );

        private static final Map<Rarity, Integer> DEFAULT_WEIGHTS = Map.of(
                UNCHANGED, 67,
                COMMON, 167,
                UNCOMMON, 200,
                RARE, 133,
                EPIC, 100,
                LEGENDARY, 67,
                MYTHIC, 50,
                HERO, 33
        );

        public ChatFormatting getColor() {
            if (net.serex.upgradedarsenal.config.CustomConfigCache.RARITY_CONFIG.containsKey(this)) {
                return net.serex.upgradedarsenal.config.CustomConfigCache.RARITY_CONFIG.get(this).color;
            }
            return DEFAULT_COLORS.getOrDefault(this, ChatFormatting.GRAY);
        }

        public int getWeight() {
            if (net.serex.upgradedarsenal.config.CustomConfigCache.RARITY_CONFIG.containsKey(this)) {
                return net.serex.upgradedarsenal.config.CustomConfigCache.RARITY_CONFIG.get(this).weight;
            }
            return DEFAULT_WEIGHTS.getOrDefault(this, 100);
        }
    }

    public static class AttributeModifierSupplier {
        public final double amount;
        public final AttributeModifier.Operation operation;

        public AttributeModifierSupplier(double amount, AttributeModifier.Operation operation) {
            this.amount = amount;
            this.operation = operation;
        }
    }

    public static class ModifierBuilder {
        private final ResourceLocation name;
        private final ModifierType type;
        private final String debugName;

        private String displayName;
        private int weight;
        private Rarity rarity = Rarity.COMMON;
        private final List<Pair<Supplier<Attribute>, AttributeModifierSupplier>> modifiers = new ArrayList<>();

        public ModifierBuilder(ResourceLocation name, String debugName, ModifierType type) {
            this.name = name;
            this.debugName = debugName;
            this.type = type;
        }

        public ModifierBuilder(Modifier existing) {
            this(existing.name, existing.debugName, existing.type);
            this.displayName = existing.displayName;
            this.weight = existing.weight;
            this.rarity = existing.rarity;
            this.modifiers.addAll(existing.modifiers);
        }

        public ModifierBuilder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public ModifierBuilder setWeight(int weight) {
            this.weight = weight;
            return this;
        }

        public ModifierBuilder setRarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public ModifierBuilder addModifier(Supplier<Attribute> attribute, AttributeModifierSupplier modifier) {
            this.modifiers.add(Pair.of(attribute, modifier));
            return this;
        }

        public Modifier build() {
            int actualWeight = this.weight > 0 ? this.weight : this.rarity.getWeight();
            return new Modifier(this.name, this.debugName, this.displayName,  actualWeight, this.type, this.modifiers, this.rarity);
        }

    }


    public boolean hasAttribute(Attribute... attributes) {
        Set<Attribute> targets = Set.of(attributes);
        return this.modifiers.stream()
                .map(entry -> entry.getKey().get())
                .anyMatch(targets::contains);
    }

}
