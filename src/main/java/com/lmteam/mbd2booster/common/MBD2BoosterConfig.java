package com.lmteam.mbd2booster.common;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class MBD2BoosterConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.IntValue DEFAULT_UPDATE_INTERVAL;
    public static final ForgeConfigSpec.IntValue DEFAULT_MAX_PARALLEL_CAP;
    public static final ForgeConfigSpec.DoubleValue MIN_DURATION_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MAX_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MAX_OUTPUT_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MAX_ENERGY_OUTPUT_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MIN_ENERGY_INPUT_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MAX_ENERGY_INPUT_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ALLOW_CROSS_DIMENSION;
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("limits");
        DEFAULT_UPDATE_INTERVAL = BUILDER.comment("Booster base service interval in ticks.")
                .defineInRange("updateIntervalTicks", 20, 1, 20 * 60);
        DEFAULT_MAX_PARALLEL_CAP = BUILDER.comment("Hard cap for effective MBD2 max parallel.")
                .defineInRange("maxParallelCap", 64, 1, 4096);
        MIN_DURATION_MULTIPLIER = BUILDER.comment("Smallest allowed final duration multiplier.")
                .defineInRange("minDurationMultiplier", 0.05d, 0.0001d, 1000d);
        MAX_SPEED_MULTIPLIER = BUILDER.defineInRange("maxSpeedMultiplier", 64d, 0.0001d, 1000d);
        MAX_OUTPUT_MULTIPLIER = BUILDER.defineInRange("maxOutputMultiplier", 64d, 0d, 1000d);
        MAX_ENERGY_OUTPUT_MULTIPLIER = BUILDER.defineInRange("maxEnergyOutputMultiplier", 64d, 0d, 1000d);
        MIN_ENERGY_INPUT_MULTIPLIER = BUILDER.defineInRange("minEnergyInputMultiplier", 0.05d, 0d, 1000d);
        MAX_ENERGY_INPUT_MULTIPLIER = BUILDER.defineInRange("maxEnergyInputMultiplier", 64d, 0d, 1000d);
        ALLOW_CROSS_DIMENSION = BUILDER.comment("First release defaults to same-dimension binding only.")
                .define("allowCrossDimension", false);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private MBD2BoosterConfig() {
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC);
    }
}
