package de.ellpeck.prettypipes.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PipeNetwork {
    public static PipeNetwork get(Level level) { throw new AssertionError("stub"); }
    public void removeNode(BlockPos pos) { throw new AssertionError("stub"); }
    public void onPipeChanged(BlockPos pos, BlockState state) { throw new AssertionError("stub"); }
    public void uncachePipe(BlockPos pos) { throw new AssertionError("stub"); }
    public void clearCaches() { throw new AssertionError("stub"); }
    public List<IPipeItem> getItemsInPipe(BlockPos pos) { throw new AssertionError("stub"); }
    public List<ActiveCraft> getActiveCrafts(BlockPos pos) { throw new AssertionError("stub"); }
    public List<NetworkLock> getNetworkLocks(BlockPos pos) { throw new AssertionError("stub"); }
}
