package com.moneymantra.ppwecompat;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public final class PpweCompatConfig {
    public static final float MIN_PRESSURIZER_SPEED_MULTIPLIER = 0.1F;
    public static final float MAX_PRESSURIZER_SPEED_MULTIPLIER = 10.0F;

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("ppwecompat.properties");
    private static float pressurizerSpeedMultiplier = 1.0F;
    private static boolean worldEditSummaryMessages = true;

    private PpweCompatConfig() {
    }

    public static synchronized void load() {
        Properties properties = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream stream = Files.newInputStream(CONFIG_PATH)) {
                properties.load(stream);
            } catch (IOException e) {
                PpweCompatMod.LOGGER.warn("Failed to read {}. Falling back to defaults.", CONFIG_PATH, e);
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
        save();
    }

    public static synchronized void save() {
        Properties properties = new Properties();
        properties.setProperty("pressurizerSpeedMultiplier", formatFloat(pressurizerSpeedMultiplier));
        properties.setProperty("worldEditSummaryMessages", Boolean.toString(worldEditSummaryMessages));

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream stream = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(stream, "Pretty Pipes WorldEdit Compat server settings");
            }
        } catch (IOException e) {
            PpweCompatMod.LOGGER.warn("Failed to save {}.", CONFIG_PATH, e);
        }
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

    public static String formatFloat(float value) {
        if (Math.abs(value - Math.round(value)) < 0.0001F) {
            return Integer.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
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
}
