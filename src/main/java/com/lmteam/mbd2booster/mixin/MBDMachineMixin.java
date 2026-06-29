package com.lmteam.mbd2booster.mixin;

import com.lmteam.mbd2booster.common.service.BoostService;
import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import com.lowdragmc.mbd2.api.recipe.content.ContentModifier;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MBDMachine.class, remap = false)
public abstract class MBDMachineMixin {
    @Inject(method = "getMaxParallel", at = @At("RETURN"), cancellable = true, require = 1)
    private void mbd2Booster$addParallelBonus(MBDRecipe recipe, CallbackInfoReturnable<ContentModifier> cir) {
        int bonus = BoostService.parallelBonus((MBDMachine) (Object) this);
        if (bonus > 0) {
            cir.setReturnValue(cir.getReturnValue().merge(ContentModifier.addition(bonus)));
        }
    }
}
