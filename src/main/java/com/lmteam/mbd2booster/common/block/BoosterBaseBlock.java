package com.lmteam.mbd2booster.common.block;

import com.lmteam.mbd2booster.common.blockentity.BoosterBaseBlockEntity;
import com.lmteam.mbd2booster.common.registry.BoosterRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BoosterBaseBlock extends BaseEntityBlock {
    public BoosterBaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoosterBaseBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof ServerPlayer player && level.getBlockEntity(pos) instanceof BoosterBaseBlockEntity base) {
            base.assignOwner(player.getUUID());
        }
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != BoosterRegistry.BOOSTER_BASE_ENTITY.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) -> BoosterBaseBlockEntity.serverTick(tickerLevel, pos, tickerState, (BoosterBaseBlockEntity) blockEntity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BoosterBaseBlockEntity base && player instanceof ServerPlayer serverPlayer) {
            base.ensureOwner(serverPlayer);
            base.sendSummary(serverPlayer);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!oldState.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BoosterBaseBlockEntity base) {
            base.onRemoved();
        }
        super.onRemove(oldState, level, pos, newState, isMoving);
    }
}
