package com.lmteam.mbd2booster.common.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class BindingRecord {
    private final UUID targetUuid;
    private final GlobalPosKey targetPos;
    private ResourceLocation machineId;
    private boolean active = true;
    private long lastSeenTime;

    public BindingRecord(UUID targetUuid, GlobalPosKey targetPos, ResourceLocation machineId) {
        this.targetUuid = targetUuid;
        this.targetPos = targetPos;
        this.machineId = machineId;
    }

    public UUID targetUuid() {
        return targetUuid;
    }

    public GlobalPosKey targetPos() {
        return targetPos;
    }

    public ResourceLocation machineId() {
        return machineId;
    }

    public void machineId(ResourceLocation machineId) {
        this.machineId = machineId;
    }

    public boolean active() {
        return active;
    }

    public void active(boolean active) {
        this.active = active;
    }

    public long lastSeenTime() {
        return lastSeenTime;
    }

    public void lastSeenTime(long lastSeenTime) {
        this.lastSeenTime = lastSeenTime;
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putUUID("targetUuid", targetUuid);
        tag.put("targetPos", targetPos.save());
        tag.putString("machineId", machineId == null ? "" : machineId.toString());
        tag.putBoolean("active", active);
        tag.putLong("lastSeenTime", lastSeenTime);
        return tag;
    }

    public static BindingRecord load(CompoundTag tag) {
        var record = new BindingRecord(tag.getUUID("targetUuid"), GlobalPosKey.load(tag.getCompound("targetPos")),
                tag.getString("machineId").isEmpty() ? null : new ResourceLocation(tag.getString("machineId")));
        record.active(tag.getBoolean("active"));
        record.lastSeenTime(tag.getLong("lastSeenTime"));
        return record;
    }
}
