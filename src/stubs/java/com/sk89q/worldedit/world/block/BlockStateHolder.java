package com.sk89q.worldedit.world.block;

public interface BlockStateHolder<B extends BlockStateHolder<B>> {
    BlockType getBlockType();
}
