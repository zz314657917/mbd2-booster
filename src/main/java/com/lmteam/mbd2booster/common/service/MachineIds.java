package com.lmteam.mbd2booster.common.service;

import com.lowdragmc.mbd2.api.machine.IMachine;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public final class MachineIds {
    private MachineIds() {
    }

    public static Optional<ResourceLocation> get(IMachine machine) {
        if (machine instanceof MBDMachine mbdMachine) {
            return Optional.of(mbdMachine.getDefinition().id());
        }
        return Optional.empty();
    }
}
