package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.ArsenalAttributes;
import net.serex.upgradedarsenal.modifier.ModifierRegistry;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.EventUtil;
import net.serex.upgradedarsenal.util.PlayerPlacedBlocks;

/**
 * Event handler for the DOUBLE_DROP_CHANCE attribute.
 * Handles events related to double drop chance when breaking blocks.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class DoubleDropChanceEventHandler extends AttributeEventHandler {

    @Override
    public Attribute getAttribute() {
        return ArsenalAttributes.DOUBLE_DROP_CHANCE.get();
    }
    
    /**
     * Event handler for block breaking.
     * Adds a chance to get double drops based on the DOUBLE_DROP_CHANCE attribute.
     */
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
            ModifierRegistry modifier = ModifierHandler.getModifier(heldItem);
            if (modifier == null) return;

            double doubleDropChance = EventUtil.getMinedDropDoubleChance(modifier);
            if (doubleDropChance > 0 && serverLevel.getRandom().nextDouble() < doubleDropChance) {
                Block.dropResources(state, serverLevel, event.getPos(), null, player, heldItem);
            }
        }
    }
}