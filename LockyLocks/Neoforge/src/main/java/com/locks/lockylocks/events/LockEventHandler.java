package com.locks.lockylocks.events;

import com.locks.lockylocks.LockyLocks;
import com.locks.lockylocks.config.Config;
import com.locks.lockylocks.registry.ModTags;
import com.locks.lockylocks.sound.ModSounds;
import com.locks.lockylocks.util.ChestUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Random;

@EventBusSubscriber(modid = LockyLocks.MOD_ID)
public class LockEventHandler {
    private static final ThreadLocal<Boolean> JUST_LOCKED = ThreadLocal.withInitial(() -> false);
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();
        BlockPos pos = event.getHitVec().getBlockPos();
        BlockState state = event.getLevel().getBlockState(pos);
        BlockEntity blockEntity = event.getLevel().getBlockEntity(pos);
        Level level = event.getLevel();


        // Checking if player is holding the Lock and sneaking
        if (!level.isClientSide()){
            if (player.isShiftKeyDown() && itemStack.is(ModTags.Items.LOCKS)) {
                if (blockEntity != null) {
                    CompoundTag tag = blockEntity.getPersistentData();
                    if (!tag.contains("lockylocks_locked")) {
                        String lockName = itemStack.getHoverName().getString();
                        String lockId = lockName + "_" + player.getUUID();
                        String blockName = state.getBlock().getName().getString();
                        float useDurabilityLoss = 1.1f + (RANDOM.nextFloat() * 0.9f);
                        float remainingDurability = itemStack.getMaxDamage() - useDurabilityLoss;


                        tag.putBoolean("lockylocks_locked", true);
                        tag.putString("lockylocks_ownerName", player.getName().getString());
                        tag.putString("lockylocks_ownerId", player.getUUID().toString());
                        tag.putString("lockylocks_name", blockName);
                        tag.putString("lockylocks_id", lockId);
                        tag.putFloat("lockylocks_durability", remainingDurability);
                        tag.putString("lockylocks_type", BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());

                        itemStack.shrink(1);

                        Component locking_message = Component.literal("🔒 ")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(blockName)
                                        .withStyle(ChatFormatting.GREEN))
                                .append(Component.literal(" locked successfully with owner: ")
                                        .withStyle(ChatFormatting.GREEN))
                                .append(Component.literal(tag.getString("lockylocks_ownerName"))
                                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x1F5DEB))));


                        if (state.getBlock() instanceof ChestBlock) {
                            BlockPos secondPos = ChestUtils.getConnectedChest(pos, level);
                            if (secondPos != null) {
                                BlockEntity secondEntity = level.getBlockEntity(secondPos);
                                if (secondEntity != null) {
                                    CompoundTag secondTag = secondEntity.getPersistentData();
                                    if (!secondTag.contains("lockylocks_locked")) {
                                        secondTag.putBoolean("lockylocks_locked", true);
                                        secondTag.putString("lockylocks_ownerName", player.getName().getString());
                                        secondTag.putString("lockylocks_ownerId", player.getUUID().toString());
                                        secondTag.putString("lockylocks_name", blockName);
                                        secondTag.putString("lockylocks_id", lockId);
                                        secondTag.putFloat("lockylocks_durability", remainingDurability);
                                        secondTag.putString("lockylocks_type", BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());

                                        secondEntity.setChanged();
                                        LockDurabilityHandler.addLockedContainer(secondPos);
                                    }
                                }
                            }
                        }

                        blockEntity.setChanged();
                        LockDurabilityHandler.addLockedContainer(pos);
                        JUST_LOCKED.set(true);
                        level.playSound(null, pos, ModSounds.LOCK_USE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                        player.displayClientMessage(locking_message, true);
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        return;
                    }
                }
                return;
            }

            else if (itemStack.is(ModTags.Items.KEYS)) {
                if (blockEntity != null) {
                    CompoundTag tag = blockEntity.getPersistentData();

                    if (tag.getBoolean("lockylocks_locked")) {
                        String keyName = itemStack.getHoverName().getString();
                        String keyId = keyName + "_" + player.getUUID();
                        String lockId = tag.getString("lockylocks_id");
                        String blockName = tag.getString("lockylocks_name");

                        if (keyId.equals(lockId)) {
                            //Drop unlocked Locks
                            float remainingDurabilityFloat = tag.getFloat("lockylocks_durability");
                            int remainingDurability = Math.round(remainingDurabilityFloat);
                            String lockType = tag.getString("lockylocks_type");

                            ItemStack lockDrop = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(lockType)));
                            lockDrop.setDamageValue(lockDrop.getMaxDamage() - remainingDurability);

                            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, lockDrop);
                            level.addFreshEntity(itemEntity);

                            //Unlock Containers
                            tag.remove("lockylocks_locked");
                            tag.remove("lockylocks_ownerName");
                            tag.remove("lockylocks_ownerId");
                            tag.remove("lockylocks_name");
                            tag.remove("lockylocks_id");
                            tag.remove("lockylocks_durability");
                            tag.remove("lockylocks_type");

                            Component unlocking_message = Component.literal("🔓 ")
                                    .withStyle(ChatFormatting.YELLOW)
                                    .append(Component.literal(blockName)
                                            .withStyle(ChatFormatting.GREEN))
                                    .append(Component.literal(" unlocked successfully!")
                                            .withStyle(ChatFormatting.GREEN));


                            if (state.getBlock() instanceof ChestBlock) {
                                BlockPos secondPos = ChestUtils.getConnectedChest(pos, level);
                                if (secondPos != null) {
                                    BlockEntity secondEntity = level.getBlockEntity(secondPos);
                                    if (secondEntity != null) {
                                        CompoundTag secondTag = secondEntity.getPersistentData();
                                        if (keyId.equals(secondTag.getString("lockylocks_id"))) {
                                            secondTag.remove("lockylocks_locked");
                                            secondTag.remove("lockylocks_ownerName");
                                            secondTag.remove("lockylocks_ownerId");
                                            secondTag.remove("lockylocks_name");
                                            secondTag.remove("lockylocks_id");
                                            secondTag.remove("lockylocks_durability");
                                            secondTag.remove("lockylocks_type");

                                            secondEntity.setChanged();
                                            LockDurabilityHandler.removeLockedContainer(secondPos);
                                        }
                                    }
                                }
                            }
                            blockEntity.setChanged();
                            LockDurabilityHandler.removeLockedContainer(pos);
                            level.playSound(null, pos, ModSounds.KEY_USE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                            player.displayClientMessage(unlocking_message, true);
                            event.setCanceled(true);
                            event.setCancellationResult(InteractionResult.SUCCESS);
                            itemStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

                        } else {
                            Component incorrect_key_message = Component.literal("❌ ")
                                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xD11818)))
                                    .append(Component.literal("Key doesn't match the lock!")
                                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFE0000))));

                            player.displayClientMessage(incorrect_key_message, true);
                            event.setCanceled(true);
                            event.setCancellationResult(InteractionResult.FAIL);
                            return;
                        }
                    }
                }
            }

            if (JUST_LOCKED.get()) {
                JUST_LOCKED.set(false);
                return;
            }

            // Block access to locked block for non-owners
            if (blockEntity != null) {
                CompoundTag tag = blockEntity.getPersistentData();
                if (tag.getBoolean("lockylocks_locked")) {
                    String blockName = tag.getString("lockylocks_name");
                    String ownerName = tag.getString("lockylocks_ownerName");

                    String lockId = tag.getString("lockylocks_id");
                    int index = lockId.indexOf('_');
                    String lockName = lockId.substring(0, index);

                    if (!tag.getString("lockylocks_ownerId").equals(player.getUUID().toString())) {

                        Component lock_message = Component.literal("🔐 ")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal("This ")
                                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xF0DE1B))))
                                .append(Component.literal(blockName)
                                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xF0DE1B))))
                                .append(Component.literal(" is locked! ")
                                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xF0DE1B))))
                                .append(Component.literal("Owner: ")
                                        .withStyle(ChatFormatting.WHITE))
                                .append(Component.literal(ownerName)
                                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x1F5DEB))));

                        player.displayClientMessage(lock_message, true);
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.FAIL);
                        return;
                    }

                    if (tag.getString("lockylocks_ownerId").equals(player.getUUID().toString())) {
                        if (itemStack.is(ModTags.Items.LOCKS)) {
                            Component warning_message = Component.literal("⚠️")
                                    .withStyle(ChatFormatting.YELLOW)
                                    .append(Component.literal(" This ")
                                            .withStyle(ChatFormatting.YELLOW))
                                    .append(Component.literal(blockName)
                                            .withStyle(ChatFormatting.YELLOW))
                                    .append(Component.literal(" is already locked!")
                                            .withStyle(ChatFormatting.YELLOW));

                            player.displayClientMessage(warning_message, true);
                            event.setCanceled(true);
                            event.setCancellationResult(InteractionResult.FAIL);
                            return;
                        }
                        if ((!player.getMainHandItem().is(ModTags.Items.LOCKS) && !player.isShiftKeyDown()) || (player.getMainHandItem().isEmpty() && player.isShiftKeyDown())) {
                            Component request_message_1 = Component.literal("Please unlock with your key to access this.")
                                    .withStyle(ChatFormatting.YELLOW)
                                    .append(Component.literal(" Lock Name: ")
                                            .withStyle(ChatFormatting.WHITE))
                                    .append(Component.literal(lockName)
                                            .withStyle(ChatFormatting.RED));


                            player.displayClientMessage(request_message_1, true);
                            event.setCanceled(true);
                            event.setCancellationResult(InteractionResult.FAIL);
                            return;
                        }

                    }
                }
            }
        }
    }

    // If containers are protected from breaking
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();
        BlockEntity blockEntity = event.getLevel().getBlockEntity(pos);
        Level level = (Level) event.getLevel();


        if (!level.isClientSide() && blockEntity != null) {
            CompoundTag tag = blockEntity.getPersistentData();
            if (tag.getBoolean("lockylocks_locked") && Config.PROTECT_LOCKED_CONTAINERS.get()) {

                Component break_protect_message = Component.literal("⚠️ ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal("This ")
                                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xF0DE1B))))
                        .append(Component.literal(tag.getString("lockylocks_name"))
                                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xF0DE1B))))
                        .append(Component.literal(" is locked and protected from breaking!")
                                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xF0DE1B))));

                player.displayClientMessage(break_protect_message, true);
                event.setCanceled(true);
            } else {
                boolean shouldDropLock = true;
                if (blockEntity.getBlockState().getBlock() instanceof ChestBlock) {
                    BlockPos secondPos = ChestUtils.getConnectedChest(pos, level);
                    if (secondPos != null) {
                        shouldDropLock = pos.compareTo(secondPos) < 0;
                    }
                }
                if (shouldDropLock) {
                    float remainingDurabilityFloat = tag.getFloat("lockylocks_durability");
                    int remainingDurability = Math.round(remainingDurabilityFloat);
                    String lockType = tag.getString("lockylocks_type");

                    ItemStack lockDrop = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(lockType)));
                    lockDrop.setDamageValue(lockDrop.getMaxDamage() - remainingDurability);

                    ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, lockDrop);
                    level.addFreshEntity(itemEntity);
                }
            }
        }
    }
}
