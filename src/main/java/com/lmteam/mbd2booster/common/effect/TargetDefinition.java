package com.lmteam.mbd2booster.common.effect;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class TargetDefinition {
    private final ResourceLocation machineId;
    private final Map<Integer, BoostEffect> levelEffects = new LinkedHashMap<>();

    public TargetDefinition(ResourceLocation machineId) {
        this.machineId = machineId;
        levelEffects.put(1, BoostEffect.IDENTITY);
    }

    public ResourceLocation machineId() {
        return machineId;
    }

    public Map<Integer, BoostEffect> levelEffects() {
        return levelEffects;
    }

    public BoostEffect effectForLevel(int level) {
        var effect = BoostEffect.IDENTITY;
        for (var entry : levelEffects.entrySet()) {
            if (entry.getKey() <= level) {
                effect = entry.getValue();
            }
        }
        return effect;
    }
}
