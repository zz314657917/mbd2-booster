package com.lmteam.mbd2booster.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface BoosterServerEvents {
    EventGroup EVENTS = EventGroup.of("MBD2BoosterEvents");
    EventHandler REGISTRY = EVENTS.server("registry", () -> BoosterRegistryEventJS.class);

    static void init() {
        // Loads static event handler definitions.
    }
}
