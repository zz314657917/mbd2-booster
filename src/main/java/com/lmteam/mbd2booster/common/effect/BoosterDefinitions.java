package com.lmteam.mbd2booster.common.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public final class BoosterDefinitions {
    private static final Map<ResourceLocation, BaseDefinition> BASES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, BuffDefinition> BUFFS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, TargetDefinition> TARGETS = new LinkedHashMap<>();
    private static long version = 1;

    static {
        resetDefaults();
    }

    private BoosterDefinitions() {
    }

    public static void resetDefaults() {
        BASES.clear();
        BUFFS.clear();
        TARGETS.clear();
        var baseId = new ResourceLocation("mbd2_booster", "core");
        var base = new BaseDefinition(baseId);
        base.levels().put(1, new BaseLevel(2, 3));
        base.levels().put(2, new BaseLevel(4, 5));
        BASES.put(baseId, base);

        var buffId = new ResourceLocation("mbd2_booster", "overdrive");
        var buff = new BuffDefinition(buffId);
        buff.durationTicks(20 * 60);
        buff.costInterval(20);
        buff.costs().add(new ItemCost(new ItemStack(Items.REDSTONE, 8)));
        buff.effect(new BoostEffect(1.5, 1.25, 1, 1.2, 1, 1));
        BUFFS.put(buffId, buff);
        version++;
    }

    public static long version() {
        return version;
    }

    public static void bumpVersion() {
        version++;
    }

    public static Optional<BaseDefinition> base(ResourceLocation id) {
        return Optional.ofNullable(BASES.get(id));
    }

    public static Optional<BuffDefinition> buff(ResourceLocation id) {
        return Optional.ofNullable(BUFFS.get(id));
    }

    public static TargetDefinition target(ResourceLocation machineId) {
        return TARGETS.computeIfAbsent(machineId, TargetDefinition::new);
    }

    public static Collection<BuffDefinition> buffs() {
        return Collections.unmodifiableCollection(BUFFS.values());
    }

    public static BaseDefinition defineBase(ResourceLocation id) {
        var definition = new BaseDefinition(id);
        BASES.put(id, definition);
        bumpVersion();
        return definition;
    }

    public static BuffDefinition defineBuff(ResourceLocation id) {
        var definition = new BuffDefinition(id);
        BUFFS.put(id, definition);
        bumpVersion();
        return definition;
    }

    public static TargetDefinition defineTarget(ResourceLocation id) {
        var definition = new TargetDefinition(id);
        TARGETS.put(id, definition);
        bumpVersion();
        return definition;
    }
}
