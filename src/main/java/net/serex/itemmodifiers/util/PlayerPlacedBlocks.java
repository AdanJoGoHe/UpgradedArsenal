package net.serex.itemmodifiers.util;/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.saveddata.SavedData
 *  net.minecraftforge.event.level.BlockEvent$BreakEvent
 *  net.minecraftforge.event.level.BlockEvent$EntityPlaceEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */


import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="itemmodifiers")
public class PlayerPlacedBlocks extends SavedData {
    private static final String DATA_NAME = "itemmodifiers_PlayerPlacedBlocks";
    private final Set<BlockPos> placedBlocks = new HashSet<>();

    public static PlayerPlacedBlocks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(PlayerPlacedBlocks::load, PlayerPlacedBlocks::new, DATA_NAME);
    }

    public void addBlock(BlockPos pos) {
        this.placedBlocks.add(pos);
        this.setDirty();
    }

    public void removeBlock(BlockPos pos) {
        this.placedBlocks.remove(pos);
        this.setDirty();
    }

    public boolean isPlayerPlaced(BlockPos pos) {
        return this.placedBlocks.contains(pos);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag list = new ListTag();
        for (BlockPos pos : this.placedBlocks) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", pos.getX());
            posTag.putInt("Y", pos.getY());
            posTag.putInt("Z", pos.getZ());
            list.add(posTag);
        }
        compound.put("PlacedBlocks", list);
        return compound;
    }

    public static PlayerPlacedBlocks load(CompoundTag compound) {
        PlayerPlacedBlocks tracker = new PlayerPlacedBlocks();
        ListTag list = compound.getList("PlacedBlocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag posTag = list.getCompound(i);
            BlockPos pos = new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));
            tracker.placedBlocks.add(pos);
        }
        return tracker;
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (levelAccessor instanceof ServerLevel serverLevel) {
            PlayerPlacedBlocks tracker = PlayerPlacedBlocks.get(serverLevel);
            tracker.addBlock(event.getPos());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (levelAccessor instanceof ServerLevel serverLevel) {
            PlayerPlacedBlocks tracker = PlayerPlacedBlocks.get(serverLevel);
            tracker.removeBlock(event.getPos());
        }
    }
}


