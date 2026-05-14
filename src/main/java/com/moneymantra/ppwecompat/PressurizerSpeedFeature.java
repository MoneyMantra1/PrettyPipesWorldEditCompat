package com.moneymantra.ppwecompat;

/**
 * Separate Pretty Pipes tuning feature: increases the speed bonus granted by
 * pipe pressurizers without changing their FE cost or WorldEdit compatibility behavior.
 */
public final class PressurizerSpeedFeature {
    public static final float ORIGINAL_PRESSURIZER_SPEED_BONUS = 0.45F;
    public static final float ENHANCED_PRESSURIZER_SPEED_BONUS = 0.95F;

    private PressurizerSpeedFeature() {
    }

    public static void logEnabled() {
        PpweCompatMod.LOGGER.info(
            "Pretty Pipes pressurizer speed feature enabled: {} -> {} speed bonus",
            ORIGINAL_PRESSURIZER_SPEED_BONUS,
            ENHANCED_PRESSURIZER_SPEED_BONUS
        );
    }
}
