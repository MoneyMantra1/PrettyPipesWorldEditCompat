package com.moneymantra.ppwecompat;

/**
 * Separate Pretty Pipes tuning feature: adjusts the speed bonus granted by
 * pipe pressurizers without changing their FE cost or WorldEdit compatibility behavior.
 */
public final class PressurizerSpeedFeature {
    public static final float ORIGINAL_PRESSURIZER_SPEED_BONUS = 0.45F;

    private PressurizerSpeedFeature() {
    }

    public static float getPressurizerSpeedBonus() {
        return ORIGINAL_PRESSURIZER_SPEED_BONUS * PpweCompatConfig.getPressurizerSpeedMultiplier();
    }

    public static void logEnabled() {
        PpweCompatMod.LOGGER.info(
            "Pretty Pipes pressurizer speed multiplier loaded: {}x ({} speed bonus)",
            PpweCompatConfig.formatFloat(PpweCompatConfig.getPressurizerSpeedMultiplier()),
            PpweCompatConfig.formatFloat(getPressurizerSpeedBonus())
        );
    }
}
