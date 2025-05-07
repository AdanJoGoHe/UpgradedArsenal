package net.serex.itemmodifiers.event;

import java.util.UUID;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;

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

