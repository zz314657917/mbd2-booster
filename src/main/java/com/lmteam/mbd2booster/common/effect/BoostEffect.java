package com.lmteam.mbd2booster.common.effect;

import com.lmteam.mbd2booster.common.MBD2BoosterConfig;

public record BoostEffect(
        double speed,
        double energyInput,
        double energyOutput,
        double itemOutput,
        double fluidOutput,
        int parallelBonus
) {
    public static final BoostEffect IDENTITY = new BoostEffect(1, 1, 1, 1, 1, 0);

    public BoostEffect multiply(BoostEffect other) {
        return new BoostEffect(
                speed * other.speed,
                energyInput * other.energyInput,
                energyOutput * other.energyOutput,
                itemOutput * other.itemOutput,
                fluidOutput * other.fluidOutput,
                parallelBonus + other.parallelBonus
        );
    }

    public BoostEffect clamp() {
        return new BoostEffect(
                clamp(speed, 0.0001d, MBD2BoosterConfig.MAX_SPEED_MULTIPLIER.get()),
                clamp(energyInput, MBD2BoosterConfig.MIN_ENERGY_INPUT_MULTIPLIER.get(), MBD2BoosterConfig.MAX_ENERGY_INPUT_MULTIPLIER.get()),
                clamp(energyOutput, 0d, MBD2BoosterConfig.MAX_ENERGY_OUTPUT_MULTIPLIER.get()),
                clamp(itemOutput, 0d, MBD2BoosterConfig.MAX_OUTPUT_MULTIPLIER.get()),
                clamp(fluidOutput, 0d, MBD2BoosterConfig.MAX_OUTPUT_MULTIPLIER.get()),
                Math.max(0, Math.min(parallelBonus, MBD2BoosterConfig.DEFAULT_MAX_PARALLEL_CAP.get()))
        );
    }

    public boolean isIdentity() {
        return speed == 1 && energyInput == 1 && energyOutput == 1 && itemOutput == 1 && fluidOutput == 1 && parallelBonus == 0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
