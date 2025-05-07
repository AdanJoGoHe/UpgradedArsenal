package net.serex.itemmodifiers.modifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.serex.itemmodifiers.attribute.ModAttributes;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Modifier {
    public final ResourceLocation name;
    public final String debugName;
    public final int weight;
    public final ModifierType type;
    public final List<Pair<Supplier<Attribute>, AttributeModifierSupplier>> modifiers;
    public final Rarity rarity;

    private Modifier(ResourceLocation name, String debugName, int weight, ModifierType type, List<Pair<Supplier<Attribute>, AttributeModifierSupplier>> modifiers, Rarity rarity) {
        this.name = name;
        this.debugName = debugName;
        this.weight = weight;
        this.type = type;
        this.modifiers = modifiers;
        this.rarity = rarity;
    }

    public MutableComponent getFormattedName() {
        return Component.translatable("modifier.itemmodifiers." + this.name.getPath())
                .withStyle(this.rarity.getColor());
    }


    public double getDurabilityIncrease() {
        return this.modifiers.stream().filter(pair -> ((Supplier)pair.getKey()).get() == ModAttributes.DURABILITY_INCREASE.get()).mapToDouble(pair -> ((AttributeModifierSupplier)pair.getValue()).amount).sum();
    }

    public static enum ModifierType {
        EQUIPPED,
        HELD;

    }

    public static enum Rarity {
        UNCHANGED(ChatFormatting.GRAY, 50),
        COMMON(ChatFormatting.GRAY, 75),
        UNCOMMON(ChatFormatting.GREEN, 100),
        RARE(ChatFormatting.BLUE, 125),
        EPIC(ChatFormatting.LIGHT_PURPLE, 150),
        LEGENDARY(ChatFormatting.GOLD, 175),
        MYTHIC(ChatFormatting.RED, 200);

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
        private final String debugName;
        private final ModifierType type;
        private final List<Pair<Supplier<Attribute>, AttributeModifierSupplier>> modifiers = new ArrayList<Pair<Supplier<Attribute>, AttributeModifierSupplier>>();
        private Rarity rarity = Rarity.COMMON;

        public ModifierBuilder(ResourceLocation name, String debugName, ModifierType type) {
            this.name = name;
            this.debugName = debugName;
            this.type = type;
        }

        public ModifierBuilder setRarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public ModifierBuilder addModifier(Supplier<Attribute> attribute, AttributeModifierSupplier modifier) {
            this.modifiers.add((Pair<Supplier<Attribute>, AttributeModifierSupplier>)new ImmutablePair(attribute, (Object)modifier));
            return this;
        }

        public Modifier build() {
            int weight = this.rarity.getWeight();
            return new Modifier(this.name, this.debugName, weight, this.type, this.modifiers, this.rarity);
        }
    }
}
