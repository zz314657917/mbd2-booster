# MBD2 Booster Agent Notes

## Project Scope

- This is an independent Forge 1.20.1 addon mod for Multiblocked2, not a patch inside the MBD2 upstream repository.
- Keep changes scoped to this repository unless the user explicitly asks to modify `F:/mcmod/Multiblocked2-1.20.1`.
- Default language for notes and replies is Simplified Chinese.

## Context Entry

- Start with `knowledge/00-start-here.md`.
- For active continuation, read `knowledge/tasks/current-task.md`.
- For recent history, read `knowledge/tasks/timeline.md`.
- For build and runtime verification, read `knowledge/build-and-verify.md`.

## Build

- Use Windows native Gradle wrapper:

```powershell
$OutputEncoding = [Console]::OutputEncoding = [Text.UTF8Encoding]::new($false)
./gradlew.bat build
```

## Local Dependency Boundary

- MBD2 is currently consumed from `../Multiblocked2-1.20.1/build/libs/multiblocked2-1.20.1-1.0.38.a.jar`.
- Build passes with a ForgeGradle warning that `files(...)` dependencies are not deobfuscated. Treat this as acceptable only while that jar is development-named enough for compilation.
- If runtime or CI later uses an obfuscated/release MBD2 jar, publish/install a dev jar with a real local Maven coordinate and update `build.gradle`.

## Implementation Rules

- Recipe speed, energy and output changes should stay on `MachineRecipeModifyEvent.Before`.
- Parallel changes should stay on MBD2 `getMaxParallel` via the small mixins, not by multiplying an already-parallel recipe after setup.
- Any base/buff/target/binding/config state change that affects results must call `RecipeLogic.markLastRecipeDirty()` on loaded targets.
- Target evolution data stays on MBD2 machine block entity capability; first version does not migrate target level into dropped machine items.
- UI injection into MBD2 machine screens is not implemented yet; keep binding tool and commands as stable fallback until LDLib UI injection is verified.

## Safety

- Do not commit generated `build/`, `.gradle/`, `run/`, logs or crash dumps.
- Do not store tokens, private server addresses, accounts, or internal secrets in `knowledge/`.
