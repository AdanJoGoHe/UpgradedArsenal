package net.serex.upgradedarsenal.event;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import org.apache.commons.lang3.tuple.Pair;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.config.CustomConfig;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.EventUtil;
import net.serex.upgradedarsenal.util.PlayerPlacedBlocks;

/**
 * Consolidated event handler for all item modifier related events.
 */
@Mod.EventBusSubscriber(modid = "upgradedarsenal")
public class ModifierEventHandler {

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
        double bonus = EventUtil.getAttributeValueFromAll(player, ModAttributes.XP_GAIN_BONUS.get());
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
            EventUtil.updatePlayerMovementSpeed(player);
            EventUtil.updatePlayerMaxHealth(player);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.isCreative()) return;

        BlockState state = event.getState();
        LevelAccessor level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            EventUtil.loadAllowedBlocks();
            if (!EventUtil.getAllowedBlocks().contains(state.getBlock())) return;

            PlayerPlacedBlocks tracker = PlayerPlacedBlocks.get(serverLevel);
            if (tracker.isPlayerPlaced(event.getPos())) return;

            ItemStack heldItem = player.getMainHandItem();
            Modifier modifier = ModifierHandler.getModifier(heldItem);
            if (modifier == null) return;

            double doubleDropChance = EventUtil.getMinedDropDoubleChance(modifier);
            if (doubleDropChance > 0 && serverLevel.getRandom().nextDouble() < doubleDropChance) {
                Block.dropResources(state, serverLevel, event.getPos(), null, player, heldItem);
            }
        }
    }

    // Methods from CombatEventHandler
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack heldItem = player.getMainHandItem();
        if (!ModifierHandler.hasBeenProcessed(heldItem)) return;

        EventUtil.applyAttackDamageModifier(player, heldItem, event);
        EventUtil.applyFireResistanceModifier(player, event);
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double jumpBoost = EventUtil.getAttributeValueFromAll(player, ModAttributes.JUMP_HEIGHT.get());
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

        EventUtil.handleRegeneration(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        EventUtil.applyRespirationEfficiency(player);
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
            float drawSpeedMultiplier = EventUtil.getDrawSpeedMultiplier(modifier);
            int modifiedDrawTime = EventUtil.calculateModifiedDrawTime(drawSpeedMultiplier);

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
            EventUtil.resetBowState(event.getItem());
        }
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof BowItem) {
            EventUtil.resetBowState(event.getItem());
        }
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
                        float velocityMultiplier = EventUtil.getVelocityMultiplier(modifier);
                        if (velocityMultiplier != 1.0f) {
                            Vec3 motion = arrow.getDeltaMovement();
                            arrow.setDeltaMovement(motion.scale(velocityMultiplier));
                        }
                    }
                }
            }
        }
    }

    // Methods from ChestOpenHandler
    @SubscribeEvent
    public static void onChestOpen(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof ChestMenu chestMenu && !event.getEntity().level().isClientSide()) {
            Level level = event.getEntity().level();
            EventUtil.processChestItems(chestMenu, level);
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

            int rerollCount = EventUtil.getRerollCount(heldItem);
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

                EventUtil.setRerollCount(heldItem, ++rerollCount);
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

    // Methods from MiningSpeedHandler
    @SubscribeEvent
    public static void onBreakSpeedMining(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();
        Modifier modifier = ModifierHandler.getModifier(heldItem);
        if (ModifierHandler.canHaveModifiers(heldItem) && modifier != null) {
            double speedMultiplier = EventUtil.calculateMiningSpeedMultiplier(modifier);
            event.setNewSpeed((float) (event.getNewSpeed() * speedMultiplier));
        }
    }
}
