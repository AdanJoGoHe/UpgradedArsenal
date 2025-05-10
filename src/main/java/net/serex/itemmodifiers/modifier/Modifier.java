package net.serex.itemmodifiers.modifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.serex.itemmodifiers.attribute.ModAttributes;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
        return Component.translatable("modifier.itemmodifiers." + name.getPath()).withStyle(baseStyle);
    }





    public double getDurabilityIncrease() {
        return this.modifiers.stream().filter(pair -> ((Supplier)pair.getKey()).get() == ModAttributes.DURABILITY_INCREASE.get()).mapToDouble(pair -> ((AttributeModifierSupplier)pair.getValue()).amount).sum();
    }

    public static enum ModifierType {
        EQUIPPED,
        HELD;

    }

    public static enum Rarity {
        UNCHANGED(ChatFormatting.GRAY, 800),
        COMMON(ChatFormatting.GRAY, 700),
        UNCOMMON(ChatFormatting.GREEN, 400),
        RARE(ChatFormatting.BLUE, 250),
        EPIC(ChatFormatting.LIGHT_PURPLE, 150),
        LEGENDARY(ChatFormatting.GOLD, 100),
        MYTHIC(ChatFormatting.RED, 75),
        HERO(ChatFormatting.DARK_RED, 50);


        private final ChatFormatting color;
        private final int weight;

        private Rarity(ChatFormatting color, int weight) {
            this.color = color;
            this.weight = weight;
        }

        public ChatFormatting getColor() {
            return this.color;
        }

        public int getWeight() {
            return this.weight;
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
