package com.moneymantra.ppwecompat;

import de.ellpeck.prettypipes.misc.DirectionSelector;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.PipeBlock;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

final class PrettyPipeEditTracker {
    private static final int PROCESS_DELAY_TICKS = 2;
    private static final Map<BatchKey, PendingBatch> PENDING_BATCHES = new HashMap<>();

    private PrettyPipeEditTracker() {
    }

    static synchronized void recordWorldEditChange(ServerLevel level,
                                                   UUID actorId,
                                                   String actorName,
                                                   BlockPos pos,
                                                   BlockState removedPipeState,
                                                   boolean newIsPipe,
                                                   WorldEditCommandState.PasteContext pasteContext) {
        boolean isPaste = pasteContext != null && pasteContext.paste();
        Rotation rotation = isPaste ? pasteContext.rotation() : Rotation.NONE;
        BatchKey key = new BatchKey(level, actorId, actorName, isPaste, rotation);
        PendingBatch batch = PENDING_BATCHES.computeIfAbsent(key, PendingBatch::new);
        batch.delayTicks = PROCESS_DELAY_TICKS;

        BlockPos immutablePos = pos.immutable();
        if (removedPipeState != null && !newIsPipe) {
            batch.removedPipes.put(immutablePos, removedPipeState);
        }
        if (newIsPipe) {
            batch.placedOrChangedPipes.add(immutablePos);
        }
        addNeighborPositions(batch, immutablePos);
    }

    static synchronized void onServerTick(ServerTickEvent.Post event) {
        Iterator<PendingBatch> iterator = PENDING_BATCHES.values().iterator();
        List<PendingBatch> ready = new ArrayList<>();
        while (iterator.hasNext()) {
            PendingBatch batch = iterator.next();
            batch.delayTicks--;
            if (batch.delayTicks <= 0) {
                ready.add(batch);
                iterator.remove();
            }
        }

        for (PendingBatch batch : ready) {
            process(batch);
        }
    }

    private static void process(PendingBatch batch) {
        ServerLevel level = batch.key.level();
        PipeNetwork network = PipeNetwork.get(level);
        int removed = 0;
        int rebuilt = 0;
        int neighborRefreshed = 0;
        int rotatedModules = 0;

        for (Map.Entry<BlockPos, BlockState> entry : batch.removedPipes.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState oldState = entry.getValue();
            try {
                network.removeNode(pos);
                network.getItemsInPipe(pos).clear();
                network.getActiveCrafts(pos).clear();
                network.getNetworkLocks(pos).clear();
                network.uncachePipe(pos);
                network.onPipeChanged(pos, oldState);
                removed++;
            } catch (Throwable t) {
                PpweCompatMod.LOGGER.warn("Failed to clean removed Pretty Pipes node at {}", pos, t);
            }
        }

        for (BlockPos pos : batch.placedOrChangedPipes) {
            try {
                if (!(level.getBlockState(pos).getBlock() instanceof PipeBlock)) {
                    continue;
                }
                if (batch.key.isPaste() && batch.key.rotation().rotatesDirections()) {
                    rotatedModules += rotateModuleDirections(level, pos, batch.key.rotation());
                }
                refreshPipe(level, pos);
                rebuilt++;
            } catch (Throwable t) {
                PpweCompatMod.LOGGER.warn("Failed to rebuild Pretty Pipes node at {}", pos, t);
            }
        }

        for (BlockPos pos : batch.neighborPipesToRefresh) {
            if (batch.placedOrChangedPipes.contains(pos)) {
                continue;
            }
            try {
                if (level.getBlockState(pos).getBlock() instanceof PipeBlock) {
                    refreshPipe(level, pos);
                    neighborRefreshed++;
                }
            } catch (Throwable t) {
                PpweCompatMod.LOGGER.warn("Failed to refresh neighboring Pretty Pipes node at {}", pos, t);
            }
        }

        try {
            network.clearCaches();
        } catch (Throwable t) {
            PpweCompatMod.LOGGER.warn("Failed to clear Pretty Pipes network caches after WorldEdit edit", t);
        }

        sendSummary(batch, rebuilt, removed, neighborRefreshed, rotatedModules);
    }

    private static void refreshPipe(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof PipeBlock pipeBlock)) {
            return;
        }

        // Recompute connection blockstate first. Pretty Pipes normally does this from neighbor updates,
        // but WorldEdit may place the block and its block entity without firing the same placement path.
        pipeBlock.neighborChanged(state, level, pos, state.getBlock(), pos, false);

        BlockState refreshedState = level.getBlockState(pos);
        if (refreshedState.getBlock() instanceof PipeBlock) {
            PipeBlock.onStateChanged(level, pos, refreshedState);
            PipeNetwork.get(level).uncachePipe(pos);
            level.sendBlockUpdated(pos, refreshedState, refreshedState, 3);
        }
    }

    private static int rotateModuleDirections(ServerLevel level, BlockPos pos, Rotation rotation) {
        if (!(level.getBlockEntity(pos) instanceof PipeBlockEntity pipe)) {
            return 0;
        }

        int changed = 0;
        for (int slot = 0; slot < pipe.modules.getSlots(); slot++) {
            ItemStack stack = pipe.modules.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            DirectionSelector.Data data = stack.get(DirectionSelector.Data.TYPE);
            if (data == null || data.direction() == null || data.direction().isEmpty()) {
                continue;
            }

            Direction original = Direction.byName(data.direction());
            if (original == null) {
                continue;
            }

            Direction rotated = rotation.rotate(original);
            if (rotated != original) {
                stack.set(DirectionSelector.Data.TYPE, new DirectionSelector.Data(rotated.getName()));
                changed++;
            }
        }

        if (changed > 0) {
            pipe.setChanged();
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, 3);
        }
        return changed;
    }

    private static void sendSummary(PendingBatch batch, int rebuilt, int removed, int neighborRefreshed, int rotatedModules) {
        if (rebuilt == 0 && removed == 0 && neighborRefreshed == 0 && rotatedModules == 0) {
            return;
        }

        String action = batch.key.isPaste() ? "WorldEdit paste" : "WorldEdit edit";
        String message = "[PPWE Compat] "
            + "Rebuilt " + rebuilt + " Pretty Pipes, "
            + "cleaned " + removed + " removed pipes, "
            + "refreshed " + neighborRefreshed + " boundary pipes";
        if (batch.key.isPaste() && batch.key.rotation().rotatesDirections()) {
            message += ", rotated " + rotatedModules + " module directions (" + batch.key.rotation().label() + ")";
        }
        message += " after " + action + ".";

        UUID actorId = batch.key.actorId();
        ServerPlayer player = actorId != null ? batch.key.level().getServer().getPlayerList().getPlayer(actorId) : null;
        if (player != null) {
            player.sendSystemMessage(Component.literal(message));
        } else {
            PpweCompatMod.LOGGER.info("{}: {}", batch.key.actorName(), message);
        }
    }

    private static void addNeighborPositions(PendingBatch batch, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            batch.neighborPipesToRefresh.add(pos.relative(direction).immutable());
        }
    }

    private record BatchKey(ServerLevel level, UUID actorId, String actorName, boolean isPaste, Rotation rotation) {
        private BatchKey {
            actorName = actorName == null ? "unknown" : actorName;
            rotation = rotation == null ? Rotation.NONE : rotation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof BatchKey other)) {
                return false;
            }
            return this.isPaste == other.isPaste
                && this.level == other.level
                && Objects.equals(this.actorId, other.actorId)
                && this.rotation == other.rotation;
        }

        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(this.level), this.actorId, this.isPaste, this.rotation);
        }
    }

    private static final class PendingBatch {
        private final BatchKey key;
        private final Set<BlockPos> placedOrChangedPipes = new HashSet<>();
        private final Map<BlockPos, BlockState> removedPipes = new HashMap<>();
        private final Set<BlockPos> neighborPipesToRefresh = new HashSet<>();
        private int delayTicks = PROCESS_DELAY_TICKS;

        private PendingBatch(BatchKey key) {
            this.key = key;
        }
    }
}
