package com.moneymantra.ppwecompat;

import net.neoforged.fml.loading.FMLPaths;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public final class PpweCompatConfig {
    public static final float MIN_PRESSURIZER_SPEED_MULTIPLIER = 0.1F;
    public static final float MAX_PRESSURIZER_SPEED_MULTIPLIER = 10.0F;

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("prettypipesplus.properties");
    private static final Path LEGACY_CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("ppwecompat.properties");

    private static final LinkedHashMap<String, String> DEFAULT_MESSAGES = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> DEFAULT_COLORS = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> messages = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> colors = new LinkedHashMap<>();

    private static float pressurizerSpeedMultiplier = 1.0F;
    private static boolean worldEditSummaryMessages = true;

    static {
        DEFAULT_MESSAGES.put("message.prefix", "PrettyPipes+");
        DEFAULT_MESSAGES.put("message.command.speed.status", "Speed multiplier: {multiplier}x");
        DEFAULT_MESSAGES.put("message.command.speed.updated", "Speed multiplier set to {multiplier}x");
        DEFAULT_MESSAGES.put("message.command.messages.status", "WorldEdit messages: {status}");
        DEFAULT_MESSAGES.put("message.command.messages.updated", "WorldEdit messages {status}");
        DEFAULT_MESSAGES.put("message.command.tool.issued", "Gave {amount} Filter Wand{plural} to {player}");
        DEFAULT_MESSAGES.put("message.command.reload.complete", "Configuration reloaded from {file}");
        DEFAULT_MESSAGES.put("message.filter.empty_frame", "Item frame is empty");
        DEFAULT_MESSAGES.put("message.filter.copied", "Copied {item}");
        DEFAULT_MESSAGES.put("message.filter.copy_first", "Copy an item first");
        DEFAULT_MESSAGES.put("message.filter.added_base", "Added {item} to filter");
        DEFAULT_MESSAGES.put("message.filter.added_modifier", "Added {item} to filter modifier");
        DEFAULT_MESSAGES.put("message.filter.duplicate", "Item is already in this filter");
        DEFAULT_MESSAGES.put("message.filter.no_filter", "No filter module found");
        DEFAULT_MESSAGES.put("message.filter.full", "Filter is full");
        DEFAULT_MESSAGES.put("message.worldedit.summary", "WorldEdit repair complete — {rebuilt} rebuilt, {removed} removed, {boundary} refreshed");
        DEFAULT_MESSAGES.put("message.worldedit.summary_rotated", "WorldEdit repair complete — {rebuilt} rebuilt, {removed} removed, {boundary} refreshed, {rotated} rotated ({rotation})");
        DEFAULT_MESSAGES.put("message.tool.name", "PrettyPipes+ Filter Wand");
        DEFAULT_MESSAGES.put("message.tool.lore.1", "Copy an item frame, then apply it to a pipe filter.");
        DEFAULT_MESSAGES.put("message.tool.lore.2", "Operator utility item.");

        DEFAULT_COLORS.put("color.prefix", "#35D6E8");
        DEFAULT_COLORS.put("color.separator", "#5C6370");
        DEFAULT_COLORS.put("color.text", "#B8C1CC");
        DEFAULT_COLORS.put("color.value", "#FFFFFF");
        DEFAULT_COLORS.put("color.muted", "#7A828E");
        DEFAULT_COLORS.put("color.success", "#7CFF6B");
        DEFAULT_COLORS.put("color.warning", "#FFD84D");
        DEFAULT_COLORS.put("color.error", "#FF4D5E");
        DEFAULT_COLORS.put("color.copied", "#FFD84D");
        DEFAULT_COLORS.put("color.added", "#7CFF6B");
        DEFAULT_COLORS.put("color.duplicate", "#FF4D5E");
    }

    private PpweCompatConfig() {
    }

    public static synchronized void load() {
        Properties properties = new Properties();
        Path readPath = Files.exists(CONFIG_PATH) ? CONFIG_PATH : LEGACY_CONFIG_PATH;
        if (Files.exists(readPath)) {
            try (BufferedReader reader = Files.newBufferedReader(readPath, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException e) {
                PpweCompatMod.LOGGER.warn("Failed to read {}. Falling back to defaults.", readPath, e);
            }
        }

        pressurizerSpeedMultiplier = clamp(parseFloat(
            properties.getProperty("pressurizerSpeedMultiplier"),
            1.0F
        ));
        worldEditSummaryMessages = parseBoolean(
            properties.getProperty("worldEditSummaryMessages"),
            true
        );

        messages.clear();
        for (Map.Entry<String, String> entry : DEFAULT_MESSAGES.entrySet()) {
            messages.put(entry.getKey(), properties.getProperty(entry.getKey(), entry.getValue()));
        }

        colors.clear();
        for (Map.Entry<String, String> entry : DEFAULT_COLORS.entrySet()) {
            colors.put(entry.getKey(), sanitizeHexColor(properties.getProperty(entry.getKey(), entry.getValue()), entry.getValue()));
        }

        save();
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.write(CONFIG_PATH, createConfigLines(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
        } catch (IOException e) {
            PpweCompatMod.LOGGER.warn("Failed to save {}.", CONFIG_PATH, e);
        }
    }

    public static synchronized void reload() {
        load();
    }

    public static synchronized float getPressurizerSpeedMultiplier() {
        return pressurizerSpeedMultiplier;
    }

    public static synchronized float setPressurizerSpeedMultiplier(float multiplier) {
        pressurizerSpeedMultiplier = clamp(multiplier);
        save();
        return pressurizerSpeedMultiplier;
    }

    public static synchronized boolean areWorldEditSummaryMessagesEnabled() {
        return worldEditSummaryMessages;
    }

    public static synchronized boolean setWorldEditSummaryMessages(boolean enabled) {
        worldEditSummaryMessages = enabled;
        save();
        return worldEditSummaryMessages;
    }

    static synchronized String message(String key) {
        return messages.getOrDefault(key, DEFAULT_MESSAGES.getOrDefault(key, key));
    }

    static synchronized String color(String key) {
        return colors.getOrDefault(key, DEFAULT_COLORS.getOrDefault(key, "#FFFFFF"));
    }

    static String getConfigFileName() {
        return CONFIG_PATH.getFileName().toString();
    }

    public static String formatFloat(float value) {
        if (Math.abs(value - Math.round(value)) < 0.0001F) {
            return Integer.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static Iterable<String> createConfigLines() {
        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add("# PrettyPipes+ server settings");
        lines.add("# Use /pipe reload after editing this file.");
        lines.add("");
        lines.add("# Pressurizer speed is a multiplier of Pretty Pipes' default 0.45F pressurizer bonus.");
        lines.add("pressurizerSpeedMultiplier=" + formatFloat(pressurizerSpeedMultiplier));
        lines.add("worldEditSummaryMessages=" + worldEditSummaryMessages);
        lines.add("");
        lines.add("# Chat message text. Available placeholders are shown in each default value.");
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            lines.add(entry.getKey() + "=" + entry.getValue());
        }
        lines.add("");
        lines.add("# Hex chat colors.");
        for (Map.Entry<String, String> entry : colors.entrySet()) {
            lines.add(entry.getKey() + "=" + entry.getValue());
        }
        return lines;
    }

    private static float parseFloat(String raw, float fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Float.parseFloat(raw.trim());
        } catch (NumberFormatException e) {
            PpweCompatMod.LOGGER.warn("Invalid pressurizerSpeedMultiplier '{}'. Using {}.", raw, fallback);
            return fallback;
        }
    }

    private static boolean parseBoolean(String raw, boolean fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "true", "yes", "on", "1" -> true;
            case "false", "no", "off", "0" -> false;
            default -> fallback;
        };
    }

    private static float clamp(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return 1.0F;
        }
        return Math.max(MIN_PRESSURIZER_SPEED_MULTIPLIER, Math.min(MAX_PRESSURIZER_SPEED_MULTIPLIER, value));
    }

    private static String sanitizeHexColor(String raw, String fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String normalized = raw.trim();
        if (!normalized.startsWith("#")) {
            normalized = "#" + normalized;
        }
        if (normalized.matches("#[0-9a-fA-F]{6}")) {
            return normalized.toUpperCase(Locale.ROOT);
        }
        PpweCompatMod.LOGGER.warn("Invalid color '{}'. Using {}.", raw, fallback);
        return fallback;
    }
}
