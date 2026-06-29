package com.lmteam.mbd2booster.integration.kubejs;

import com.lmteam.mbd2booster.common.effect.BoosterDefinitions;
import com.lmteam.mbd2booster.common.service.BoostService;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.server.ServerScriptManager;

public class BoosterKubeJSPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        super.registerEvents();
        BoosterServerEvents.init();
        BoosterServerEvents.EVENTS.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        super.registerBindings(event);
        event.add("MBD2BoosterDefinitions", BoosterDefinitions.class);
        event.add("MBD2BoosterEvents", BoosterServerEvents.EVENTS);
    }

    @Override
    public void onServerReload() {
        super.onServerReload();
        BoosterDefinitions.resetDefaults();
        BoosterServerEvents.REGISTRY.post(ScriptType.SERVER, new BoosterRegistryEventJS());
        if (ServerScriptManager.instance != null) {
            BoostService.onDefinitionsReloaded(ServerScriptManager.instance.server);
        }
    }
}
