package net.serex.itemmodifiers.modifier;/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.EquipmentSlot$Type
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ArmorItem
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.PickaxeItem
 *  net.minecraft.world.item.ShovelItem
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.TieredItem
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.apache.commons.lang3.tuple.Pair
 */

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;
import net.serex.itemmodifiers.attribute.CustomAttributeModifier;
import org.apache.commons.lang3.tuple.Pair;

public class ModifierHandler {
    public static final String MODIFIER_TAG = "itemmodifiers:modifier";
    public static final String PROCESSED_TAG = "itemmodifiers:processed";
    private static final Map<UUID, Map<Attribute, AttributeModifier>> playerModifiers = new HashMap<UUID, Map<Attribute, AttributeModifier>>();

    public static void processNewItem(ItemStack stack, RandomSource random) {
        if (canHaveModifiers(stack)) {
            ModifierPool pool = getAppropriatePool(stack);
            pool.add(Modifiers.UNCHANGED);
            Modifier selectedModifier = pool.roll(random);
            pool.remove(Modifiers.UNCHANGED);
            if (selectedModifier != null) {
                applyModifier(stack, selectedModifier);
            }
            markAsProcessed(stack);
        }
    }

    public static boolean canHaveModifiers(ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() <= 1 &&
                (stack.getItem() instanceof SwordItem ||
                        stack.getItem() instanceof AxeItem ||
                        stack.getItem() instanceof PickaxeItem ||
                        stack.getItem() instanceof ShovelItem ||
                        stack.getItem() instanceof ArmorItem ||
                        stack.getItem() instanceof BowItem);
    }

    public static void handleEquipmentChange(Player player, EquipmentSlot slot, ItemStack oldItem, ItemStack newItem) {
        if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            if (!oldItem.isEmpty()) {
                removeModifiers(player, oldItem, slot);
            }
            if (!newItem.isEmpty()) {
                applyModifiers(player, newItem, slot);
            }
        }
    }

    private static void removeModifiers(Player player, ItemStack stack, EquipmentSlot slot) {
        Modifier modifier = getModifier(stack);
        if (modifier != null) {
            for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
                Attribute attribute = entry.getKey().get();
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance != null) {
                    UUID modifierUUID = getModifierUUID(attribute, slot);
                    attributeInstance.removeModifier(modifierUUID);
                }
            }
        }
    }

    private static void applyModifiers(Player player, ItemStack stack, EquipmentSlot slot) {
        Modifier modifier = getModifier(stack);
        if (modifier != null) {
            for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
                Attribute attribute = entry.getKey().get();
                Modifier.AttributeModifierSupplier supplier = entry.getValue();
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance != null) {
                    UUID modifierUUID = getModifierUUID(attribute, slot);
                    attributeInstance.removeModifier(modifierUUID);
                    AttributeModifier attributeModifier = new AttributeModifier(modifierUUID, "ArmorModifier", supplier.amount, supplier.operation);
                    attributeInstance.addPermanentModifier(attributeModifier);
                }
            }
        }
    }

    public static void applyModifier(ItemStack stack, Modifier newModifier) {
        if (canHaveModifiers(stack)) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString(MODIFIER_TAG, newModifier.name.toString());

            Multimap<Attribute, AttributeModifier> existingModifiers = HashMultimap.create(stack.getAttributeModifiers(EquipmentSlot.MAINHAND));
            for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : newModifier.modifiers) {
                Attribute attribute = entry.getKey().get();
                Modifier.AttributeModifierSupplier supplier = entry.getValue();
                UUID modifierUUID = UUID.randomUUID();
                String modifierName = "Custom_" + attribute.getDescriptionId() + "_modifier";
                AttributeModifier newModifierInstance = new AttributeModifier(modifierUUID, modifierName, supplier.amount, supplier.operation);
                existingModifiers.put(attribute, newModifierInstance);
            }

            if (newModifier.rarity != Modifier.Rarity.UNCHANGED) {
                String itemName = stack.getItem().getDescription().getString();
                String modifierName = newModifier.getFormattedName().getString();
                if (!itemName.startsWith(modifierName)) {
                    MutableComponent newName = Component.literal(modifierName + " " + itemName).withStyle(newModifier.rarity.getColor());
                    stack.setHoverName(newName);
                }
            } else {
                stack.resetHoverName();
            }

            CompoundTag attributesNBT = new CompoundTag();
            ListTag listNBT = new ListTag();
            for (Map.Entry<Attribute, AttributeModifier> entry : existingModifiers.entries()) {
                CompoundTag attributeNBT = new CompoundTag();
                attributeNBT.putString("AttributeName", ForgeRegistries.ATTRIBUTES.getKey(entry.getKey()).toString());
                attributeNBT.putString("Name", entry.getValue().getName());
                attributeNBT.putDouble("Amount", entry.getValue().getAmount());
                attributeNBT.putInt("Operation", entry.getValue().getOperation().toValue());
                attributeNBT.putUUID("UUID", entry.getValue().getId());
                attributeNBT.putString("Slot", EquipmentSlot.MAINHAND.getName());
                listNBT.add(attributeNBT);
            }
            attributesNBT.put("AttributeModifiers", listNBT);
            stack.getTag().put("AttributeModifiers", attributesNBT);

            markAsProcessed(stack);
        }
    }

    public static void updateItemNameAndColor(ItemStack stack) {
        Modifier modifier = getModifier(stack);
        if (modifier != null && modifier.rarity != Modifier.Rarity.UNCHANGED) {
            String itemName = stack.getItem().getDescriptionId();
            String modifierName = modifier.getFormattedName().getString();
            if (!stack.hasCustomHoverName() || !stack.getHoverName().getString().startsWith(modifierName)) {
                MutableComponent newName = Component.literal(modifierName + " ").append(Component.translatable(itemName))
                        .withStyle(style -> style.withColor(modifier.rarity.getColor()).withItalic(false));
                stack.setHoverName(newName);
            }
        } else {
            stack.resetHoverName();
        }
    }

    private static ListTag serializeAttributeModifiers(Multimap<Attribute, AttributeModifier> modifiers) {
        ListTag list = new ListTag();
        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
            CompoundTag compoundNBT = new CompoundTag();
            compoundNBT.putString("AttributeName", ForgeRegistries.ATTRIBUTES.getKey(entry.getKey()).toString());
            compoundNBT.putString("Name", entry.getValue().getName());
            double amount = entry.getValue() instanceof CustomAttributeModifier custom ? custom.getRawAmount() : entry.getValue().getAmount();
            compoundNBT.putDouble("Amount", amount);
            compoundNBT.putInt("Operation", entry.getValue().getOperation().toValue());
            compoundNBT.putUUID("UUID", entry.getValue().getId());
            compoundNBT.putString("Slot", EquipmentSlot.MAINHAND.getName());
            list.add(compoundNBT);
        }
        return list;
    }

    private static double getBaseAttributeValue(ItemStack stack, Attribute attribute) {
        double baseValue = 0.0;
        if (attribute == Attributes.ATTACK_DAMAGE) {
            if (stack.getItem() instanceof TieredItem tieredItem) {
                baseValue = tieredItem.getTier().getAttackDamageBonus() + 1.0f;
            } else {
                baseValue = 1.0;
            }
        } else if (attribute == Attributes.ATTACK_SPEED) {
            baseValue = 4.0;
        } else if ((attribute == Attributes.ARMOR || attribute == Attributes.ARMOR_TOUGHNESS) && stack.getItem() instanceof ArmorItem armorItem) {
            baseValue = attribute == Attributes.ARMOR ? armorItem.getDefense() : armorItem.getToughness();
        }
        for (AttributeModifier modifier : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(attribute)) {
            if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                baseValue += modifier.getAmount();
            }
        }
        return baseValue;
    }

    private static UUID getModifierUUID(Attribute attribute, EquipmentSlot slot) {
        return UUID.nameUUIDFromBytes((attribute.getDescriptionId() + slot.getName()).getBytes());
    }

    public static Modifier getModifier(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(MODIFIER_TAG)) {
            return Modifiers.getModifier(new ResourceLocation(tag.getString(MODIFIER_TAG)));
        }
        return null;
    }

    public static boolean hasBeenProcessed(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(PROCESSED_TAG);
    }

    private static void markAsProcessed(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(PROCESSED_TAG, true);
    }

    public static void setMaxDurability(ItemStack stack, int newMaxDurability) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("CustomMaxDurability", newMaxDurability);
    }

    public static void processChestLoot(ItemStack stack, RandomSource random, ResourceLocation lootTableId) {
        if (canHaveModifiers(stack) && !hasBeenProcessed(stack)) {
            float rarityChance = random.nextFloat();
            Modifier modifier = rarityChance < 0.4f ? rollModifierWithMinRarity(stack, random, Modifier.Rarity.UNCOMMON)
                    : rarityChance < 0.7f ? rollModifierWithMinRarity(stack, random, Modifier.Rarity.RARE)
                    : rarityChance < 0.9f ? rollModifierWithMinRarity(stack, random, Modifier.Rarity.EPIC)
                    : rollModifierWithMinRarity(stack, random, Modifier.Rarity.LEGENDARY);
            applyModifier(stack, modifier != null ? modifier : Modifiers.UNCHANGED);
            markAsProcessed(stack);
        }
    }

    private static Modifier rollModifierWithMinRarity(ItemStack stack, RandomSource random, Modifier.Rarity minRarity) {
        ModifierPool originalPool = getAppropriatePool(stack);
        if (minRarity.ordinal() <= Modifier.Rarity.COMMON.ordinal()) {
            return originalPool.roll(random);
        }
        ModifierPool filteredPool = new ModifierPool();
        for (Modifier mod : originalPool.getModifiers()) {
            if (mod.rarity.ordinal() >= minRarity.ordinal()) {
                filteredPool.add(mod);
            }
        }
        return filteredPool.roll(random);
    }

    private static ModifierPool getAppropriatePool(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ArmorItem) return Modifiers.ARMOR_POOL;
        if (item instanceof BowItem || item instanceof CrossbowItem) return Modifiers.RANGED_POOL;
        if (item instanceof PickaxeItem || item instanceof ShovelItem || (item instanceof AxeItem && !(item instanceof SwordItem))) {
            return Modifiers.TOOL_POOL;
        }
        return Modifiers.WEAPON_POOL;
    }
}

