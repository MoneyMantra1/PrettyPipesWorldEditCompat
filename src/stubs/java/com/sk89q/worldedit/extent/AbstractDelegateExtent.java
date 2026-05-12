package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public abstract class AbstractDelegateExtent implements Extent {
    private final Extent extent;
    protected AbstractDelegateExtent(Extent extent) { this.extent = extent; }
    public Extent getExtent() { return this.extent; }
    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        return this.extent.setBlock(location, block);
    }
}
