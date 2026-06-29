package com.lmteam.mbd2booster.common.service;

import com.lowdragmc.mbd2.api.capability.recipe.RecipeCapability;
import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import com.lowdragmc.mbd2.api.recipe.content.Content;
import com.lowdragmc.mbd2.api.recipe.content.ContentModifier;
import com.lowdragmc.mbd2.common.capability.recipe.FluidRecipeCapability;
import com.lowdragmc.mbd2.common.capability.recipe.ForgeEnergyRecipeCapability;
import com.lowdragmc.mbd2.common.capability.recipe.ItemRecipeCapability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class RecipeOutputModifier {
    private RecipeOutputModifier() {
    }

    static MBDRecipe copyInputs(MBDRecipe recipe, double energyInputMultiplier) {
        Map<RecipeCapability<?>, List<Content>> inputs = new HashMap<>();
        for (var entry : recipe.inputs.entrySet()) {
            var modifier = entry.getKey() == ForgeEnergyRecipeCapability.CAP ? ContentModifier.multiplier(energyInputMultiplier) : null;
            inputs.put(entry.getKey(), copyContents(entry.getKey(), entry.getValue(), modifier));
        }
        return new MBDRecipe(recipe.recipeType, recipe.id, inputs, recipe.copyContents(recipe.outputs, false, null),
                recipe.conditions, recipe.data, recipe.duration, recipe.isFuel, recipe.isXEIHidden, recipe.priority);
    }

    static MBDRecipe copyOutputs(MBDRecipe recipe, double itemMultiplier, double fluidMultiplier, double energyMultiplier) {
        Map<RecipeCapability<?>, List<Content>> outputs = new HashMap<>();
        for (var entry : recipe.outputs.entrySet()) {
            double multiplier = multiplierFor(entry.getKey(), itemMultiplier, fluidMultiplier, energyMultiplier);
            outputs.put(entry.getKey(), copyChanceContents(entry.getKey(), entry.getValue(), multiplier));
        }
        return new MBDRecipe(recipe.recipeType, recipe.id, recipe.copyContents(recipe.inputs, false, null), outputs,
                recipe.conditions, recipe.data, recipe.duration, recipe.isFuel, recipe.isXEIHidden, recipe.priority);
    }

    private static double multiplierFor(RecipeCapability<?> capability, double itemMultiplier, double fluidMultiplier, double energyMultiplier) {
        if (capability == ItemRecipeCapability.CAP) {
            return itemMultiplier;
        }
        if (capability == FluidRecipeCapability.CAP) {
            return fluidMultiplier;
        }
        if (capability == ForgeEnergyRecipeCapability.CAP) {
            return energyMultiplier;
        }
        return 1d;
    }

    private static List<Content> copyContents(RecipeCapability<?> capability, List<Content> contents, ContentModifier modifier) {
        List<Content> copied = new ArrayList<>(contents.size());
        for (Content content : contents) {
            copied.add(content.copy(capability, modifier));
        }
        return copied;
    }

    private static List<Content> copyChanceContents(RecipeCapability<?> capability, List<Content> contents, double multiplier) {
        if (multiplier == 1d) {
            return copyContents(capability, contents, null);
        }
        if (multiplier <= 0d) {
            return List.of();
        }
        int whole = (int) Math.floor(multiplier);
        float fractional = (float) (multiplier - whole);
        List<Content> copied = new ArrayList<>(contents.size() * Math.max(1, whole + (fractional > 0f ? 1 : 0)));
        if (whole > 0) {
            for (Content content : contents) {
                copied.add(content.copy(capability, ContentModifier.multiplier(whole)));
            }
        }
        if (fractional > 0f) {
            for (Content content : contents) {
                Content chanceContent = content.copy(capability, null);
                chanceContent.chance = content.chance * fractional;
                copied.add(chanceContent);
            }
        }
        return copied;
    }
}
