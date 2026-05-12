package com.moneymantra.ppwecompat;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

final class TrackingExtent extends AbstractDelegateExtent {
    private static final ResourceLocation PRETTY_PIPE_ID = ResourceLocation.fromNamespaceAndPath("prettypipes", "pipe");

    private final ServerLevel level;
    private final Actor actor;
    private final WorldEditCommandState.PasteContext pasteContext;

    TrackingExtent(Extent extent, ServerLevel level, Actor actor, WorldEditCommandState.PasteContext pasteContext) {
        super(extent);
        this.level = level;
        this.actor = actor;
        this.pasteContext = pasteContext;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        BlockPos pos = new BlockPos(location.x(), location.y(), location.z());
        BlockState oldState = this.level.getBlockState(pos);
        boolean oldWasPipe = isPrettyPipe(oldState);
        boolean newIsPipe = isPrettyPipe(block);

        boolean changed = super.setBlock(location, block);

        if (oldWasPipe || newIsPipe) {
            PrettyPipeEditTracker.recordWorldEditChange(
                this.level,
                this.actor.getUniqueId(),
                this.actor.getName(),
                pos,
                oldWasPipe ? oldState : null,
                newIsPipe,
                this.pasteContext
            );
        }

        return changed;
    }

    private static boolean isPrettyPipe(BlockState state) {
        return PRETTY_PIPE_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    private static boolean isPrettyPipe(BlockStateHolder<?> state) {
        return state != null && PRETTY_PIPE_ID.toString().equals(state.getBlockType().id());
    }
}
