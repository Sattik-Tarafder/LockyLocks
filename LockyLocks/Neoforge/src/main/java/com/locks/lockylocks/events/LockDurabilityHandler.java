package com.locks.lockylocks.events;

import com.locks.lockylocks.LockyLocks;
import com.locks.lockylocks.util.ChestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@EventBusSubscriber(modid = LockyLocks.MOD_ID)
public class LockDurabilityHandler {
    private static final Set<BlockPos> LOCKED_CONTAINERS = new HashSet<>();
    private static final Random RANDOM = new Random();
    private static long lastDayChecked = -1;

    // Add a container to tracking when locked
    public static void addLockedContainer(BlockPos pos) {
        LOCKED_CONTAINERS.add(pos.immutable());
    }

    // Remove a container when unlocked
    public static void removeLockedContainer(BlockPos pos) {
        LOCKED_CONTAINERS.remove(pos);
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        LOCKED_CONTAINERS.clear(); // Reset on server start
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        LOCKED_CONTAINERS.clear(); // Clean up on server stop
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel) {
            LOCKED_CONTAINERS.removeIf(pos -> ((ServerLevel) event.getLevel()).dimension().location().toString().equals(pos.toString()));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD); // Use overworld for time
        if (level == null) return;

        long currentTime = level.getDayTime();
        long currentDay = currentTime / 24000;

        if (currentDay > lastDayChecked) {
            lastDayChecked = currentDay;
            updateLockDurability(level);
        }
    }

    private static void updateLockDurability(ServerLevel level) {
        Set<BlockPos> toRemove = new HashSet<>();
        for (BlockPos pos : LOCKED_CONTAINERS) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                CompoundTag tag = blockEntity.getPersistentData();
                if (tag.getBoolean("lockylocks_locked")) {
                    float currentDurability = tag.getFloat("lockylocks_durability");
                    float dailyDurabilityLoss = 3.0f + (RANDOM.nextFloat() * 2.0f);
                    currentDurability -= dailyDurabilityLoss;

                    if (currentDurability <= 0.0f) {
                        // Unlock container
                        tag.remove("lockylocks_locked");
                        tag.remove("lockylocks_ownerName");
                        tag.remove("lockylocks_ownerId");
                        tag.remove("lockylocks_name");
                        tag.remove("lockylocks_id");
                        tag.remove("lockylocks_durability");
                        tag.remove("lockylocks_item");
                        toRemove.add(pos);
                        blockEntity.setChanged();

                        // Handle double chests
                        if (level.getBlockState(pos).getBlock() instanceof ChestBlock) {
                            BlockPos secondPos = ChestUtils.getConnectedChest(pos, level);
                            if (secondPos != null) {
                                BlockEntity secondEntity = level.getBlockEntity(secondPos);
                                if (secondEntity != null) {
                                    CompoundTag secondTag = secondEntity.getPersistentData();
                                    if (secondTag.getBoolean("lockylocks_locked")) {
                                        secondTag.remove("lockylocks_locked");
                                        secondTag.remove("lockylocks_ownerName");
                                        secondTag.remove("lockylocks_ownerId");
                                        secondTag.remove("lockylocks_name");
                                        secondTag.remove("lockylocks_id");
                                        secondTag.remove("lockylocks_durability");
                                        secondTag.remove("lockylocks_item");
                                        toRemove.add(secondPos);
                                        secondEntity.setChanged();
                                    }
                                }
                            }
                        }
                    } else {
                        tag.putFloat("lockylocks_durability", currentDurability);
                        blockEntity.setChanged();

                        // Sync durability to connected chest
                        if (level.getBlockState(pos).getBlock() instanceof ChestBlock) {
                            BlockPos secondPos = ChestUtils.getConnectedChest(pos, level);
                            if (secondPos != null) {
                                BlockEntity secondEntity = level.getBlockEntity(secondPos);
                                if (secondEntity != null) {
                                    CompoundTag secondTag = secondEntity.getPersistentData();
                                    if (secondTag.getBoolean("lockylocks_locked")) {
                                        secondTag.putFloat("lockylocks_durability", currentDurability);
                                        secondEntity.setChanged();
                                    }
                                }
                            }
                        }
                    }
                } else {
                    toRemove.add(pos); // Clean up invalid entries
                }
            } else {
                toRemove.add(pos); // Clean up if block entity is gone
            }
        }
        LOCKED_CONTAINERS.removeAll(toRemove);
    }
}