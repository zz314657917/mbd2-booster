package com.lmteam.mbd2booster.integration.kubejs;

import com.lmteam.mbd2booster.common.effect.BaseDefinition;
import com.lmteam.mbd2booster.common.effect.BaseLevel;

import java.util.Map;

public class BaseBuilder {
    private final BaseDefinition definition;

    BaseBuilder(BaseDefinition definition) {
        this.definition = definition;
    }

    public BaseBuilder level(int level, Map<?, ?> properties) {
        int maxTargets = readInt(properties, "maxTargets", 1);
        int maxTargetLevel = readInt(properties, "maxTargetLevel", 1);
        definition.levels().put(Math.max(1, level), new BaseLevel(Math.max(1, maxTargets), Math.max(1, maxTargetLevel)));
        return this;
    }

    public BaseBuilder maxActiveBuffs(int value) {
        definition.maxActiveBuffs(value);
        return this;
    }

    private static int readInt(Map<?, ?> properties, String key, int fallback) {
        Object value = properties.get(key);
        return value instanceof Number number ? number.intValue() : fallback;
    }
}
