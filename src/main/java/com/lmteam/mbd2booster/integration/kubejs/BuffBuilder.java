package com.lmteam.mbd2booster.integration.kubejs;

import com.lmteam.mbd2booster.common.effect.BoostEffect;
import com.lmteam.mbd2booster.common.effect.BuffDefinition;
import com.lmteam.mbd2booster.common.effect.ItemCost;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Map;

public class BuffBuilder {
    private final BuffDefinition definition;

    BuffBuilder(BuffDefinition definition) {
        this.definition = definition;
    }

    public BuffBuilder duration(int ticks) {
        definition.durationTicks(ticks);
        return this;
    }

    public BuffBuilder costInterval(int ticks) {
        definition.costInterval(ticks);
        return this;
    }

    public BuffBuilder energyCost(int energy) {
        definition.energyCost(energy);
        return this;
    }

    public BuffBuilder cost(Collection<?> costs) {
        definition.costs().clear();
        for (Object cost : costs) {
            if (cost instanceof Map<?, ?> map) {
                ItemStack stack = readStack(map);
                if (!stack.isEmpty()) {
                    definition.costs().add(new ItemCost(stack));
                }
            }
        }
        return this;
    }

    public BuffBuilder targets(Map<?, ?> targetSpec) {
        addResourceLocations(targetSpec.get("machines"), definition.targetMachines());
        return this;
    }

    public BuffBuilder deny(Map<?, ?> denySpec) {
        Object output = denySpec.get("output");
        if (output instanceof Map<?, ?> outputSpec) {
            addResourceLocations(outputSpec.get("machines"), definition.deniedOutputMachines());
        }
        return this;
    }

    public BuffBuilder effect(Map<?, ?> effectSpec) {
        definition.effect(new BoostEffect(
                readDouble(effectSpec, "speed", 1d),
                readDouble(effectSpec, "energyInput", 1d),
                readDouble(effectSpec, "energyOutput", 1d),
                readDouble(effectSpec, "itemOutput", 1d),
                readDouble(effectSpec, "fluidOutput", 1d),
                readInt(effectSpec, "parallelBonus", 0)
        ));
        return this;
    }

    private static ItemStack readStack(Map<?, ?> map) {
        Object itemId = map.get("item");
        if (itemId == null) {
            return ItemStack.EMPTY;
        }
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId.toString()));
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, Math.max(1, readInt(map, "count", 1)));
    }

    private static void addResourceLocations(Object value, java.util.Set<ResourceLocation> target) {
        if (value instanceof Collection<?> collection) {
            for (Object entry : collection) {
                target.add(new ResourceLocation(entry.toString()));
            }
        } else if (value != null) {
            target.add(new ResourceLocation(value.toString()));
        }
    }

    private static int readInt(Map<?, ?> properties, String key, int fallback) {
        Object value = properties.get(key);
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static double readDouble(Map<?, ?> properties, String key, double fallback) {
        Object value = properties.get(key);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }
}
