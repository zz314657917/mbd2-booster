package com.lmteam.mbd2booster.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.energy.EnergyStorage;

public class SerializableEnergyStorage extends EnergyStorage {
    private final Runnable changedCallback;

    public SerializableEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable changedCallback) {
        super(capacity, maxReceive, maxExtract);
        this.changedCallback = changedCallback;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (!simulate && received > 0) {
            changedCallback.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (!simulate && extracted > 0) {
            changedCallback.run();
        }
        return extracted;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("energy", energy);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        energy = Math.max(0, Math.min(capacity, tag.getInt("energy")));
        changedCallback.run();
    }
}
