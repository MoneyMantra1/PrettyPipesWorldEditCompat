package de.ellpeck.prettypipes.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PipeBlock extends Block {
    protected PipeBlock(Properties properties) { super(properties); }
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos fromPos, boolean isMoving) { throw new AssertionError("stub"); }
    public static void onStateChanged(Level world, BlockPos pos, BlockState newState) { throw new AssertionError("stub"); }
}
