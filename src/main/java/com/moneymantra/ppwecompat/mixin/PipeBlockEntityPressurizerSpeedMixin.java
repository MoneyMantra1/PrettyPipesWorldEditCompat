package com.moneymantra.ppwecompat.mixin;

import com.moneymantra.ppwecompat.PressurizerSpeedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Pretty Pipes hardcodes the pipe pressurizer item speed bonus inside
 * PipeBlockEntity#getItemSpeed. This mixin replaces only that bonus constant.
 *
 * It intentionally does not change PressurizerBlockEntity energy usage.
 */
@Mixin(targets = "de.ellpeck.prettypipes.pipe.PipeBlockEntity", remap = false)
public abstract class PipeBlockEntityPressurizerSpeedMixin {
    @ModifyConstant(
        method = "getItemSpeed",
        constant = @Constant(floatValue = PressurizerSpeedFeature.ORIGINAL_PRESSURIZER_SPEED_BONUS),
        remap = false
    )
    private float ppwecompat$increasePressurizerSpeedBonus(float original) {
        return PressurizerSpeedFeature.ENHANCED_PRESSURIZER_SPEED_BONUS;
    }
}
