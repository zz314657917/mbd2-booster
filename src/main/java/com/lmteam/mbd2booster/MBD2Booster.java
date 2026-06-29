package com.lmteam.mbd2booster;

import com.lmteam.mbd2booster.common.BoosterEventHandlers;
import com.lmteam.mbd2booster.common.MBD2BoosterConfig;
import com.lmteam.mbd2booster.common.registry.BoosterRegistry;
import com.lmteam.mbd2booster.network.BoosterNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MBD2Booster.MOD_ID)
public class MBD2Booster {
    public static final String MOD_ID = "mbd2_booster";
    public static final Logger LOGGER = LoggerFactory.getLogger("MBD2 Booster");

    public MBD2Booster() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BoosterRegistry.register(modBus);
        modBus.addListener(BoosterRegistry::registerCapabilities);
        modBus.addListener(BoosterRegistry::buildCreativeTab);
        modBus.addListener(BoosterNetwork::register);
        MinecraftForge.EVENT_BUS.register(new BoosterEventHandlers());
        MBD2BoosterConfig.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
