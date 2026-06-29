package com.lmteam.mbd2booster.common.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;

public record GlobalPosKey(ResourceKey<Level> dimension, BlockPos pos) {
    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putString("dimension", dimension.location().toString());
        tag.putLong("pos", pos.asLong());
        return tag;
    }

    public static GlobalPosKey load(CompoundTag tag) {
        var dim = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, new ResourceLocation(tag.getString("dimension")));
        return new GlobalPosKey(dim, BlockPos.of(tag.getLong("pos")));
    }

    public String display() {
        return "%s %d,%d,%d".formatted(dimension.location(), pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GlobalPosKey other)) return false;
        return dimension.equals(other.dimension) && pos.equals(other.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, pos);
    }
}
