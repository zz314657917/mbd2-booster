package com.lmteam.mbd2booster.common.item;

import com.lmteam.mbd2booster.common.blockentity.BoosterBaseBlockEntity;
import com.lmteam.mbd2booster.common.capability.BoosterCapabilities;
import com.lmteam.mbd2booster.common.data.BindingRecord;
import com.lmteam.mbd2booster.common.data.BoosterSavedData;
import com.lmteam.mbd2booster.common.data.GlobalPosKey;
import com.lmteam.mbd2booster.common.effect.BoosterDefinitions;
import com.lmteam.mbd2booster.common.service.BoostService;
import com.lmteam.mbd2booster.common.service.MachineIds;
import com.lowdragmc.mbd2.api.machine.IMachine;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class BindingToolItem extends Item {
    private static final String SELECTED_BASE = "selectedBase";
    private static final String SELECTED_DIMENSION = "selectedDimension";
    private static final String SELECTED_POS = "selectedPos";

    public BindingToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level) || !(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        BlockPos pos = context.getClickedPos();
        var blockEntity = level.getBlockEntity(pos);
        ItemStack stack = context.getItemInHand();
        if (blockEntity instanceof BoosterBaseBlockEntity base) {
            base.ensureOwner(player);
            if (!base.canManage(player)) {
                player.sendSystemMessage(Component.translatable("message.mbd2_booster.not_owner").withStyle(ChatFormatting.RED));
                return InteractionResult.CONSUME;
            }
            rememberBase(stack, base, level.dimension());
            BoosterSavedData.get(level).rememberBase(base.baseUuid(), base.globalPos());
            BoostService.refreshBase(base);
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.base_selected", base.globalPos().display()).withStyle(ChatFormatting.AQUA));
            return InteractionResult.CONSUME;
        }
        var machine = IMachine.ofMachine(blockEntity).orElse(null);
        if (machine == null) {
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.not_mbd2_machine").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        return handleTarget(stack, level, player, machine);
    }

    private InteractionResult handleTarget(ItemStack stack, ServerLevel targetLevel, ServerPlayer player, IMachine machine) {
        var selected = readSelectedBase(stack, targetLevel);
        if (selected == null) {
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.base_missing").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        if (!selected.dimension().equals(targetLevel.dimension())) {
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.cross_dimension").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        var baseLevel = targetLevel.getServer().getLevel(selected.dimension());
        if (baseLevel == null || !(baseLevel.getBlockEntity(selected.pos()) instanceof BoosterBaseBlockEntity base)) {
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.base_missing").withStyle(ChatFormatting.RED));
            clearSelectedBase(stack);
            return InteractionResult.CONSUME;
        }
        if (!base.baseUuid().equals(selected.baseUuid())) {
            clearSelectedBase(stack);
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.base_changed").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        if (!base.canManage(player)) {
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.not_owner").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        var targetData = machine.getHolder().getCapability(BoosterCapabilities.TARGET_EVOLUTION).orElse(null);
        if (targetData == null) {
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.not_mbd2_machine").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        var savedData = BoosterSavedData.get(targetLevel);
        var existingBase = savedData.getBoundBase(targetData.getTargetUuid()).orElse(null);
        if (player.isShiftKeyDown()) {
            if (existingBase == null) {
                player.sendSystemMessage(Component.translatable("message.mbd2_booster.not_bound").withStyle(ChatFormatting.RED));
                return InteractionResult.CONSUME;
            }
            if (!canUnbind(player, targetLevel, existingBase, base)) {
                player.sendSystemMessage(Component.translatable("message.mbd2_booster.not_owner").withStyle(ChatFormatting.RED));
                return InteractionResult.CONSUME;
            }
            savedData.unbind(targetData.getTargetUuid());
            BoostService.markDirty(targetLevel, new GlobalPosKey(targetLevel.dimension(), machine.getPos()));
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.target_unbound", targetData.getTargetUuid()).withStyle(ChatFormatting.YELLOW));
            return InteractionResult.CONSUME;
        }
        if (existingBase != null && !existingBase.equals(base.baseUuid())) {
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.already_bound").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        var baseDefinition = BoosterDefinitions.base(com.lmteam.mbd2booster.MBD2Booster.id("core")).orElse(null);
        int maxTargets = baseDefinition == null ? 1 : baseDefinition.levelOrDefault(base.baseLevel()).maxTargets();
        if (savedData.getTargets(base.baseUuid()).size() >= maxTargets && existingBase == null) {
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.max_targets", maxTargets).withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        savedData.rememberBase(base.baseUuid(), base.globalPos());
        var machineId = MachineIds.get(machine).orElse(null);
        if (machineId == null) {
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.not_mbd2_machine").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        boolean bound = savedData.bind(base.baseUuid(), new BindingRecord(targetData.getTargetUuid(),
                new GlobalPosKey(targetLevel.dimension(), machine.getPos()), machineId));
        if (bound) {
            machine.getHolder().setChanged();
            machine.markDirty();
            BoostService.refreshBase(base);
            BoostService.markDirty(targetLevel, new GlobalPosKey(targetLevel.dimension(), machine.getPos()));
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.target_bound", targetData.getTargetUuid()).withStyle(ChatFormatting.GREEN));
        } else {
            player.sendSystemMessage(Component.translatable("message.mbd2_booster.already_bound").withStyle(ChatFormatting.RED));
        }
        return InteractionResult.CONSUME;
    }

    private static void rememberBase(ItemStack stack, BoosterBaseBlockEntity base, ResourceKey<Level> dimension) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID(SELECTED_BASE, base.baseUuid());
        tag.putString(SELECTED_DIMENSION, dimension.location().toString());
        tag.putLong(SELECTED_POS, base.getBlockPos().asLong());
    }

    private static void clearSelectedBase(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(SELECTED_BASE);
        tag.remove(SELECTED_DIMENSION);
        tag.remove(SELECTED_POS);
    }

    private static SelectedBase readSelectedBase(ItemStack stack, ServerLevel fallbackLevel) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.hasUUID(SELECTED_BASE) || !tag.contains(SELECTED_DIMENSION) || !tag.contains(SELECTED_POS)) {
            return null;
        }
        ResourceKey<Level> dimension = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                new ResourceLocation(tag.getString(SELECTED_DIMENSION)));
        return new SelectedBase(tag.getUUID(SELECTED_BASE), dimension, BlockPos.of(tag.getLong(SELECTED_POS)));
    }

    private static boolean canUnbind(ServerPlayer player, ServerLevel level, java.util.UUID existingBase, BoosterBaseBlockEntity selectedBase) {
        if (existingBase.equals(selectedBase.baseUuid())) {
            return true;
        }
        if (player.hasPermissions(2)) {
            return true;
        }
        var data = BoosterSavedData.get(level);
        var ownerPos = data.getBasePos(existingBase).orElse(null);
        if (ownerPos == null) {
            return false;
        }
        var ownerLevel = level.getServer().getLevel(ownerPos.dimension());
        if (ownerLevel == null || !(ownerLevel.getBlockEntity(ownerPos.pos()) instanceof BoosterBaseBlockEntity ownerBase)) {
            return false;
        }
        return ownerBase.canManage(player);
    }

    private record SelectedBase(java.util.UUID baseUuid, ResourceKey<Level> dimension, BlockPos pos) {
    }
}
