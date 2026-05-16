package com.moneymantra.ppwecompat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class PipeCommandFeature {
    private PipeCommandFeature() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pipe")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("speed")
                .executes(context -> showSpeed(context.getSource()))
                .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(
                        PpweCompatConfig.MIN_PRESSURIZER_SPEED_MULTIPLIER,
                        PpweCompatConfig.MAX_PRESSURIZER_SPEED_MULTIPLIER
                    ))
                    .executes(context -> setSpeed(
                        context.getSource(),
                        (float) DoubleArgumentType.getDouble(context, "multiplier")
                    ))))
            .then(Commands.literal("messages")
                .executes(context -> showMessages(context.getSource()))
                .then(Commands.literal("on")
                    .executes(context -> setMessages(context.getSource(), true)))
                .then(Commands.literal("off")
                    .executes(context -> setMessages(context.getSource(), false))))
            .then(Commands.literal("tool")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> giveTool(
                        context.getSource(),
                        EntityArgument.getPlayer(context, "target"),
                        1
                    ))
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                        .executes(context -> giveTool(
                            context.getSource(),
                            EntityArgument.getPlayer(context, "target"),
                            IntegerArgumentType.getInteger(context, "amount")
                        ))))));
    }

    private static int showSpeed(CommandSourceStack source) {
        float multiplier = PpweCompatConfig.getPressurizerSpeedMultiplier();
        source.sendSuccess(() -> Component.literal(
            "Pipe pressurizer speed multiplier is "
                + PpweCompatConfig.formatFloat(multiplier)
                + "x. Pressurizer bonus: "
                + PpweCompatConfig.formatFloat(PressurizerSpeedFeature.getPressurizerSpeedBonus())
                + "F."
        ), false);
        return 1;
    }

    private static int setSpeed(CommandSourceStack source, float multiplier) {
        float applied = PpweCompatConfig.setPressurizerSpeedMultiplier(multiplier);
        source.sendSuccess(() -> Component.literal(
            "Pipe pressurizer speed multiplier set to "
                + PpweCompatConfig.formatFloat(applied)
                + "x. Pressurizer bonus is now "
                + PpweCompatConfig.formatFloat(PressurizerSpeedFeature.getPressurizerSpeedBonus())
                + "F."
        ), true);
        return 1;
    }

    private static int showMessages(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(
            "Pipe WorldEdit summary messages are "
                + (PpweCompatConfig.areWorldEditSummaryMessagesEnabled() ? "on" : "off")
                + "."
        ), false);
        return 1;
    }

    private static int setMessages(CommandSourceStack source, boolean enabled) {
        PpweCompatConfig.setWorldEditSummaryMessages(enabled);
        source.sendSuccess(() -> Component.literal(
            "Pipe WorldEdit summary messages are now " + (enabled ? "on" : "off") + "."
        ), true);
        return 1;
    }

    private static int giveTool(CommandSourceStack source, ServerPlayer target, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            int stackAmount = Math.min(remaining, 64);
            ItemStack tool = FilterWandFeature.createTool(stackAmount);
            boolean added = target.getInventory().add(tool);
            if (!added && !tool.isEmpty()) {
                target.drop(tool, false);
            }
            remaining -= stackAmount;
        }

        source.sendSuccess(() -> Component.literal(
            "Gave " + amount + " Pipe Filter Wand" + (amount == 1 ? "" : "s") + " to " + target.getGameProfile().getName() + "."
        ), true);
        return amount;
    }
}
