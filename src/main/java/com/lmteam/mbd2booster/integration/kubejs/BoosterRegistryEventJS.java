package com.lmteam.mbd2booster.integration.kubejs;

import com.lmteam.mbd2booster.common.effect.BaseDefinition;
import com.lmteam.mbd2booster.common.effect.BoosterDefinitions;
import com.lmteam.mbd2booster.common.effect.BuffDefinition;
import com.lmteam.mbd2booster.common.effect.TargetDefinition;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

public class BoosterRegistryEventJS extends EventJS {
    public BaseBuilder base(String id) {
        return new BaseBuilder(BoosterDefinitions.defineBase(new ResourceLocation(id)));
    }

    public BuffBuilder buff(String id) {
        return new BuffBuilder(BoosterDefinitions.defineBuff(new ResourceLocation(id)));
    }

    public TargetBuilder target(String id) {
        return new TargetBuilder(BoosterDefinitions.defineTarget(new ResourceLocation(id)));
    }
}
