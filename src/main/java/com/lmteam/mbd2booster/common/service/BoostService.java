package com.lmteam.mbd2booster.common.service;

import com.lmteam.mbd2booster.common.MBD2BoosterConfig;
import com.lmteam.mbd2booster.common.blockentity.BoosterBaseBlockEntity;
import com.lmteam.mbd2booster.common.capability.BoosterCapabilities;
import com.lmteam.mbd2booster.common.data.GlobalPosKey;
import com.lmteam.mbd2booster.common.effect.BoostEffect;
import com.lmteam.mbd2booster.common.effect.BoosterDefinitions;
import com.lowdragmc.mbd2.api.machine.IMachine;
import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import com.lowdragmc.mbd2.api.recipe.content.ContentModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BoostService {
    private static final Map<UUID, CachedBase> BASES = new ConcurrentHashMap<>();
    private static volatile long observedDefinitionVersion = BoosterDefinitions.version();

    private BoostService() {
    }

    public static void refreshBase(BoosterBaseBlockEntity base) {
        if (base.getLevel() instanceof ServerLevel) {
            BASES.put(base.baseUuid(), CachedBase.from(base));
        }
    }

    public static void removeBase(UUID baseUuid) {
        BASES.remove(baseUuid);
    }

    public static BoostEffect effectFor(IMachine machine) {
        if (!(machine.getLevel() instanceof ServerLevel serverLevel)) {
            return BoostEffect.IDENTITY;
        }
        return machine.getHolder().getCapability(BoosterCapabilities.TARGET_EVOLUTION)
                .map(target -> effectFor(serverLevel, machine, target.getTargetUuid(), target.getLevel()))
                .orElse(BoostEffect.IDENTITY);
    }

    public static int parallelBonus(IMachine machine) {
        return Math.max(0, effectFor(machine).parallelBonus());
    }

    public static int effectiveTargetLevel(ServerLevel level, UUID targetUuid, int storedLevel) {
        var data = com.lmteam.mbd2booster.common.data.BoosterSavedData.get(level);
        var baseUuid = data.getBoundBase(targetUuid).orElse(null);
        if (baseUuid == null) {
            return storedLevel;
        }
        return Math.min(storedLevel, resolveBase(level, baseUuid).map(CachedBase::maxTargetLevel).orElse(storedLevel));
    }

    public static Optional<UUID> boundBase(ServerLevel level, UUID targetUuid) {
        return com.lmteam.mbd2booster.common.data.BoosterSavedData.get(level).getBoundBase(targetUuid);
    }

    public static void markDirty(ServerLevel level, GlobalPosKey targetPos) {
        ServerLevel targetLevel = level.getServer().getLevel(targetPos.dimension());
        if (targetLevel == null || !targetLevel.isLoaded(targetPos.pos())) {
            return;
        }
        IMachine.ofMachine(targetLevel, targetPos.pos()).ifPresent(machine -> machine.getRecipeLogic().markLastRecipeDirty());
    }

    public static void markAllDirty(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            var data = com.lmteam.mbd2booster.common.data.BoosterSavedData.get(level);
            for (var record : data.allRecords()) {
                markDirty(level, record.targetPos());
            }
        }
    }

    public static void onDefinitionsReloaded(MinecraftServer server) {
        observedDefinitionVersion = BoosterDefinitions.version();
        markAllDirty(server);
    }

    public static MBDRecipe applyRecipeEffect(MBDRecipe recipe, BoostEffect effect) {
        if (effect.isIdentity()) {
            return recipe;
        }
        MBDRecipe modified = recipe.copy();
        if (effect.speed() != 1) {
            double durationMultiplier = 1d / Math.max(0.0001d, effect.speed());
            durationMultiplier = Math.max(MBD2BoosterConfig.MIN_DURATION_MULTIPLIER.get(), durationMultiplier);
            modified.duration = Math.max(1, ContentModifier.multiplier(durationMultiplier).apply(modified.duration).intValue());
        }
        if (effect.energyInput() != 1) {
            modified = RecipeOutputModifier.copyInputs(modified, effect.energyInput());
        }
        if (effect.itemOutput() != 1 || effect.fluidOutput() != 1 || effect.energyOutput() != 1) {
            modified = RecipeOutputModifier.copyOutputs(modified, effect.itemOutput(), effect.fluidOutput(), effect.energyOutput());
        }
        return modified;
    }

    public static boolean definitionsChanged() {
        return observedDefinitionVersion != BoosterDefinitions.version();
    }

    private static BoostEffect effectFor(ServerLevel level, IMachine machine, UUID targetUuid, int storedTargetLevel) {
        var data = com.lmteam.mbd2booster.common.data.BoosterSavedData.get(level);
        var baseUuid = data.getBoundBase(targetUuid).orElse(null);
        if (baseUuid == null) {
            return targetLevelEffect(machine, storedTargetLevel).clamp();
        }
        var base = resolveBase(level, baseUuid).orElse(null);
        if (base == null) {
            return BoostEffect.IDENTITY;
        }
        int effectiveLevel = Math.min(storedTargetLevel, base.maxTargetLevel());
        ResourceLocation machineId = MachineIds.get(machine).orElse(null);
        if (machineId == null) {
            return BoostEffect.IDENTITY;
        }
        BoostEffect effect = targetLevelEffect(machine, effectiveLevel);
        for (var buff : base.activeBuffs().entrySet()) {
            var definition = BoosterDefinitions.buff(buff.getKey()).orElse(null);
            if (definition == null || buff.getValue() || !definition.appliesTo(machineId)) {
                continue;
            }
            effect = effect.multiply(definition.effectFor(machineId));
        }
        return effect.clamp();
    }

    private static BoostEffect targetLevelEffect(IMachine machine, int level) {
        return MachineIds.get(machine)
                .map(machineId -> BoosterDefinitions.target(machineId).effectForLevel(level))
                .orElse(BoostEffect.IDENTITY);
    }

    private static Optional<CachedBase> resolveBase(ServerLevel level, UUID baseUuid) {
        var cached = BASES.get(baseUuid);
        if (cached != null) {
            return Optional.of(cached);
        }
        var data = com.lmteam.mbd2booster.common.data.BoosterSavedData.get(level);
        var pos = data.getBasePos(baseUuid).orElse(null);
        if (pos == null) {
            return Optional.empty();
        }
        ServerLevel baseLevel = level.getServer().getLevel(pos.dimension());
        if (baseLevel == null || !baseLevel.isLoaded(pos.pos())) {
            return Optional.empty();
        }
        BlockEntity blockEntity = baseLevel.getBlockEntity(pos.pos());
        if (blockEntity instanceof BoosterBaseBlockEntity base) {
            refreshBase(base);
            return Optional.ofNullable(BASES.get(baseUuid));
        }
        return Optional.empty();
    }

    private record CachedBase(
            UUID baseUuid,
            GlobalPosKey pos,
            int baseLevel,
            int maxTargets,
            int maxTargetLevel,
            Map<ResourceLocation, Boolean> activeBuffs
    ) {
        static CachedBase from(BoosterBaseBlockEntity base) {
            var baseDefinition = BoosterDefinitions.base(com.lmteam.mbd2booster.MBD2Booster.id("core")).orElse(null);
            var baseLevel = baseDefinition == null ? null : baseDefinition.levelOrDefault(base.baseLevel());
            int maxTargets = baseLevel == null ? 1 : baseLevel.maxTargets();
            int maxTargetLevel = baseLevel == null ? 1 : baseLevel.maxTargetLevel();
            Map<ResourceLocation, Boolean> buffs = new java.util.HashMap<>();
            for (var buff : base.activeBuffs()) {
                buffs.put(buff.id(), buff.paused());
            }
            return new CachedBase(base.baseUuid(), base.globalPos(), base.baseLevel(), maxTargets, maxTargetLevel, Map.copyOf(buffs));
        }
    }
}
