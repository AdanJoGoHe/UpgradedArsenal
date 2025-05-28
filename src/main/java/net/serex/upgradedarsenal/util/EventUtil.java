package net.serex.upgradedarsenal.util;

import java.util.*;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.serex.upgradedarsenal.ArsenalAttributes;
import net.serex.upgradedarsenal.config.CustomConfig;
import net.serex.upgradedarsenal.modifier.ModifierRegistry;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Utility class for event-related helper methods.
 */
public class EventUtil {
    // Constants from RangedWeaponEventHandler
    private static final int DEFAULT_BOW_DRAW_TIME = 20;
    private static final int MIN_BOW_DRAW_TIME = 10;
    private static final int MAX_BOW_DRAW_TIME = 100;

    // Constants from GrindstoneHandler
    private static final String REROLL_COUNT_TAG = "upgradedarsenal:reroll_count";

    // Set for allowed blocks
    private static Set<Block> allowedBlocks = new HashSet<>();

    public static void updatePlayerMovementSpeed(Player player) {
        double speedIncrease = getSpeedIncrease(player);
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        applyModifier(movementSpeed, UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635"), "Movement Speed Modifier", speedIncrease, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public static void updatePlayerMaxHealth(Player player) {
        double maxHealthIncrease = getMaxAttributeValue(player, Attributes.MAX_HEALTH);
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;

        UUID modifierUUID = UUID.fromString("5D6F0BA2-1186-46AC-B896-C61C5CEE99CC");
        AttributeModifier existingModifier = maxHealth.getModifier(modifierUUID);
        double currentModifier = existingModifier != null ? existingModifier.getAmount() : 0.0;

        if (Math.abs(maxHealthIncrease - currentModifier) > 0.01) {
            if (existingModifier != null) {
                maxHealth.removeModifier(modifierUUID);
            }

            if (maxHealthIncrease > 0.0) {
                AttributeModifier modifier = new AttributeModifier(
                        modifierUUID,
                        "Max Health Modifier",
                        maxHealthIncrease,
                        AttributeModifier.Operation.ADDITION
                );
                maxHealth.addPermanentModifier(modifier);
            }

            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    public static double getAttributeValueFromEquipment(Player player, Attribute attribute) {
        double total = 0.0;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            ModifierRegistry modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                total += getAttributeValue(modifier, attribute);
            }
        }
        for (ItemStack stack : player.getArmorSlots()) {
            ModifierRegistry modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                total += getAttributeValue(modifier, attribute);
            }
        }
        return total;
    }

    public static void applyModifier(AttributeInstance instance, UUID uuid, String name, double amount, AttributeModifier.Operation operation) {
        if (instance == null) return;

        AttributeModifier existingModifier = instance.getModifier(uuid);
        if (existingModifier != null && Math.abs(existingModifier.getAmount() - amount) < 0.01) {
            return;
        }

        if (existingModifier != null) {
            instance.removeModifier(uuid);
        }

        if (amount != 0.0) {
            AttributeModifier modifier = new AttributeModifier(uuid, name, amount, operation);
            instance.addPermanentModifier(modifier);
        }
    }

    public static double getMaxAttributeValue(Player player, Attribute attribute) {
        double maxValue = 0.0;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            ModifierRegistry modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                double value = getAttributeValue(modifier, attribute);
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }
        for (ItemStack stack : player.getArmorSlots()) {
            ModifierRegistry modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                double value = getAttributeValue(modifier, attribute);
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }
        return maxValue;
    }

    public static double getSpeedIncrease(ModifierRegistry modifier) {
        return getAttributeValue(modifier, ArsenalAttributes.MOVEMENT_SPEED.get());
    }

    public static void loadAllowedBlocks() {
        if (!allowedBlocks.isEmpty()) return;
        List<? extends String> blockNames = CustomConfig.ALLOWED_DUPLICATION_BLOCKS.get();
        for (String name : blockNames) {
            Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(name));
            if (block != null) allowedBlocks.add(block);
        }
    }

    public static double getDurabilityIncrease(ModifierRegistry modifier) {
        return getAttributeValue(modifier, ArsenalAttributes.MAX_DURABILITY.get());
    }

    public static double getSpeedIncrease(Player player) {
        double total = 0.0;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            ModifierRegistry modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                total += getSpeedIncrease(modifier);
            }
        }
        for (ItemStack stack : player.getArmorSlots()) {
            ModifierRegistry modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                total += getSpeedIncrease(modifier);
            }
        }
        return total;
    }

    public static double getMinedDropDoubleChance(ModifierRegistry modifier) {
        return getAttributeValue(modifier, ArsenalAttributes.DOUBLE_DROP_CHANCE.get());
    }

    public static double getMeltingTouchChance(ModifierRegistry modifier) {
        return getAttributeValue(modifier, ArsenalAttributes.MELTING_TOUCH.get());
    }

    public static double getVeinMinerChance(ModifierRegistry modifier) {
        return getAttributeValue(modifier, ArsenalAttributes.VEIN_MINER.get());
    }

    public static double getAttributeValue(ModifierRegistry modifier, Attribute attribute) {
        for (Pair<Supplier<Attribute>, ModifierRegistry.AttributeModifierSupplier> entry : modifier.modifiers) {
            if (entry.getKey().get().equals(attribute)) {
                return entry.getValue().amount;
            }
        }
        return 0.0;
    }

    public static void handleRegeneration(Player player) {
        double regenAmount = getAttributeValueFromAll(player, ArsenalAttributes.REGENERATION.get());
        if (regenAmount > 0.0 && player.getHealth() < player.getMaxHealth()) {
            player.heal((float) regenAmount);
        }
    }


    public static void applyAttackDamageModifier(ItemStack stack, LivingHurtEvent event) {
        ModifierRegistry modifier = ModifierHandler.getModifier(stack);
        if (modifier == null) return;

        for (Pair<Supplier<Attribute>, ModifierRegistry.AttributeModifierSupplier> entry : modifier.modifiers) {
            if (entry.getKey().get() == Attributes.ATTACK_DAMAGE) {
                double modifierAmount = entry.getValue().amount;
                float adjustedDamage = event.getAmount() * (1.0f + (float) modifierAmount);
                event.setAmount(adjustedDamage);
                return;
            }
        }
    }

    public static void applyFireResistanceModifier(Player player, LivingHurtEvent event) {
        if (!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) return;

        double resistance = getAttributeValueFromAll(player, ArsenalAttributes.FIRE_RESISTANCE.get());
        if (resistance > 0) {
            float reduced = event.getAmount() * (1.0f - (float) resistance);
            event.setAmount(reduced);
        }
    }

    public static double getAttributeValueFromAll(Player player, Attribute attribute) {
        double total = 0.0;
        // Main hand and offhand
        for (InteractionHand hand : InteractionHand.values()) {
            ModifierRegistry modifier = ModifierHandler.getModifier(player.getItemInHand(hand));
            if (modifier != null) {
                total += getAttributeValue(modifier, attribute);
            }
        }
        // Armor
        for (ItemStack item : player.getArmorSlots()) {
            ModifierRegistry modifier = ModifierHandler.getModifier(item);
            if (modifier != null) {
                total += getAttributeValue(modifier, attribute);
            }
        }
        return total;
    }

    public static void processChestItems(ChestMenu chestMenu, Level level) {
        Container container = chestMenu.getContainer();
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty() || !ModifierHandler.canHaveModifiers(stack) || ModifierHandler.hasBeenProcessed(stack)) {
                continue;
            }
            ModifierHandler.processNewItem(stack, null, level.getRandom() );
            container.setItem(i, stack);
        }
    }

    public static int getRerollCount(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(REROLL_COUNT_TAG);
    }

    public static void setRerollCount(ItemStack stack, int count) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(REROLL_COUNT_TAG, count);
    }

    public static double calculateMiningSpeedMultiplier(ModifierRegistry modifier) {
        double multiplier = 1.0;
        for (Pair<Supplier<Attribute>, ModifierRegistry.AttributeModifierSupplier> entry : modifier.modifiers) {
            if (((Supplier)entry.getKey()).get() != ArsenalAttributes.MINING_SPEED.get()) continue;
            ModifierRegistry.AttributeModifierSupplier modifierSupplier = (ModifierRegistry.AttributeModifierSupplier)entry.getValue();
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

    public static Set<Block> getAllowedBlocks() {
        return allowedBlocks;
    }

    /**
     * Applies lifesteal healing to a player based on damage dealt.
     * 
     * @param player The player to heal
     * @param damageDealt The amount of damage dealt
     */
    public static void handleLifesteal(Player player, float damageDealt) {
        double lifestealAmount = getAttributeValueFromAll(player, ArsenalAttributes.LIFESTEAL.get());
        if (lifestealAmount > 0 && player.getHealth() < player.getMaxHealth()) {
            float healAmount = damageDealt * (float)lifestealAmount;
            player.heal(healAmount);
        }
    }

    /**
     * Calculates if a critical hit occurs based on player's critical hit chance.
     * 
     * @param player The player attacking
     * @return true if a critical hit occurs, false otherwise
     */
    public static boolean rollForCriticalHit(Player player) {
        double critChance = getAttributeValueFromAll(player, ArsenalAttributes.CRITICAL_CHANCE.get());
        return critChance > 0 && player.getRandom().nextDouble() < critChance;
    }

    /**
     * Calculates the damage multiplier for a critical hit.
     * 
     * @param player The player attacking
     * @return The damage multiplier (1.0 + critical damage bonus)
     */
    public static float getCriticalDamageMultiplier(Player player) {
        double critDamage = getAttributeValueFromAll(player, ArsenalAttributes.CRITICAL_DAMAGE.get());
        return 1.0f + (float)critDamage;
    }
}
