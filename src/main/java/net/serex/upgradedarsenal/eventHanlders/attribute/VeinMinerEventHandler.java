package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.attribute.ModAttributes;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.ModifierHandler;
import net.serex.upgradedarsenal.util.EventUtil;
import net.serex.upgradedarsenal.util.PlayerPlacedBlocks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Event handler for the VEIN_MINER attribute.
 * Handles events related to mining connected blocks of the same type.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class VeinMinerEventHandler extends AttributeEventHandler {

    // Maximum number of blocks to mine in a single vein mining operation
    private static final int MAX_BLOCKS_TO_MINE = 32;

    @Override
    public Attribute getAttribute() {
        return ModAttributes.VEIN_MINER.get();
    }
    
    /**
     * Event handler for block breaking.
     * Mines connected blocks of the same type based on the VEIN_MINER attribute.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.isCreative()) return;

        BlockState state = event.getState();
        LevelAccessor level = event.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        EventUtil.loadAllowedBlocks();
        if (!EventUtil.getAllowedBlocks().contains(state.getBlock())) return;

        PlayerPlacedBlocks tracker = PlayerPlacedBlocks.get(serverLevel);
        if (tracker.isPlayerPlaced(event.getPos())) return;

        ItemStack heldItem = player.getMainHandItem();
        Modifier modifier = ModifierHandler.getModifier(heldItem);
        if (modifier == null) return;

        double veinMinerChance = EventUtil.getVeinMinerChance(modifier);
        if (veinMinerChance <= 0 || serverLevel.getRandom().nextDouble() >= veinMinerChance) return;

        // Let the original block break normally
        // Then find and break connected blocks of the same type
        BlockPos startPos = event.getPos();
        Block targetBlock = state.getBlock();
        
        // Use BFS to find connected blocks
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        
        // Start with the neighbors of the broken block
        visited.add(startPos);
        addNeighbors(startPos, queue);
        
        int blocksToMine = 0;
        
        while (!queue.isEmpty() && blocksToMine < MAX_BLOCKS_TO_MINE) {
            BlockPos pos = queue.poll();
            
            if (visited.contains(pos)) continue;
            visited.add(pos);
            
            BlockState blockState = serverLevel.getBlockState(pos);
            if (blockState.getBlock() != targetBlock) continue;
            
            // Check if this block was placed by a player
            if (tracker.isPlayerPlaced(pos)) continue;
            
            // Break the block and drop its resources
            serverLevel.destroyBlock(pos, true, player);
            blocksToMine++;
            
            // Add neighbors to the queue
            addNeighbors(pos, queue);
        }
    }
    
    /**
     * Adds the neighboring positions to the queue.
     */
    private static void addNeighbors(BlockPos pos, Queue<BlockPos> queue) {
        queue.add(pos.above());
        queue.add(pos.below());
        queue.add(pos.north());
        queue.add(pos.south());
        queue.add(pos.east());
        queue.add(pos.west());
    }
}