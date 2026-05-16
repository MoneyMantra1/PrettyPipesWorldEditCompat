package com.moneymantra.ppwecompat;

import de.ellpeck.prettypipes.misc.ItemFilter;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.pipe.modules.filter.FilterIncreaseModuleItem;
import de.ellpeck.prettypipes.pipe.modules.insertion.FilterModuleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FilterWandFeature {
    private static final String TOOL_TAG = "ppwecompat_filter_wand";
    private static final Map<UUID, ItemStack> CLIPBOARDS = new ConcurrentHashMap<>();

    private FilterWandFeature() {
    }

    public static ItemStack createTool(int amount) {
        ItemStack stack = new ItemStack(Items.STICK, amount);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TOOL_TAG, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Pipe Filter Wand").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("Copy an item from an item frame.").withStyle(ChatFormatting.GRAY),
            Component.literal("Apply it to a Pretty Pipes filter module.").withStyle(ChatFormatting.GRAY),
            Component.literal("Operator utility item.").withStyle(ChatFormatting.DARK_GRAY)
        )));
        return stack;
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer) || !serverPlayer.hasPermissions(2)) {
            return;
        }
        InteractionHand hand = event.getHand();
        ItemStack held = player.getItemInHand(hand);
        if (!isTool(held) || !(event.getTarget() instanceof ItemFrame frame)) {
            return;
        }

        ItemStack framed = frame.getItem();
        if (framed.isEmpty()) {
            serverPlayer.sendSystemMessage(PpweMessages.filterWand(
                PpweMessages.warning("That item frame is empty.")
            ));
            cancelSuccess(event);
            return;
        }

        ItemStack copied = framed.copy();
        copied.setCount(1);
        CLIPBOARDS.put(serverPlayer.getUUID(), copied);
        serverPlayer.sendSystemMessage(PpweMessages.filterWand(
            PpweMessages.copied("Copied ")
                .append(PpweMessages.itemName(copied))
                .append(PpweMessages.copied(" to the clipboard."))
        ));
        cancelSuccess(event);
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer) || !serverPlayer.hasPermissions(2)) {
            return;
        }
        InteractionHand hand = event.getHand();
        ItemStack held = player.getItemInHand(hand);
        if (!isTool(held) || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        if (!(level.getBlockEntity(pos) instanceof PipeBlockEntity pipe)) {
            return;
        }

        ItemStack copied = CLIPBOARDS.get(serverPlayer.getUUID());
        if (copied == null || copied.isEmpty()) {
            serverPlayer.sendSystemMessage(PpweMessages.filterWand(
                PpweMessages.warning("Copy an item from an item frame first.")
            ));
            cancelSuccess(event);
            return;
        }

        AddResult result = addCopiedItemToPipeFilter(pipe, copied);
        switch (result) {
            case ADDED_TO_BASE_FILTER -> {
                markPipeChanged(level, pos, pipe);
                serverPlayer.sendSystemMessage(PpweMessages.filterWand(
                    PpweMessages.added("Added ")
                        .append(PpweMessages.itemName(copied))
                        .append(PpweMessages.added(" to the pipe filter module."))
                ));
            }
            case ADDED_TO_FILTER_INCREASE -> {
                markPipeChanged(level, pos, pipe);
                serverPlayer.sendSystemMessage(PpweMessages.filterWand(
                    PpweMessages.added("Added ")
                        .append(PpweMessages.itemName(copied))
                        .append(PpweMessages.added(" to a filter increase modifier."))
                ));
            }
            case DUPLICATE -> serverPlayer.sendSystemMessage(PpweMessages.filterWand(
                PpweMessages.duplicate("This item is already added to this pipe filter.")
            ));
            case NO_FILTER_MODULE -> serverPlayer.sendSystemMessage(PpweMessages.filterWand(
                PpweMessages.error("No Low, Medium, or High Filter Module was found in this pipe.")
            ));
            case FULL -> serverPlayer.sendSystemMessage(PpweMessages.filterWand(
                PpweMessages.error("This pipe filter and all filter increase modifiers are full.")
            ));
        }
        cancelSuccess(event);
    }

    private static AddResult addCopiedItemToPipeFilter(PipeBlockEntity pipe, ItemStack copied) {
        List<ItemFilter> baseFilters = new ArrayList<>();
        List<ItemFilter> increaseFilters = new ArrayList<>();

        for (int slot = 0; slot < pipe.modules.getSlots(); slot++) {
            ItemStack moduleStack = pipe.modules.getStackInSlot(slot);
            if (moduleStack.isEmpty()) {
                continue;
            }
            if (moduleStack.getItem() instanceof FilterModuleItem filterModule) {
                baseFilters.add(filterModule.getItemFilter(moduleStack, pipe));
            } else if (moduleStack.getItem() instanceof FilterIncreaseModuleItem increaseModule) {
                increaseFilters.add(increaseModule.getItemFilter(moduleStack, pipe));
            }
        }

        if (baseFilters.isEmpty()) {
            return AddResult.NO_FILTER_MODULE;
        }

        List<ItemFilter> allFilters = new ArrayList<>(baseFilters.size() + increaseFilters.size());
        allFilters.addAll(baseFilters);
        allFilters.addAll(increaseFilters);
        if (containsDuplicate(allFilters, copied)) {
            return AddResult.DUPLICATE;
        }

        if (insertIntoFirstEmptySlot(baseFilters, copied)) {
            return AddResult.ADDED_TO_BASE_FILTER;
        }
        if (insertIntoFirstEmptySlot(increaseFilters, copied)) {
            return AddResult.ADDED_TO_FILTER_INCREASE;
        }
        return AddResult.FULL;
    }

    private static boolean containsDuplicate(List<ItemFilter> filters, ItemStack copied) {
        for (ItemFilter filter : filters) {
            for (int slot = 0; slot < filter.content.getSlots(); slot++) {
                ItemStack existing = filter.content.getStackInSlot(slot);
                if (!existing.isEmpty() && sameItemAndComponents(existing, copied)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean insertIntoFirstEmptySlot(List<ItemFilter> filters, ItemStack copied) {
        for (ItemFilter filter : filters) {
            for (int slot = 0; slot < filter.content.getSlots(); slot++) {
                if (filter.content.getStackInSlot(slot).isEmpty()) {
                    ItemStack entry = copied.copy();
                    entry.setCount(1);
                    filter.content.setStackInSlot(slot, entry);
                    filter.save();
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean sameItemAndComponents(ItemStack first, ItemStack second) {
        return first.is(second.getItem()) && Objects.equals(first.getComponents(), second.getComponents());
    }

    private static boolean isTool(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(Items.STICK)) {
            return false;
        }
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return !customData.isEmpty() && customData.copyTag().getBoolean(TOOL_TAG);
    }

    private static void markPipeChanged(ServerLevel level, BlockPos pos, PipeBlockEntity pipe) {
        pipe.setChanged();
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private static void cancelSuccess(PlayerInteractEvent.EntityInteract event) {
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void cancelSuccess(PlayerInteractEvent.RightClickBlock event) {
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private enum AddResult {
        ADDED_TO_BASE_FILTER,
        ADDED_TO_FILTER_INCREASE,
        DUPLICATE,
        NO_FILTER_MODULE,
        FULL
    }
}
