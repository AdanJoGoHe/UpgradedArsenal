package net.serex.itemmodifiers.event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.serex.itemmodifiers.attribute.ModAttributes;
import net.serex.itemmodifiers.config.CustomConfig;
import net.serex.itemmodifiers.modifier.Modifier;
import net.serex.itemmodifiers.modifier.ModifierHandler;
import net.serex.itemmodifiers.util.PlayerPlacedBlocks;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid="itemmodifiers")
public class AttributeEventHandler {
    private static final UUID MOVEMENT_SPEED_MODIFIER_UUID = UUID.fromString("d74fc612-c093-4a42-a146-45c8f3f8babe");
    private static final UUID MAX_HEALTH_MODIFIER_UUID = UUID.fromString("91fc27b3-f61d-4627-9d33-bcb2938e0ddf");


    @SubscribeEvent
    public static void onPlayerUpdate(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        updatePlayerMovementSpeed(player);
        updatePlayerMaxHealth(player);
    }

    private static void updatePlayerMovementSpeed(Player player) {
        double speedIncrease = getMaxAttributeValue(player, ModAttributes.MOVEMENT_SPEED.get());
        AttributeInstance movementAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        applyModifier(movementAttribute, MOVEMENT_SPEED_MODIFIER_UUID, "ItemModifiers movement speed bonus", speedIncrease - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static void updatePlayerMaxHealth(Player player) {
        double extraHealth = getAttributeValueFromEquipment(player, Attributes.MAX_HEALTH);

        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            AttributeModifier existing = maxHealthAttr.getModifier(MAX_HEALTH_MODIFIER_UUID);
            if (existing != null) {
                maxHealthAttr.removeModifier(existing);
            }

            if (extraHealth != 0.0) {
                AttributeModifier mod = new AttributeModifier(
                        MAX_HEALTH_MODIFIER_UUID,
                        "ItemModifiers max health bonus",
                        extraHealth,
                        AttributeModifier.Operation.ADDITION
                );
                maxHealthAttr.addTransientModifier(mod);
            }
        }

        // Cap current health to new max
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static double getAttributeValueFromEquipment(Player player, Attribute attribute) {
        double total = 0.0;

        // Recorre armadura
        for (ItemStack item : player.getArmorSlots()) {
            Modifier modifier = ModifierHandler.getModifier(item);
            if (modifier != null) {
                total += getAttributeValue(modifier, attribute);
            }
        }

        // Recorre ambas manos
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack item = player.getItemInHand(hand);
            Modifier modifier = ModifierHandler.getModifier(item);
            if (modifier != null) {
                total += getAttributeValue(modifier, attribute);
            }
        }

        return total;
    }


    private static void applyModifier(AttributeInstance instance, UUID uuid, String name, double amount, AttributeModifier.Operation operation) {
        if (instance == null) return;

        AttributeModifier existing = instance.getModifier(uuid);
        if (existing != null) {
            instance.removeModifier(existing);
        }
        if (amount != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(uuid, name, amount, operation));
        }
    }

    private static double getMaxAttributeValue(Player player, Attribute attribute) {
        double result = 1.0;
        for (ItemStack item : player.getArmorSlots()) {
            Modifier modifier = ModifierHandler.getModifier(item);
            if (modifier != null) {
                result = Math.max(result, getAttributeValue(modifier, attribute));
            }
        }
        for (InteractionHand hand : InteractionHand.values()) {
            Modifier modifier = ModifierHandler.getModifier(player.getItemInHand(hand));
            if (modifier != null) {
                result = Math.max(result, getAttributeValue(modifier, attribute));
            }
        }
        return result;
    }


    private static double getSpeedIncrease(Modifier modifier) {
        return modifier.modifiers.stream().filter(pair -> ((Supplier)pair.getKey()).get() == ModAttributes.MOVEMENT_SPEED.get()).mapToDouble(pair -> ((Modifier.AttributeModifierSupplier)pair.getValue()).amount).sum();
    }

    private static final Set<ResourceLocation> ALLOWED_BLOCKS = new HashSet<>();

    public static void loadAllowedBlocks() {
        ALLOWED_BLOCKS.clear();
        for (String id : CustomConfig.ALLOWED_DUPLICATION_BLOCKS.get()) {
            ALLOWED_BLOCKS.add(new ResourceLocation(id));
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel serverLevel)) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getMainHandItem();
        Modifier modifier = ModifierHandler.getModifier(tool);
        if (modifier == null) return;

        double doubleChance = getMinedDropDoubleChance(modifier);
        if (doubleChance <= 0.0) return;

        PlayerPlacedBlocks tracker = PlayerPlacedBlocks.get(serverLevel);
        if (tracker.isPlayerPlaced(event.getPos())) return;

        BlockState state = event.getState();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (!ALLOWED_BLOCKS.contains(blockId)) return;

        if (serverLevel.getRandom().nextDouble() < doubleChance) {
            List<ItemStack> drops = Block.getDrops(state, serverLevel, event.getPos(), null, player, tool);
            for (ItemStack drop : drops) {
                Block.popResource(serverLevel, event.getPos(), drop.copy());
            }
        }
    }

    private static double getDurabilityIncrease(Modifier modifier) {
        return AttributeEventHandler.getAttributeValue(modifier, (Attribute)ModAttributes.MAX_DURABILITY.get());
    }

    private static double getSpeedIncrease(Player player) {
        double maxIncrease = 1.0;
        for (ItemStack item : player.getArmorSlots()) {
            Modifier modifier = ModifierHandler.getModifier(item);
            if (modifier != null) {
                maxIncrease = Math.max(maxIncrease, getAttributeValue(modifier, ModAttributes.MOVEMENT_SPEED.get()));
            }
        }
        for (InteractionHand hand : InteractionHand.values()) {
            Modifier modifier = ModifierHandler.getModifier(player.getItemInHand(hand));
            if (modifier != null) {
                maxIncrease = Math.max(maxIncrease, getAttributeValue(modifier, ModAttributes.MOVEMENT_SPEED.get()));
            }
        }
        return maxIncrease;
    }

    private static double getMinedDropDoubleChance(Modifier modifier) {
        return AttributeEventHandler.getAttributeValue(modifier, (Attribute)ModAttributes.DOUBLE_DROP_CHANCE.get()) - 1.0;
    }

    private static double getAttributeValue(Modifier modifier, Attribute attribute) {
        for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
            if (entry.getKey().get().equals(attribute)) {
                double value = entry.getValue().amount;
                // Max health should be additive, not multiplier
                if (attribute.equals(Attributes.MAX_HEALTH)) {
                    return value;
                }
                return 1.0 + value;
            }
        }
        return attribute.equals(Attributes.MAX_HEALTH) ? 0.0 : 1.0;
    }

}

