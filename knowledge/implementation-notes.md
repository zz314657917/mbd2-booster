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
- A stored target level only becomes effective while the target is bound to a booster base. When unbound, the stored level is retained but the effective level is `0`.

## Binding And Cost Safety

- Binding tool selections include base UUID, dimension and position. If the block at that position is replaced by another base, the tool clears the selection and requires selecting again.
- Shift-unbind requires the selected base to own the target binding, or the player to be an operator, or the player to manage the actual owning base resolved from saved data.
- Successful binding marks the target machine holder dirty so a newly generated target UUID is persisted with the target capability.
- Buff material costs are merged by item and NBT before simulation and extraction, so duplicate cost entries must be backed by the combined item count.
- Cross-dimension binding is intentionally unsupported in the first release and is enforced in code, not only by config.

## Current Limitations

- No MBD2 machine UI injection yet. Binding tool and op commands are the stable fallback.
- No item-drop migration for target level. Breaking the target invalidates binding; target level is not written into drops.
- KubeJS `tags` target matching from the original plan is not implemented yet; machine ID matching is implemented.
- The current DSL validates through builders and clamping but does not yet produce a detailed script-side validation report.
- Runtime game smoke has not been executed yet.
