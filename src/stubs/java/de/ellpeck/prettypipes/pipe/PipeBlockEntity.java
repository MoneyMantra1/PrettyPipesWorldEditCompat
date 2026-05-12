package de.ellpeck.prettypipes.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class PipeBlockEntity extends BlockEntity {
    public final ItemStackHandler modules = new ItemStackHandler(3);
    public PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) { super(type, pos, blockState); }
}
