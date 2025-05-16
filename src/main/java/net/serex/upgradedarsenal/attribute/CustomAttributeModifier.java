package net.serex.upgradedarsenal.attribute;

import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class CustomAttributeModifier extends AttributeModifier {
    private final double rawAmount;

    public CustomAttributeModifier(UUID uuid, String name, double amount, Operation operation) {
        super(uuid, name, amount, operation);
        this.rawAmount = amount;
    }

    @Override
    public double getAmount() {
        if (getName().startsWith("CustomModifier_")) {
            return this.rawAmount;
        }
        return super.getAmount();
    }
}

