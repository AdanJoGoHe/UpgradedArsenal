package net.serex.upgradedarsenal;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.serex.upgradedarsenal.modifier.ModifierRegistry;

import java.util.function.Supplier;

public record AttributeEntry(Supplier<Attribute> attribute, ModifierRegistry.AttributeModifierSupplier modifier) {
}
