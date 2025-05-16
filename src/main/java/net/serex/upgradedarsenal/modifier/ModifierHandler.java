package net.serex.upgradedarsenal.modifier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.serex.upgradedarsenal.util.AttributeUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid="upgradedarsenal")
public class ModifierHandler {
    private static final Queue<QueuedItem> itemQueue = new ConcurrentLinkedQueue<QueuedItem>();
    public static final String MODIFIER_TAG = "upgradedarsenal:modifier";
    public static final String PROCESSED_TAG = "upgradedarsenal:processed";
    private static final Map<UUID, Map<Attribute, AttributeModifier>> playerModifiers = new HashMap<UUID, Map<Attribute, AttributeModifier>>();

    public static void processNewItem(ItemStack stack, Player player, RandomSource random) {
        if (canHaveModifiers(stack)) {
            ModifierPool pool = getAppropriatePool(stack);
            pool.add(Modifiers.UNCHANGED);
            Modifier selectedModifier = pool.roll(random.fork());
            pool.remove(Modifiers.UNCHANGED);
            if (selectedModifier != null) {
                applyModifier(stack, selectedModifier, player);
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

    public static void applyModifier(ItemStack stack, Modifier newModifier, @Nullable Player player) {
        if (canHaveModifiers(stack)) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("upgradedarsenal:modifier", newModifier.name.toString());

            Multimap<Attribute, AttributeModifier> existingModifiers = HashMultimap.create(stack.getAttributeModifiers(EquipmentSlot.MAINHAND));
            for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : newModifier.modifiers) {
                Attribute attribute = entry.getKey().get();
                Modifier.AttributeModifierSupplier supplier = entry.getValue();
                UUID modifierUUID = UUID.randomUUID();
                String modifierName = "Custom_" + attribute.getDescriptionId() + "_modifier";
                AttributeModifier newModifierInstance = new AttributeModifier(modifierUUID, modifierName, supplier.amount, supplier.operation);
                existingModifiers.put(attribute, newModifierInstance);
            }

            String itemName = stack.getItem().getDescription().getString();
            String modifierName = newModifier.getFormattedName().getString();
            if (!itemName.startsWith(modifierName)) {
                MutableComponent newName = Component.literal(modifierName + " " + itemName).withStyle(newModifier.rarity.getColor());
                stack.setHoverName(newName);
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
        if (modifier != null) {
            String itemName = stack.getItem().getDescriptionId();
            String modifierName = modifier.getFormattedName().getString();
            if (!stack.hasCustomHoverName() || !stack.getHoverName().getString().startsWith(modifierName)) {
                MutableComponent newName = Component.literal(modifierName + " ").append(Component.translatable(itemName))
                        .withStyle(style -> style.withColor(modifier.rarity.getColor()).withItalic(false));
                stack.setHoverName(newName);
            }
        } else {
            // Reset hover name for null modifiers
            stack.resetHoverName();
        }
    }

    private static UUID getModifierUUID(Attribute attribute, EquipmentSlot slot) {
        return UUID.nameUUIDFromBytes((attribute.getDescriptionId() + slot.getName()).getBytes());
    }

    public static Modifier getModifier(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return null;

        CompoundTag tag = stack.getTag();
        if (!tag.contains(MODIFIER_TAG)) return null;

        String rawId = tag.getString(MODIFIER_TAG);
        if (rawId == null || rawId.isEmpty()) {
            System.out.println("[upgradedarsenal] Tag '" + MODIFIER_TAG + "' está presente pero vacío.");
            return null;
        }

        ResourceLocation id;
        try {
            id = AttributeUtils.createResourceLocation(rawId); // tu método existente
        } catch (Exception e) {
            System.out.println("[upgradedarsenal] Error creando ResourceLocation con '" + rawId + "': " + e.getMessage());
            return null;
        }

        Modifier mod = Modifiers.getModifier(id);
        if (mod == null) {
            System.out.println("[upgradedarsenal] Modificador '" + id + "' no está registrado en este lado (¿cliente sin datapack?).");
        }

        return mod;
    }

    public static boolean hasBeenProcessed(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(PROCESSED_TAG);
    }

    public static void markAsProcessed(ItemStack stack) {
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
            applyModifier(stack, modifier != null ? modifier : Modifiers.UNCHANGED, null);
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

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            processQueue();
        }
    }

    private static void processQueue() {
        QueuedItem queuedItem;
        while ((queuedItem = itemQueue.poll()) != null) {
            if (canHaveModifiers(queuedItem.stack) && !hasBeenProcessed(queuedItem.stack)) {
                processNewItem(queuedItem.stack, queuedItem.player, queuedItem.player.getRandom());
            }
            updateItemNameAndColor(queuedItem.stack);
        }
    }

    private static class QueuedItem {
        final ItemStack stack;
        final Player player;

        QueuedItem(ItemStack stack, Player player) {
            this.stack = stack;
            this.player = player;
        }
    }

}
