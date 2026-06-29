# MBD2 Booster Knowledge Entry

## What This Repo Is

`mbd2_booster` is a standalone Forge 1.20.1 addon for Multiblocked2. It adds a booster base, a binding tool, target evolution data, recipe effect calculation, KubeJS definitions, debug commands and small mixins for MBD2 parallel limits.

## Key Files

- `src/main/java/com/lmteam/mbd2booster/common/service/BoostService.java`: runtime effect cache, dirty refresh, recipe effect application.
- `src/main/java/com/lmteam/mbd2booster/common/service/RecipeOutputModifier.java`: recipe input/output copy and multiplier logic.
- `src/main/java/com/lmteam/mbd2booster/common/blockentity/BoosterBaseBlockEntity.java`: base inventory, FE, active buffs and interval cost.
- `src/main/java/com/lmteam/mbd2booster/common/item/BindingToolItem.java`: base selection and target bind/unbind fallback.
- `src/main/java/com/lmteam/mbd2booster/common/data/BoosterSavedData.java`: saved binding indexes and base positions.
- `src/main/java/com/lmteam/mbd2booster/common/BoosterEventHandlers.java`: capability attach, recipe modify event and commands.
- `src/main/java/com/lmteam/mbd2booster/mixin/`: MBD2 max parallel injection.
- `src/main/java/com/lmteam/mbd2booster/integration/kubejs/`: server-side KubeJS DSL.

## Current Design

- Speed, energy input, energy output, item output and fluid output are applied during `MachineRecipeModifyEvent.Before`.
- Parallel bonus is added through `MBDMachine#getMaxParallel` and `MBDMultiblockMachine#getMaxParallel`.
- Buffs and target levels only affect the next recipe setup after dirty refresh; running recipes keep their current snapshot.
- Cross-dimension binding is disabled by default in server config.
- Target level is stored on the MBD2 machine block entity capability and is not moved to dropped machine items in this first version.

## Useful Commands

```text
/mbd2booster debug target <pos>
/mbd2booster dirty <pos>
/mbd2booster bindings
/mbd2booster clean
/mbd2booster base level <pos> <level>
/mbd2booster base buff activate <pos> <id>
/mbd2booster base buff stop <pos> <id>
/mbd2booster target level <pos> <level>
```

## Next Knowledge Files

- `knowledge/build-and-verify.md`: build command, artifact path and warnings.
- `knowledge/implementation-notes.md`: architecture decisions and known limitations.
- `knowledge/tasks/current-task.md`: current handoff snapshot.
- `knowledge/tasks/timeline.md`: recent project history.
