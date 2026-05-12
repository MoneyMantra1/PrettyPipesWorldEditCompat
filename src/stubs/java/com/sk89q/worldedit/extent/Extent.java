package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public interface Extent {
    <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException;
}
