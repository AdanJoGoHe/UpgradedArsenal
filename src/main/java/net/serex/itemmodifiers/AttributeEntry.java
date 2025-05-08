package net.serex.itemmodifiers;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.serex.itemmodifiers.modifier.Modifier;

import java.util.function.Supplier;

public record AttributeEntry(Supplier<Attribute> attribute, Modifier.AttributeModifierSupplier modifier) {
}
