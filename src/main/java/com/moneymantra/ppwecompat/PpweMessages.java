package com.moneymantra.ppwecompat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

final class PpweMessages {
    private static final ChatFormatting PREFIX_COLOR = ChatFormatting.AQUA;
    private static final ChatFormatting SEPARATOR_COLOR = ChatFormatting.DARK_GRAY;
    private static final ChatFormatting TEXT_COLOR = ChatFormatting.GRAY;
    private static final ChatFormatting VALUE_COLOR = ChatFormatting.WHITE;
    private static final ChatFormatting MUTED_COLOR = ChatFormatting.DARK_GRAY;

    static final ChatFormatting COPIED_COLOR = ChatFormatting.YELLOW;
    static final ChatFormatting ADDED_COLOR = ChatFormatting.GREEN;
    static final ChatFormatting DUPLICATE_COLOR = ChatFormatting.RED;

    private PpweMessages() {
    }

    static Component command(Component body) {
        return prefixed("Pipe", body);
    }

    static Component worldEdit(Component body) {
        return prefixed("Pipe WorldEdit", body);
    }

    static Component filterWand(Component body) {
        return prefixed("Pipe Filter Wand", body);
    }

    static MutableComponent text(String text) {
        return Component.literal(text).withStyle(TEXT_COLOR);
    }

    static MutableComponent value(String text) {
        return Component.literal(text).withStyle(VALUE_COLOR);
    }

    static MutableComponent muted(String text) {
        return Component.literal(text).withStyle(MUTED_COLOR);
    }

    static MutableComponent success(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GREEN);
    }

    static MutableComponent warning(String text) {
        return Component.literal(text).withStyle(ChatFormatting.YELLOW);
    }

    static MutableComponent error(String text) {
        return Component.literal(text).withStyle(ChatFormatting.RED);
    }

    static MutableComponent copied(String text) {
        return Component.literal(text).withStyle(COPIED_COLOR);
    }

    static MutableComponent added(String text) {
        return Component.literal(text).withStyle(ADDED_COLOR);
    }

    static MutableComponent duplicate(String text) {
        return Component.literal(text).withStyle(DUPLICATE_COLOR);
    }

    static MutableComponent itemName(ItemStack stack) {
        return stack.getHoverName().copy().withStyle(VALUE_COLOR);
    }

    private static Component prefixed(String label, Component body) {
        return Component.literal(label)
            .withStyle(PREFIX_COLOR, ChatFormatting.BOLD)
            .append(Component.literal(" › ").withStyle(SEPARATOR_COLOR))
            .append(body);
    }
}
