package com.lmteam.mbd2booster.common.effect;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class BaseDefinition {
    private final ResourceLocation id;
    private final Map<Integer, BaseLevel> levels = new LinkedHashMap<>();
    private int maxActiveBuffs = 1;

    public BaseDefinition(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation id() {
        return id;
    }

    public Map<Integer, BaseLevel> levels() {
        return levels;
    }

    public int maxActiveBuffs() {
        return maxActiveBuffs;
    }

    public BaseDefinition maxActiveBuffs(int maxActiveBuffs) {
        this.maxActiveBuffs = Math.max(1, maxActiveBuffs);
        return this;
    }

    public BaseLevel levelOrDefault(int level) {
        var selected = levels.get(1);
        for (var entry : levels.entrySet()) {
            if (entry.getKey() <= level) {
                selected = entry.getValue();
            }
        }
        return selected == null ? new BaseLevel(1, 1) : selected;
    }
}
