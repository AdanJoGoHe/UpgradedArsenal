package net.serex.itemmodifiers.event;

import java.util.function.Supplier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid="itemmodifiers")
public class MiningSpeedHandler {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();
        Modifier modifier = ModifierHandler.getModifier(heldItem);
        if (ModifierHandler.canHaveModifiers(heldItem) && modifier != null) {
            double speedMultiplier = MiningSpeedHandler.calculateMiningSpeedMultiplier(modifier);
            event.setNewSpeed((float) (event.getNewSpeed() * speedMultiplier));
        }
    }

    private static double calculateMiningSpeedMultiplier(Modifier modifier) {
        double multiplier = 1.0;
        for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
            if (((Supplier)entry.getKey()).get() != ModAttributes.MINING_SPEED.get()) continue;
            Modifier.AttributeModifierSupplier modifierSupplier = (Modifier.AttributeModifierSupplier)entry.getValue();
            if (modifierSupplier.operation == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                multiplier *= 1.0 + modifierSupplier.amount;
                continue;
            }
            if (modifierSupplier.operation == AttributeModifier.Operation.MULTIPLY_BASE) {
                multiplier += modifierSupplier.amount;
                continue;
            }
            if (modifierSupplier.operation != AttributeModifier.Operation.ADDITION) continue;
            multiplier += modifierSupplier.amount;
        }
        return multiplier;
    }
}

