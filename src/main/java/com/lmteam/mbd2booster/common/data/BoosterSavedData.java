package com.lmteam.mbd2booster.common.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class BoosterSavedData extends SavedData {
    private static final String ID = "mbd2_booster";
    private final Map<UUID, UUID> targetToBase = new HashMap<>();
    private final Map<UUID, Set<UUID>> baseToTargets = new HashMap<>();
    private final Map<UUID, BindingRecord> targets = new HashMap<>();
    private final Map<UUID, GlobalPosKey> basePositions = new HashMap<>();

    public static BoosterSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BoosterSavedData::load, BoosterSavedData::new, ID);
    }

    public static BoosterSavedData load(CompoundTag tag) {
        var data = new BoosterSavedData();
        var list = tag.getList("targets", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            var entry = list.getCompound(i);
            var target = entry.getUUID("targetUuid");
            var base = entry.hasUUID("baseUuid") ? entry.getUUID("baseUuid") : null;
            var record = BindingRecord.load(entry.getCompound("record"));
            data.targets.put(target, record);
            if (base != null) {
                data.targetToBase.put(target, base);
                data.baseToTargets.computeIfAbsent(base, ignored -> new LinkedHashSet<>()).add(target);
            }
        }
        var bases = tag.getList("bases", Tag.TAG_COMPOUND);
        for (int i = 0; i < bases.size(); i++) {
            var entry = bases.getCompound(i);
            if (entry.hasUUID("baseUuid")) {
                data.basePositions.put(entry.getUUID("baseUuid"), GlobalPosKey.load(entry.getCompound("pos")));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        var list = new ListTag();
        for (var entry : targets.entrySet()) {
            var row = new CompoundTag();
            row.putUUID("targetUuid", entry.getKey());
            var base = targetToBase.get(entry.getKey());
            if (base != null) {
                row.putUUID("baseUuid", base);
            }
            row.put("record", entry.getValue().save());
            list.add(row);
        }
        tag.put("targets", list);
        var bases = new ListTag();
        for (var entry : basePositions.entrySet()) {
            var row = new CompoundTag();
            row.putUUID("baseUuid", entry.getKey());
            row.put("pos", entry.getValue().save());
            bases.add(row);
        }
        tag.put("bases", bases);
        return tag;
    }

    public Optional<UUID> getBoundBase(UUID targetUuid) {
        return Optional.ofNullable(targetToBase.get(targetUuid));
    }

    public Optional<BindingRecord> getRecord(UUID targetUuid) {
        return Optional.ofNullable(targets.get(targetUuid));
    }

    public Collection<BindingRecord> allRecords() {
        return Collections.unmodifiableCollection(targets.values());
    }

    public Set<UUID> getTargets(UUID baseUuid) {
        return Collections.unmodifiableSet(baseToTargets.getOrDefault(baseUuid, Set.of()));
    }

    public Optional<GlobalPosKey> getBasePos(UUID baseUuid) {
        return Optional.ofNullable(basePositions.get(baseUuid));
    }

    public void rememberBase(UUID baseUuid, GlobalPosKey basePos) {
        if (!Objects.equals(basePositions.get(baseUuid), basePos)) {
            basePositions.put(baseUuid, basePos);
            setDirty();
        }
    }

    public boolean bind(UUID baseUuid, BindingRecord record) {
        var existing = targetToBase.get(record.targetUuid());
        if (existing != null && !existing.equals(baseUuid)) {
            return false;
        }
        targets.put(record.targetUuid(), record);
        targetToBase.put(record.targetUuid(), baseUuid);
        baseToTargets.computeIfAbsent(baseUuid, ignored -> new LinkedHashSet<>()).add(record.targetUuid());
        setDirty();
        return true;
    }

    public void unbind(UUID targetUuid) {
        var base = targetToBase.remove(targetUuid);
        if (base != null) {
            var set = baseToTargets.get(base);
            if (set != null) {
                set.remove(targetUuid);
                if (set.isEmpty()) {
                    baseToTargets.remove(base);
                }
            }
            setDirty();
        }
        var record = targets.get(targetUuid);
        if (record != null && record.active()) {
            record.active(false);
            setDirty();
        }
    }

    public void removeBase(UUID baseUuid) {
        var targetsForBase = new ArrayList<>(baseToTargets.getOrDefault(baseUuid, Set.of()));
        for (var target : targetsForBase) {
            targetToBase.remove(target);
            var record = targets.get(target);
            if (record != null) {
                record.active(false);
            }
        }
        baseToTargets.remove(baseUuid);
        basePositions.remove(baseUuid);
        setDirty();
    }

    public int cleanInvalid(ServerLevel level) {
        int removed = 0;
        for (var entry : new ArrayList<>(targets.entrySet())) {
            var record = entry.getValue();
            if (!record.active()) {
                continue;
            }
            var targetLevel = level.getServer().getLevel(record.targetPos().dimension());
            if (targetLevel == null || !targetLevel.isLoaded(record.targetPos().pos())) {
                continue;
            }
            if (com.lowdragmc.mbd2.api.machine.IMachine.ofMachine(targetLevel, record.targetPos().pos()).isEmpty()) {
                unbind(entry.getKey());
                record.active(false);
                removed++;
            }
        }
        if (removed > 0) {
            setDirty();
        }
        return removed;
    }
}
