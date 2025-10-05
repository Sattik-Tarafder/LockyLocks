package com.locks.lockylocks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.core.Direction;

public class ChestUtils {

    public static BlockPos getConnectedChest(BlockPos pos, Level level) {
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof ChestBlock)) return null;

        ChestType type = state.getValue(ChestBlock.TYPE);
        Direction facing = state.getValue(ChestBlock.FACING);

        if (type == ChestType.SINGLE) return null;

        Direction attachedDirection = type == ChestType.RIGHT ? facing.getCounterClockWise() : facing.getClockWise();

        return pos.relative(attachedDirection);
    }
}
