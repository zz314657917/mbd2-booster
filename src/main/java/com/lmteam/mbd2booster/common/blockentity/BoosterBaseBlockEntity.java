package com.lmteam.mbd2booster.common.blockentity;

import com.lmteam.mbd2booster.MBD2Booster;
import com.lmteam.mbd2booster.common.MBD2BoosterConfig;
import com.lmteam.mbd2booster.common.data.BoosterSavedData;
import com.lmteam.mbd2booster.common.data.GlobalPosKey;
import com.lmteam.mbd2booster.common.effect.ActiveBuff;
import com.lmteam.mbd2booster.common.effect.BoosterDefinitions;
import com.lmteam.mbd2booster.common.effect.BuffDefinition;
import com.lmteam.mbd2booster.common.registry.BoosterRegistry;
import com.lmteam.mbd2booster.common.service.BoostService;
import com.lmteam.mbd2booster.common.util.SerializableEnergyStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BoosterBaseBlockEntity extends BlockEntity {
    private UUID baseUuid = UUID.randomUUID();
    private UUID ownerUuid;
    private int baseLevel = 1;
    private final ItemStackHandler inventory = new ItemStackHandler(18) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final SerializableEnergyStorage energy = new SerializableEnergyStorage(1_000_000, 10_000, 10_000, this::setChanged);
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> inventory);
    private final LazyOptional<SerializableEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private final List<ActiveBuff> activeBuffs = new ArrayList<>();
    private int tickCounter;

    public BoosterBaseBlockEntity(BlockPos pos, BlockState state) {
        super(BoosterRegistry.BOOSTER_BASE_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BoosterBaseBlockEntity base) {
        base.serverTick();
    }

    private void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        tickCounter++;
        if (tickCounter % Math.max(1, MBD2BoosterConfig.DEFAULT_UPDATE_INTERVAL.get()) != 0) return;
        var changed = false;
        var iterator = activeBuffs.iterator();
        while (iterator.hasNext()) {
            var active = iterator.next();
            var definition = BoosterDefinitions.buff(active.id()).orElse(null);
            if (definition == null) {
                active.pausedReason("definition_missing");
                changed = true;
                continue;
            }
            if (active.paused()) {
                if (tryPayCost(definition, true)) {
                    payCost(definition);
                    active.pausedReason("");
                    active.nextCostTicks(definition.costInterval());
                    markTargetsDirty(serverLevel);
                    changed = true;
                }
                continue;
            }
            active.nextCostTicks(active.nextCostTicks() - MBD2BoosterConfig.DEFAULT_UPDATE_INTERVAL.get());
            if (active.nextCostTicks() <= 0) {
                if (tryPayCost(definition, true)) {
                    payCost(definition);
                    active.nextCostTicks(definition.costInterval());
                } else {
                    active.pausedReason("missing_cost");
                    markTargetsDirty(serverLevel);
                    changed = true;
                    continue;
                }
            }
            active.remainingTicks(active.remainingTicks() - MBD2BoosterConfig.DEFAULT_UPDATE_INTERVAL.get());
            if (active.remainingTicks() <= 0) {
                iterator.remove();
                markTargetsDirty(serverLevel);
                changed = true;
            }
        }
        BoostService.refreshBase(this);
        BoosterSavedData.get(serverLevel).rememberBase(baseUuid, globalPos());
        if (changed) {
            setChanged();
        }
    }

    public UUID baseUuid() {
        return baseUuid;
    }

    public int baseLevel() {
        return baseLevel;
    }

    public void setBaseLevel(int baseLevel) {
        int next = Math.max(1, baseLevel);
        if (this.baseLevel != next) {
            this.baseLevel = next;
            setChanged();
            if (level instanceof ServerLevel serverLevel) {
                markTargetsDirty(serverLevel);
            }
        }
    }

    public void ensureOwner(ServerPlayer player) {
        if (ownerUuid == null) {
            ownerUuid = player.getUUID();
            setChanged();
        }
    }

    public boolean canManage(ServerPlayer player) {
        return ownerUuid == null || ownerUuid.equals(player.getUUID()) || player.hasPermissions(2);
    }

    public GlobalPosKey globalPos() {
        return new GlobalPosKey(level.dimension(), worldPosition);
    }

    public List<ActiveBuff> activeBuffs() {
        return Collections.unmodifiableList(activeBuffs);
    }

    public void sendSummary(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("MBD2 Booster base " + baseUuid + " level " + baseLevel).withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Active buffs: " + activeBuffs.size()).withStyle(ChatFormatting.GRAY));
    }

    public boolean activateBuff(ResourceLocation id) {
        var definition = BoosterDefinitions.buff(id).orElse(null);
        if (definition == null) return false;
        var baseDef = BoosterDefinitions.base(MBD2Booster.id("core")).orElse(null);
        var maxActive = baseDef == null ? 1 : baseDef.maxActiveBuffs();
        if (activeBuffs.size() >= maxActive) return false;
        if (!tryPayCost(definition, true)) return false;
        payCost(definition);
        activeBuffs.add(new ActiveBuff(id, definition.durationTicks(), definition.costInterval()));
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            markTargetsDirty(serverLevel);
        }
        return true;
    }

    public boolean stopBuff(ResourceLocation id) {
        boolean removed = activeBuffs.removeIf(activeBuff -> activeBuff.id().equals(id));
        if (removed) {
            setChanged();
            if (level instanceof ServerLevel serverLevel) {
                markTargetsDirty(serverLevel);
            }
        }
        return removed;
    }

    public void markTargetsDirty(ServerLevel serverLevel) {
        var data = BoosterSavedData.get(serverLevel);
        for (var targetUuid : data.getTargets(baseUuid)) {
            data.getRecord(targetUuid).ifPresent(record -> BoostService.markDirty(serverLevel, record.targetPos()));
        }
    }

    public void onRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            markTargetsDirty(serverLevel);
            BoosterSavedData.get(serverLevel).removeBase(baseUuid);
            BoostService.removeBase(baseUuid);
        }
    }

    private boolean tryPayCost(BuffDefinition definition, boolean includeExternal) {
        if (definition.energyCost() > 0 && energy.getEnergyStored() < definition.energyCost()) {
            return false;
        }
        for (var cost : definition.costs()) {
            if (!canExtract(cost.stack(), includeExternal)) {
                return false;
            }
        }
        return true;
    }

    private void payCost(BuffDefinition definition) {
        if (definition.energyCost() > 0) {
            energy.extractEnergy(definition.energyCost(), false);
        }
        for (var cost : definition.costs()) {
            extract(cost.stack());
        }
        setChanged();
    }

    private boolean canExtract(ItemStack requested, boolean includeExternal) {
        var remaining = requested.copy();
        simulateExtract(inventory, remaining);
        if (includeExternal && !remaining.isEmpty()) {
            for (var side : Direction.values()) {
                var neighbor = level.getBlockEntity(worldPosition.relative(side));
                if (neighbor == null) continue;
                var optional = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite());
                if (optional.isPresent()) {
                    simulateExtract(optional.orElseThrow(IllegalStateException::new), remaining);
                }
                if (remaining.isEmpty()) break;
            }
        }
        return remaining.isEmpty();
    }

    private void extract(ItemStack requested) {
        var remaining = requested.copy();
        doExtract(inventory, remaining);
        if (!remaining.isEmpty()) {
            for (var side : Direction.values()) {
                var neighbor = level.getBlockEntity(worldPosition.relative(side));
                if (neighbor == null) continue;
                var optional = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite());
                optional.ifPresent(handler -> doExtract(handler, remaining));
                if (remaining.isEmpty()) break;
            }
        }
    }

    private static void simulateExtract(IItemHandler handler, ItemStack remaining) {
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            var extracted = handler.extractItem(slot, remaining.getCount(), true);
            if (ItemStack.isSameItemSameTags(extracted, remaining)) {
                remaining.shrink(extracted.getCount());
            }
        }
    }

    private static void doExtract(IItemHandler handler, ItemStack remaining) {
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            var extracted = handler.extractItem(slot, remaining.getCount(), true);
            if (ItemStack.isSameItemSameTags(extracted, remaining)) {
                handler.extractItem(slot, extracted.getCount(), false);
                remaining.shrink(extracted.getCount());
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("baseUuid")) baseUuid = tag.getUUID("baseUuid");
        if (tag.hasUUID("ownerUuid")) ownerUuid = tag.getUUID("ownerUuid");
        baseLevel = Math.max(1, tag.getInt("baseLevel"));
        inventory.deserializeNBT(tag.getCompound("inventory"));
        energy.deserializeNBT(tag.getCompound("energy"));
        activeBuffs.clear();
        var list = tag.getList("activeBuffs", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            activeBuffs.add(ActiveBuff.load(list.getCompound(i)));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putUUID("baseUuid", baseUuid);
        if (ownerUuid != null) tag.putUUID("ownerUuid", ownerUuid);
        tag.putInt("baseLevel", baseLevel);
        tag.put("inventory", inventory.serializeNBT());
        tag.put("energy", energy.serializeNBT());
        var list = new ListTag();
        for (var buff : activeBuffs) {
            list.add(buff.save());
        }
        tag.put("activeBuffs", list);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCap.cast();
        if (cap == ForgeCapabilities.ENERGY) return energyCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
        energyCap.invalidate();
    }
}
