package com.lmteam.mbd2booster.common.command;

import com.lmteam.mbd2booster.common.capability.BoosterCapabilities;
import com.lmteam.mbd2booster.common.blockentity.BoosterBaseBlockEntity;
import com.lmteam.mbd2booster.common.data.BoosterSavedData;
import com.lmteam.mbd2booster.common.data.GlobalPosKey;
import com.lmteam.mbd2booster.common.effect.BoostEffect;
import com.lmteam.mbd2booster.common.service.BoostService;
import com.lowdragmc.mbd2.api.machine.IMachine;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public final class BoosterCommands {
    private BoosterCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mbd2booster")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("debug")
                        .then(Commands.literal("target")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> debugTarget(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"))))))
                .then(Commands.literal("dirty")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> dirty(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("base")
                        .then(Commands.literal("level")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(context -> setBaseLevel(context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        IntegerArgumentType.getInteger(context, "level"))))))
                        .then(Commands.literal("buff")
                                .then(Commands.literal("activate")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .then(Commands.argument("id", ResourceLocationArgument.id())
                                                        .executes(context -> activateBuff(context.getSource(),
                                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                                ResourceLocationArgument.getId(context, "id"))))))
                                .then(Commands.literal("stop")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .then(Commands.argument("id", ResourceLocationArgument.id())
                                                        .executes(context -> stopBuff(context.getSource(),
                                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                                ResourceLocationArgument.getId(context, "id"))))))))
                .then(Commands.literal("target")
                        .then(Commands.literal("level")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(context -> setTargetLevel(context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        IntegerArgumentType.getInteger(context, "level")))))))
                .then(Commands.literal("bindings")
                        .executes(context -> bindings(context.getSource())))
                .then(Commands.literal("clean")
                        .executes(context -> clean(context.getSource()))));
    }

    private static int debugTarget(CommandSourceStack source, net.minecraft.core.BlockPos pos) {
        ServerLevel level = source.getLevel();
        var machine = IMachine.ofMachine(level, pos).orElse(null);
        if (machine == null) {
            source.sendFailure(Component.translatable("message.mbd2_booster.not_mbd2_machine"));
            return 0;
        }
        BoostEffect effect = BoostService.effectFor(machine);
        machine.getHolder().getCapability(BoosterCapabilities.TARGET_EVOLUTION).ifPresent(target -> {
            int effectiveLevel = BoostService.effectiveTargetLevel(level, target.getTargetUuid(), target.getLevel());
            var base = BoostService.boundBase(level, target.getTargetUuid()).map(Object::toString).orElse("none");
            source.sendSuccess(() -> Component.translatable("message.mbd2_booster.target_info", target.getLevel(), effectiveLevel, base), false);
        });
        source.sendSuccess(() -> Component.literal("effect speed=%s energyIn=%s energyOut=%s itemOut=%s fluidOut=%s parallel=%s"
                .formatted(effect.speed(), effect.energyInput(), effect.energyOutput(), effect.itemOutput(), effect.fluidOutput(), effect.parallelBonus()))
                .withStyle(ChatFormatting.AQUA), false);
        return 1;
    }

    private static int dirty(CommandSourceStack source, net.minecraft.core.BlockPos pos) {
        BoostService.markDirty(source.getLevel(), new GlobalPosKey(source.getLevel().dimension(), pos));
        source.sendSuccess(() -> Component.translatable("message.mbd2_booster.debug_dirty"), true);
        return 1;
    }

    private static int setBaseLevel(CommandSourceStack source, net.minecraft.core.BlockPos pos, int level) {
        if (!(source.getLevel().getBlockEntity(pos) instanceof BoosterBaseBlockEntity base)) {
            source.sendFailure(Component.literal("No booster base at " + pos.toShortString()));
            return 0;
        }
        base.setBaseLevel(level);
        BoosterSavedData.get(source.getLevel()).rememberBase(base.baseUuid(), base.globalPos());
        BoostService.refreshBase(base);
        source.sendSuccess(() -> Component.literal("booster base level=" + base.baseLevel()), true);
        return 1;
    }

    private static int activateBuff(CommandSourceStack source, net.minecraft.core.BlockPos pos, ResourceLocation id) {
        if (!(source.getLevel().getBlockEntity(pos) instanceof BoosterBaseBlockEntity base)) {
            source.sendFailure(Component.literal("No booster base at " + pos.toShortString()));
            return 0;
        }
        boolean success = base.activateBuff(id);
        if (!success) {
            source.sendFailure(Component.literal("Failed to activate buff " + id));
            return 0;
        }
        BoostService.refreshBase(base);
        source.sendSuccess(() -> Component.literal("activated buff " + id), true);
        return 1;
    }

    private static int stopBuff(CommandSourceStack source, net.minecraft.core.BlockPos pos, ResourceLocation id) {
        if (!(source.getLevel().getBlockEntity(pos) instanceof BoosterBaseBlockEntity base)) {
            source.sendFailure(Component.literal("No booster base at " + pos.toShortString()));
            return 0;
        }
        boolean success = base.stopBuff(id);
        if (!success) {
            source.sendFailure(Component.literal("No active buff " + id));
            return 0;
        }
        BoostService.refreshBase(base);
        source.sendSuccess(() -> Component.literal("stopped buff " + id), true);
        return 1;
    }

    private static int setTargetLevel(CommandSourceStack source, net.minecraft.core.BlockPos pos, int level) {
        var machine = IMachine.ofMachine(source.getLevel(), pos).orElse(null);
        if (machine == null) {
            source.sendFailure(Component.translatable("message.mbd2_booster.not_mbd2_machine"));
            return 0;
        }
        var target = machine.getHolder().getCapability(BoosterCapabilities.TARGET_EVOLUTION).orElse(null);
        if (target == null) {
            source.sendFailure(Component.translatable("message.mbd2_booster.not_mbd2_machine"));
            return 0;
        }
        target.setLevel(level);
        machine.getHolder().setChanged();
        BoostService.markDirty(source.getLevel(), new GlobalPosKey(source.getLevel().dimension(), pos));
        source.sendSuccess(() -> Component.literal("target level=" + target.getLevel()), true);
        return 1;
    }

    private static int bindings(CommandSourceStack source) {
        var data = BoosterSavedData.get(source.getLevel());
        int count = 0;
        for (var record : data.allRecords()) {
            count++;
            source.sendSuccess(() -> Component.literal("%s -> %s active=%s machine=%s"
                    .formatted(record.targetUuid(), record.targetPos().display(), record.active(), record.machineId())), false);
        }
        final int total = count;
        source.sendSuccess(() -> Component.literal("bindings=" + total), false);
        return count;
    }

    private static int clean(CommandSourceStack source) {
        int removed = BoosterSavedData.get(source.getLevel()).cleanInvalid(source.getLevel());
        source.sendSuccess(() -> Component.literal("removed invalid bindings=" + removed), true);
        return removed;
    }
}
