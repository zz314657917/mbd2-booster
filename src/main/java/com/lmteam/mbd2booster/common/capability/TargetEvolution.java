package com.lmteam.mbd2booster.common.capability;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class TargetEvolution {
    private UUID targetUuid = UUID.randomUUID();
    private int level = 1;
    private int experience;

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = Math.max(0, experience);
    }

    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putUUID("targetUuid", targetUuid);
        tag.putInt("level", level);
        tag.putInt("experience", experience);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.hasUUID("targetUuid")) {
            targetUuid = tag.getUUID("targetUuid");
        }
        level = Math.max(1, tag.getInt("level"));
        experience = Math.max(0, tag.getInt("experience"));
    }
}
