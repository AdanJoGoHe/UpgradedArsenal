package net.serex.upgradedarsenal.event;

import java.util.*;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.config.CustomConfig;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.PlayerPlacedBlocks;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Consolidated event handler for all item modifier related events.
 */
@Mod.EventBusSubscriber(modid = "upgradedarsenal")
public class ModifierEventHandler {

    // Constants from RangedWeaponEventHandler
    private static final int DEFAULT_BOW_DRAW_TIME = 20;
    private static final int MIN_BOW_DRAW_TIME = 10;
    private static final int MAX_BOW_DRAW_TIME = 100;

    // Constants from GrindstoneHandler
    private static final String REROLL_COUNT_TAG = "upgradedarsenal:reroll_count";

    // Set for allowed blocks
    private static Set<Block> allowedBlocks = new HashSet<>();

    // Methods from ArmorEventHandler
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        EquipmentSlot slot = event.getSlot();
        if (slot.getType() != EquipmentSlot.Type.ARMOR) return;

        ItemStack oldItem = event.getFrom();
        ItemStack newItem = event.getTo();
        ModifierHandler.handleEquipmentChange(player, slot, oldItem, newItem);
    }

    @SubscribeEvent
    public static void onXpGain(PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        double bonus = getAttributeValueFromAll(player, ModAttributes.XP_GAIN_BONUS.get());
        if (bonus > 0) {
            int extra = (int)(event.getAmount() * bonus);
            event.setAmount(event.getAmount() + extra);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        Modifier modifier = ModifierHandler.getModifier(boots);
        if (modifier == null) return;

        AttributeInstance fallRes = player.getAttribute(ModAttributes.FALL_DAMAGE_RESISTANCE.get());
        if (fallRes != null) {
            double value = fallRes.getValue();
            event.setDistance((float) (event.getDistance() * (1.0 - value)));
        }

        AttributeInstance jumpBoost = player.getAttribute(ModAttributes.JUMP_HEIGHT.get());
        if (jumpBoost != null && jumpBoost.getValue() > 0.0) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.05 * jumpBoost.getValue(), 0));
        }

        AttributeInstance healthRegen = player.getAttribute(ModAttributes.REGENERATION.get());
        if (healthRegen != null && player.getHealth() < player.getMaxHealth()) {
            player.heal((float) healthRegen.getValue());
        }
    }

    // Methods from AttributeEventHandler
    @SubscribeEvent
    public static void onPlayerUpdate(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.tickCount % 20 == 0) {
            updatePlayerMovementSpeed(player);
            updatePlayerMaxHealth(player);
        }
    }

    private static void updatePlayerMovementSpeed(Player player) {
        double speedIncrease = getSpeedIncrease(player);
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        applyModifier(movementSpeed, UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635"), "Movement Speed Modifier", speedIncrease, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static void updatePlayerMaxHealth(Player player) {
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

    private static double getAttributeValueFromEquipment(Player player, Attribute attribute) {
        double total = 0.0;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            Modifier modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                total += getAttributeValue(modifier, attribute);
            }
        }
        for (ItemStack stack : player.getArmorSlots()) {
            Modifier modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                total += getAttributeValue(modifier, attribute);
            }
        }
        return total;
    }

    private static void applyModifier(AttributeInstance instance, UUID uuid, String name, double amount, AttributeModifier.Operation operation) {
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

    private static double getMaxAttributeValue(Player player, Attribute attribute) {
        double maxValue = 0.0;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            Modifier modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                double value = getAttributeValue(modifier, attribute);
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }
        for (ItemStack stack : player.getArmorSlots()) {
            Modifier modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                double value = getAttributeValue(modifier, attribute);
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }
        return maxValue;
    }

    private static double getSpeedIncrease(Modifier modifier) {
        return getAttributeValue(modifier, ModAttributes.MOVEMENT_SPEED.get());
    }

    public static void loadAllowedBlocks() {
        if (!allowedBlocks.isEmpty()) return;
        List<? extends String> blockNames = CustomConfig.ALLOWED_DUPLICATION_BLOCKS.get();
        for (String name : blockNames) {
            Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(name));
            if (block != null) allowedBlocks.add(block);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.isCreative()) return;

        BlockState state = event.getState();
        LevelAccessor level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            loadAllowedBlocks();
            if (!allowedBlocks.contains(state.getBlock())) return;

            PlayerPlacedBlocks tracker = PlayerPlacedBlocks.get(serverLevel);
            if (tracker.isPlayerPlaced(event.getPos())) return;

            ItemStack heldItem = player.getMainHandItem();
            Modifier modifier = ModifierHandler.getModifier(heldItem);
            if (modifier == null) return;

            double doubleDropChance = getMinedDropDoubleChance(modifier);
            if (doubleDropChance > 0 && serverLevel.getRandom().nextDouble() < doubleDropChance) {
                Block.dropResources(state, serverLevel, event.getPos(), null, player, heldItem);
            }
        }
    }

    private static double getDurabilityIncrease(Modifier modifier) {
        return getAttributeValue(modifier, ModAttributes.MAX_DURABILITY.get());
    }

    private static double getSpeedIncrease(Player player) {
        double total = 0.0;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            Modifier modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                total += getSpeedIncrease(modifier);
            }
        }
        for (ItemStack stack : player.getArmorSlots()) {
            Modifier modifier = ModifierHandler.getModifier(stack);
            if (modifier != null) {
                total += getSpeedIncrease(modifier);
            }
        }
        return total;
    }

    private static double getMinedDropDoubleChance(Modifier modifier) {
        return getAttributeValue(modifier, ModAttributes.DOUBLE_DROP_CHANCE.get());
    }

    private static double getAttributeValue(Modifier modifier, Attribute attribute) {
        for (Pair<Supplier<Attribute>, Modifier.AttributeModifierSupplier> entry : modifier.modifiers) {
            if (entry.getKey().get().equals(attribute)) {
                return entry.getValue().amount;
            }
        }
        return 0.0;
    }

    // Methods from CombatEventHandler
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

    // Methods from DurabilityEventHandler
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        Modifier modifier = ModifierHandler.getModifier(stack);
        if (modifier != null) {
            double durabilityIncrease = modifier.getDurabilityIncrease();
            if (durabilityIncrease > 1.0) {
                float newSpeed = event.getNewSpeed() * (float) durabilityIncrease;
                event.setNewSpeed(newSpeed);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurtDurability(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            for (ItemStack armorPiece : player.getArmorSlots()) {
                Modifier modifier = ModifierHandler.getModifier(armorPiece);
                if (modifier == null) continue;
                double durabilityIncrease = modifier.getDurabilityIncrease();
                if (durabilityIncrease > 1.0) {
                    int reducedDamage = (int) Math.max(1.0, event.getAmount() / durabilityIncrease);
                    armorPiece.setDamageValue(armorPiece.getDamageValue() + reducedDamage);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDestroyItem(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        Modifier modifier = ModifierHandler.getModifier(result);
        if (modifier != null) {
            double durabilityIncrease = modifier.getDurabilityIncrease();
            if (durabilityIncrease > 1.0) {
                int newMaxDurability = (int) (result.getMaxDamage() * durabilityIncrease);
                ModifierHandler.setMaxDurability(result, newMaxDurability);
            }
        }
    }

    // Methods from RangedWeaponEventHandler
    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player && event.getItem().getItem() instanceof BowItem) {
            ItemStack bow = event.getItem();
            Modifier modifier = ModifierHandler.getModifier(bow);
            float drawSpeedMultiplier = getDrawSpeedMultiplier(modifier);
            int modifiedDrawTime = calculateModifiedDrawTime(drawSpeedMultiplier);

            CompoundTag tag = bow.getOrCreateTag();
            tag.putInt("StartUseTime", event.getDuration());
            tag.putInt("ModifiedDrawTime", modifiedDrawTime);
            tag.putFloat("DrawSpeedMultiplier", drawSpeedMultiplier);
            tag.putFloat("ElapsedTimeF", 0.0f);
            tag.putFloat("DrawProgress", 0.0f);
            tag.putBoolean("IsDrawing", true);
        }
    }

    @SubscribeEvent
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity() instanceof Player player && event.getItem().getItem() instanceof BowItem) {
            ItemStack bow = event.getItem();
            CompoundTag tag = bow.getOrCreateTag();
            if (!tag.getBoolean("IsDrawing")) return;

            int startUseTime = tag.getInt("StartUseTime");
            int modifiedDrawTime = tag.getInt("ModifiedDrawTime");
            float drawSpeedMultiplier = tag.getFloat("DrawSpeedMultiplier");
            float elapsedTimeF = tag.getFloat("ElapsedTimeF");
            elapsedTimeF += 1.0f + drawSpeedMultiplier;
            int elapsedTime = (int) elapsedTimeF;

            float drawProgress = Math.min(1.0f, elapsedTimeF / (float) modifiedDrawTime);
            int newDuration = startUseTime - elapsedTime;
            event.setDuration(newDuration);

            tag.putFloat("ElapsedTimeF", elapsedTimeF);
            tag.putFloat("DrawProgress", drawProgress);

            if (!player.getAbilities().instabuild) {
                player.releaseUsingItem();
            }
        }
    }

    @SubscribeEvent
    public static void onItemUseStop(LivingEntityUseItemEvent.Stop event) {
        if (event.getItem().getItem() instanceof BowItem) {
            resetBowState(event.getItem());
        }
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof BowItem) {
            resetBowState(event.getItem());
        }
    }

    private static void resetBowState(ItemStack bow) {
        CompoundTag tag = bow.getOrCreateTag();
        tag.putFloat("DrawProgress", 0.0f);
        tag.putFloat("ElapsedTimeF", 0.0f);
        tag.putBoolean("IsDrawing", false);
    }

    private static float getDrawSpeedMultiplier(Modifier modifier) {
        if (modifier == null) {
            return 0.0f;
        }
        return (float)modifier.modifiers.stream()
            .filter(pair -> ((Supplier)pair.getKey()).get() == ModAttributes.DRAW_SPEED.get())
            .mapToDouble(pair -> ((Modifier.AttributeModifierSupplier)pair.getValue()).amount)
            .findFirst()
            .orElse(0.0);
    }

    private static int calculateModifiedDrawTime(float drawSpeedMultiplier) {
        int modifiedDrawTime = drawSpeedMultiplier >= 0.0f ? 
            Math.round(DEFAULT_BOW_DRAW_TIME / (1.0f + drawSpeedMultiplier)) : 
            Math.round(DEFAULT_BOW_DRAW_TIME * (1.0f - drawSpeedMultiplier));
        return Math.max(MIN_BOW_DRAW_TIME, Math.min(modifiedDrawTime, MAX_BOW_DRAW_TIME));
    }

    // Methods from ArrowVelocityHandler
    @SubscribeEvent
    public static void onArrowFired(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof AbstractArrow arrow) {
            Entity shooter = arrow.getOwner();
            if (shooter instanceof Player player) {
                ItemStack bowStack = player.getMainHandItem();
                if (bowStack.getItem() instanceof BowItem) {
                    Modifier modifier = ModifierHandler.getModifier(bowStack);
                    if (modifier != null) {
                        float velocityMultiplier = getVelocityMultiplier(modifier);
                        if (velocityMultiplier != 1.0f) {
                            Vec3 motion = arrow.getDeltaMovement();
                            arrow.setDeltaMovement(motion.scale(velocityMultiplier));
                        }
                    }
                }
            }
        }
    }

    private static float getVelocityMultiplier(Modifier modifier) {
        return (float)modifier.modifiers.stream()
            .filter(pair -> ((Supplier)pair.getKey()).get() == ModAttributes.PROJECTILE_VELOCITY.get())
            .mapToDouble(pair -> 1.0 + ((Modifier.AttributeModifierSupplier)pair.getValue()).amount)
            .findFirst()
            .orElse(1.0);
    }

    // Methods from ChestOpenHandler
    @SubscribeEvent
    public static void onChestOpen(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof ChestMenu chestMenu && !event.getEntity().level().isClientSide()) {
            Level level = event.getEntity().level();
            processChestItems(chestMenu, level);
        }
    }

    private static void processChestItems(ChestMenu chestMenu, Level level) {
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

    // Methods from GrindstoneHandler
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();
        BlockState clickedBlock = event.getLevel().getBlockState(event.getPos());

        if (clickedBlock.getBlock() instanceof GrindstoneBlock
                && ModifierHandler.canHaveModifiers(heldItem)
                && ModifierHandler.hasBeenProcessed(heldItem)) {

            int rerollCount = getRerollCount(heldItem);
            int maxRerolls = CustomConfig.MAX_REROLLS.get();
            int xpCost = CustomConfig.REROLL_XP_COST.get();

            if (rerollCount >= maxRerolls) {
                player.displayClientMessage(Component.literal("This item has reached its re-roll limit."), false);
                return;
            }

            if (player.experienceLevel >= xpCost || player.isCreative()) {
                Modifier oldModifier = ModifierHandler.getModifier(heldItem);
                ModifierHandler.processNewItem(heldItem, player, player.getRandom());
                Modifier newModifier = ModifierHandler.getModifier(heldItem);

                if (!player.isCreative()) {
                    player.giveExperienceLevels(-xpCost);
                }

                setRerollCount(heldItem, ++rerollCount);
                String message = String.format("Re-rolled %s to %s (%d re-rolls remaining)",
                        oldModifier.getFormattedName().getString(),
                        newModifier.getFormattedName().getString(),
                        maxRerolls - rerollCount);
                player.displayClientMessage(Component.literal(message), false);
                event.setCanceled(true);
            } else {
                player.displayClientMessage(Component.literal("Not enough XP to re-roll. Need " + xpCost + " levels."), false);
            }
        }
    }

    private static int getRerollCount(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(REROLL_COUNT_TAG);
    }

    private static void setRerollCount(ItemStack stack, int count) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(REROLL_COUNT_TAG, count);
    }

    // Methods from MiningSpeedHandler
    @SubscribeEvent
    public static void onBreakSpeedMining(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();
        Modifier modifier = ModifierHandler.getModifier(heldItem);
        if (ModifierHandler.canHaveModifiers(heldItem) && modifier != null) {
            double speedMultiplier = calculateMiningSpeedMultiplier(modifier);
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
