package com.lmteam.mbd2booster.common.capability;

import com.lmteam.mbd2booster.MBD2Booster;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TargetEvolutionProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final ResourceLocation ID = MBD2Booster.id("target_evolution");
    private final TargetEvolution data = new TargetEvolution();
    private final LazyOptional<TargetEvolution> optional = LazyOptional.of(() -> data);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BoosterCapabilities.TARGET_EVOLUTION.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.deserializeNBT(nbt);
    }
}
