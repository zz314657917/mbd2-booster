package com.lmteam.mbd2booster.common.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class ActiveBuff {
    private ResourceLocation id;
    private int remainingTicks;
    private int nextCostTicks;
    private String pausedReason = "";

    public ActiveBuff(ResourceLocation id, int remainingTicks, int nextCostTicks) {
        this.id = id;
        this.remainingTicks = remainingTicks;
        this.nextCostTicks = nextCostTicks;
    }

    public ResourceLocation id() {
        return id;
    }

    public int remainingTicks() {
        return remainingTicks;
    }

    public void remainingTicks(int remainingTicks) {
        this.remainingTicks = Math.max(0, remainingTicks);
    }

    public int nextCostTicks() {
        return nextCostTicks;
    }

    public void nextCostTicks(int nextCostTicks) {
        this.nextCostTicks = Math.max(0, nextCostTicks);
    }

    public String pausedReason() {
        return pausedReason;
    }

    public void pausedReason(String pausedReason) {
        this.pausedReason = pausedReason == null ? "" : pausedReason;
    }

    public boolean paused() {
        return !pausedReason.isEmpty();
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putString("id", id.toString());
        tag.putInt("remainingTicks", remainingTicks);
        tag.putInt("nextCostTicks", nextCostTicks);
        tag.putString("pausedReason", pausedReason);
        return tag;
    }

    public static ActiveBuff load(CompoundTag tag) {
        var buff = new ActiveBuff(new ResourceLocation(tag.getString("id")), tag.getInt("remainingTicks"), tag.getInt("nextCostTicks"));
        buff.pausedReason(tag.getString("pausedReason"));
        return buff;
    }
}
