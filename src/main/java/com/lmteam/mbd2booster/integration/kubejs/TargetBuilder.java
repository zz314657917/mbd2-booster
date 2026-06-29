package com.lmteam.mbd2booster.integration.kubejs;

import com.lmteam.mbd2booster.common.effect.BoostEffect;
import com.lmteam.mbd2booster.common.effect.TargetDefinition;

import java.util.Map;

public class TargetBuilder {
    private final TargetDefinition definition;

    TargetBuilder(TargetDefinition definition) {
        this.definition = definition;
    }

    public TargetBuilder level(int level, Map<?, ?> effectSpec) {
        definition.levelEffects().put(Math.max(1, level), new BoostEffect(
                readDouble(effectSpec, "speed", 1d),
                readDouble(effectSpec, "energyInput", 1d),
                readDouble(effectSpec, "energyOutput", 1d),
                readDouble(effectSpec, "itemOutput", 1d),
                readDouble(effectSpec, "fluidOutput", 1d),
                readInt(effectSpec, "parallelBonus", 0)
        ).clamp());
        return this;
    }

    private static int readInt(Map<?, ?> properties, String key, int fallback) {
        Object value = properties.get(key);
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static double readDouble(Map<?, ?> properties, String key, double fallback) {
        Object value = properties.get(key);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }
}
