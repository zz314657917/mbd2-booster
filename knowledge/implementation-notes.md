# Implementation Notes

## Recipe Effect Path

`BoosterEventHandlers` listens to `MachineRecipeModifyEvent.Before`. It calls `BoostService.effectFor(machine)` and then `BoostService.applyRecipeEffect(recipe, effect)`.

Effects are split by role:

- `speed`: converts to a duration multiplier, clamped by server config.
- `energyInput`: scales Forge Energy input contents.
- `energyOutput`: scales Forge Energy output contents.
- `itemOutput`: scales item output contents.
- `fluidOutput`: scales fluid output contents.

Output multipliers use integer plus fractional chance. The integer part scales content amount; the fractional part adds a second chance content with adjusted `Content.chance`.

## Parallel Path

Parallel bonus is not applied by post-processing recipes. It is added to MBD2 max parallel through:

- `MBDMachineMixin`
- `MBDMultiblockMachineMixin`

Both inject at `getMaxParallel` return and merge a `ContentModifier.addition(parallelBonus)`.

## Dirty Refresh Rules

The first implementation calls dirty refresh for these state changes:

- Buff activation.
- Buff stop.
- Buff pause, resume and expiration during base ticking.
- Base level change.
- Target level command.
- Bind and unbind.
- Base removal.
- KubeJS server reload.

Dirty refresh only marks loaded target machines. Offline targets are retained in `BoosterSavedData` and refreshed when active paths resolve them later.

## Data Model

- Base block entity stores `baseUuid`, `ownerUuid`, base level, inventory, FE and active buffs.
- Target MBD2 block entity gets attached `TargetEvolution` capability with `targetUuid`, level and experience.
- `BoosterSavedData` stores target/base indexes, base positions and last target machine record.

## Current Limitations

- No MBD2 machine UI injection yet. Binding tool and op commands are the stable fallback.
- No item-drop migration for target level. Breaking the target invalidates binding; target level is not written into drops.
- KubeJS `tags` target matching from the original plan is not implemented yet; machine ID matching is implemented.
- The current DSL validates through builders and clamping but does not yet produce a detailed script-side validation report.
- Runtime game smoke has not been executed yet.
