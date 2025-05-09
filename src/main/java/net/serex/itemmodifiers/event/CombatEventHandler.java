package net.serex.itemmodifiers.event;

import java.util.function.Supplier;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = "itemmodifiers")
public class CombatEventHandler {

        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (!(event.getSource().getEntity() instanceof Player player)) return;

            ItemStack heldItem = player.getMainHandItem();
            if (!ModifierHandler.hasBeenProcessed(heldItem)) return;

            applyAttackDamageModifier(player, heldItem, event);
            applyFireResistanceModifier(player, event);
        }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double jumpBoost = getAttributeValueFromAll(player, ModAttributes.JUMP_HEIGHT.get());
        if (jumpBoost > 1.0) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, motion.y + (0.1 * (jumpBoost - 1.0)), motion.z);
        }
    }

    @SubscribeEvent
    public static void onPlayerTickRegen(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.isCreative()) return;

        Player player = event.player;
        if (player.tickCount % 40 != 0) return;

        handleRegeneration(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        applyRespirationEfficiency(player);
    }

    private static void handleRegeneration(Player player) {
        double regenAmount = getAttributeValueFromAll(player, ModAttributes.REGENERATION.get());
        if (regenAmount > 0.0 && player.getHealth() < player.getMaxHealth()) {
            player.heal((float) regenAmount);
        }
    }

    private static void applyRespirationEfficiency(Player player) {
        if (!player.isUnderWater()) return;

        double efficiency = getAttributeValueFromAll(player, ModAttributes.RESPIRATION_EFFICIENCY.get());
        if (efficiency > 1.0 && player.getAirSupply() < player.getMaxAirSupply()) {
            int restored = (int)((efficiency - 1.0) * 2);
            player.setAirSupply(Math.min(player.getAirSupply() + restored, player.getMaxAirSupply()));
        }
    }

    private static void applyAttackDamageModifier(Player player, ItemStack stack, LivingHurtEvent event) {
            Modifier modifier = ModifierHandler.getModifier(stack);
            if (modifier == null) return;

            for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
                if (entry.getKey().get() == Attributes.ATTACK_DAMAGE) {
                    double modifierAmount = entry.getValue().amount;
                    float adjustedDamage = event.getAmount() * (1.0f + (float) modifierAmount);
                    event.setAmount(adjustedDamage);
                    return;
                }
            }
        }

        private static void applyFireResistanceModifier(Player player, LivingHurtEvent event) {
            if (!event.getSource().is(DamageTypeTags.IS_FIRE)) return;

            double resistance = getAttributeValueFromAll(player, ModAttributes.FIRE_RESISTANCE.get());
            if (resistance > 0) {
                float reduced = event.getAmount() * (1.0f - (float) resistance);
                event.setAmount(reduced);
            }
        }


        public static double getAttributeValueFromAll(Player player, Attribute attribute) {
            double total = 0.0;
            // Main hand and offhand
            for (InteractionHand hand : InteractionHand.values()) {
                Modifier modifier = ModifierHandler.getModifier(player.getItemInHand(hand));
                if (modifier != null) {
                    total += getAttributeValue(modifier, attribute);
                }
            }
            // Armor
            for (ItemStack item : player.getArmorSlots()) {
                Modifier modifier = ModifierHandler.getModifier(item);
                if (modifier != null) {
                    total += getAttributeValue(modifier, attribute);
                }
            }
            return total;
        }

        private static double getAttributeValue(Modifier modifier, Attribute attribute) {
            for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
                if (entry.getKey().get().equals(attribute)) {
                    return entry.getValue().amount;
                }
            }
            return 0.0;
        }
    }