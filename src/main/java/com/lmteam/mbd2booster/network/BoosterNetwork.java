package com.lmteam.mbd2booster.network;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class BoosterNetwork {
    private BoosterNetwork() {
    }

    public static void register(FMLCommonSetupEvent event) {
        // Reserved for the real UI packets. The first implementation keeps all actions server-side through item and commands.
    }
}
