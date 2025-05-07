/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.EquipmentSlot$Type
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ArmorItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent
 *  net.minecraftforge.event.entity.living.LivingFallEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.apache.commons.lang3.tuple.Pair
 */
package net.serex.itemmodifiers.event;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid="itemmodifiers")
public class ArmorEventHandler {
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID TOUGHNESS_MODIFIER_UUID = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");
    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_UUID = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");
    private static final UUID MAX_HEALTH_MODIFIER_UUID = UUID.fromString("5D6F0BA2-1186-46AC-B896-C61C5CEE99CC");
    private static final UUID MOVEMENT_SPEED_MODIFIER_UUID = UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635");
    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private static final UUID LUCK_MODIFIER_UUID = UUID.fromString("03C3C89D-7037-4B42-869F-B146BCB64D2E");

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player player) {
            EquipmentSlot slot = event.getSlot();
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack oldItem = event.getFrom();
                ItemStack newItem = event.getTo();
                ModifierHandler.handleEquipmentChange(player, slot, oldItem, newItem);
            }
        }
    }

    private static void removeArmorModifiers(Player player, ItemStack armorPiece, EquipmentSlot slot) {
        Modifier modifier = ModifierHandler.getModifier(armorPiece);
        if (modifier != null) {
            for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
                Attribute attribute = entry.getKey().get();
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null) {
                    UUID modifierUUID = getModifierUUID(attribute, slot);
                    instance.removeModifier(modifierUUID);
                }
            }
        }
    }

    private static void applyArmorModifiers(Player player, ItemStack armorPiece, EquipmentSlot slot) {
        if (slot.getType() != EquipmentSlot.Type.ARMOR) return;

        Modifier modifier = ModifierHandler.getModifier(armorPiece);
        if (modifier != null) {
            System.out.println("Applying modifiers for: " + armorPiece.getItem().getDescription().getString());
            for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
                Attribute attribute = entry.getKey().get();
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null) {
                    UUID modifierUUID = getModifierUUID(attribute, slot);
                    instance.removeModifier(modifierUUID);
                    AttributeModifier attributeModifier = new AttributeModifier(modifierUUID, "Armor modifier for " + slot.getName(), entry.getValue().amount, entry.getValue().operation);
                    instance.addPermanentModifier(attributeModifier);
                    System.out.println("Applied modifier for attribute: " + attribute.getDescriptionId());
                }
            }
        }
    }

    private static void updateArmorAttributes(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armorPiece = player.getItemBySlot(slot);
                if (!armorPiece.isEmpty()) {
                    ModifierHandler.handleEquipmentChange(player, slot, ItemStack.EMPTY, armorPiece);
                }
            }
        }
    }

    private static void updateAttribute(Player player, Attribute attribute, UUID modifierUUID, String modifierName) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(modifierUUID);
            double totalModifier = 0.0;
            for (ItemStack armorPiece : player.getArmorSlots()) {
                if (armorPiece.getItem() instanceof ArmorItem armorItem) {
                    if (attribute == Attributes.ARMOR) {
                        totalModifier += armorItem.getDefense();
                    } else if (attribute == Attributes.ARMOR_TOUGHNESS) {
                        totalModifier += armorItem.getToughness();
                    }
                }
                Modifier modifier = ModifierHandler.getModifier(armorPiece);
                if (modifier != null) {
                    totalModifier += getModifierValueForAttribute(modifier, attribute);
                }
            }
            if (totalModifier != 0.0) {
                instance.addPermanentModifier(new AttributeModifier(modifierUUID, modifierName, totalModifier, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    private static double getModifierValueForAttribute(Modifier modifier, Attribute attribute) {
        return modifier.modifiers.stream()
                .filter(pair -> pair.getKey().get() == attribute)
                .mapToDouble(pair -> pair.getValue().amount)
                .sum();
    }

    public static UUID getModifierUUID(Attribute attribute, EquipmentSlot slot) {
        return UUID.nameUUIDFromBytes((attribute.getDescriptionId() + slot.getName()).getBytes());
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player player) {
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            Modifier modifier = ModifierHandler.getModifier(boots);
            AttributeInstance fallDamageResistance = player.getAttribute(ModAttributes.FALL_DAMAGE_RESISTANCE.get());
            if (modifier != null && fallDamageResistance != null) {
                double resistanceValue = fallDamageResistance.getValue();
                event.setDistance((float) (event.getDistance() * (1.0 - resistanceValue)));
            }
        }
    }
}

