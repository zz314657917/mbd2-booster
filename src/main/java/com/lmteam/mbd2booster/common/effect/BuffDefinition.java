package com.lmteam.mbd2booster.common.effect;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BuffDefinition {
    private final ResourceLocation id;
    private int durationTicks = 20 * 60;
    private int costInterval = 20;
    private int energyCost;
    private final List<ItemCost> costs = new ArrayList<>();
    private final Set<ResourceLocation> targetMachines = new HashSet<>();
    private final Set<ResourceLocation> deniedOutputMachines = new HashSet<>();
    private BoostEffect effect = BoostEffect.IDENTITY;

    public BuffDefinition(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation id() {
        return id;
    }

    public int durationTicks() {
        return durationTicks;
    }

    public BuffDefinition durationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
        return this;
    }

    public int costInterval() {
        return costInterval;
    }

    public BuffDefinition costInterval(int costInterval) {
        this.costInterval = Math.max(1, costInterval);
        return this;
    }

    public int energyCost() {
        return energyCost;
    }

    public BuffDefinition energyCost(int energyCost) {
        this.energyCost = Math.max(0, energyCost);
        return this;
    }

    public List<ItemCost> costs() {
        return costs;
    }

    public Set<ResourceLocation> targetMachines() {
        return targetMachines;
    }

    public Set<ResourceLocation> deniedOutputMachines() {
        return deniedOutputMachines;
    }

    public BoostEffect effect() {
        return effect;
    }

    public BuffDefinition effect(BoostEffect effect) {
        this.effect = effect == null ? BoostEffect.IDENTITY : effect.clamp();
        return this;
    }

    public boolean appliesTo(ResourceLocation machineId) {
        return targetMachines.isEmpty() || targetMachines.contains(machineId);
    }

    public BoostEffect effectFor(ResourceLocation machineId) {
        if (machineId != null && deniedOutputMachines.contains(machineId)) {
            return new BoostEffect(effect.speed(), effect.energyInput(), effect.energyOutput(), 1, 1, effect.parallelBonus()).clamp();
        }
        return effect;
    }
}
