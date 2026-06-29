package com.lmteam.mbd2booster.common.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class BoosterCapabilities {
    public static final Capability<TargetEvolution> TARGET_EVOLUTION = CapabilityManager.get(new CapabilityToken<>() {});

    private BoosterCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(TargetEvolution.class);
    }
}
