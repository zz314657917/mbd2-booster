package com.lmteam.mbd2booster.common;

import com.lmteam.mbd2booster.common.capability.TargetEvolutionProvider;
import com.lmteam.mbd2booster.common.command.BoosterCommands;
import com.lmteam.mbd2booster.common.data.BoosterSavedData;
import com.lmteam.mbd2booster.common.data.GlobalPosKey;
import com.lmteam.mbd2booster.common.service.BoostService;
import com.lowdragmc.mbd2.api.blockentity.IMachineBlockEntity;
import com.lowdragmc.mbd2.api.machine.IMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineRecipeModifyEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BoosterEventHandlers {
    @SubscribeEvent
    public void attachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof IMachineBlockEntity) {
            event.addCapability(TargetEvolutionProvider.ID, new TargetEvolutionProvider());
        }
    }

    @SubscribeEvent
    public void onRecipeModifyBefore(MachineRecipeModifyEvent.Before event) {
        if (event.getRecipe() == null || !(event.getMachine().getLevel() instanceof ServerLevel)) {
            return;
        }
        var effect = BoostService.effectFor(event.getMachine());
        if (!effect.isIdentity()) {
            event.setRecipe(BoostService.applyRecipeEffect(event.getRecipe(), effect));
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        IMachine.ofMachine(level, event.getPos()).ifPresent(machine ->
                machine.getHolder().getCapability(com.lmteam.mbd2booster.common.capability.BoosterCapabilities.TARGET_EVOLUTION).ifPresent(target -> {
                    var data = BoosterSavedData.get(level);
                    data.unbind(target.getTargetUuid());
                    data.getRecord(target.getTargetUuid()).ifPresent(record -> record.active(false));
                    BoostService.markDirty(level, new GlobalPosKey(level.dimension(), event.getPos()));
                }));
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        BoosterCommands.register(event.getDispatcher());
    }
}
