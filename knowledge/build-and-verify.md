# Build And Verification

## Build Command

Run from `F:/mcmod/mbd2-booster`:

```powershell
$OutputEncoding = [Console]::OutputEncoding = [Text.UTF8Encoding]::new($false)
./gradlew.bat build
```

## Last Verified Build

- Date: 2026-06-28
- Command: `./gradlew.bat build`
- Result: `BUILD SUCCESSFUL`
- Artifact: `build/libs/mbd2_booster-1.20.1-0.1.0.jar`

## Known Build Warning

ForgeGradle prints:

```text
files(...) dependencies are not deobfuscated.
Cannot deobfuscate dependency of type DefaultSelfResolvingDependency_Decorated, using obfuscated version!
```

Reason: MBD2 is currently referenced as a direct file dependency:

```gradle
implementation fg.deobf(files('../Multiblocked2-1.20.1/build/libs/multiblocked2-1.20.1-1.0.38.a.jar'))
```

This compiled successfully with the current local jar. If the jar is replaced by an obfuscated release jar or CI needs reproducibility, publish a local dev jar coordinate and update the dependency.

## Runtime Smoke Plan

1. Install MBD2, LDLib and this jar on a Forge 1.20.1 test server/client.
2. Place a booster base and an MBD2 target machine in the same dimension.
3. Use binding tool: right-click base, then target.
4. Run `/mbd2booster target level <targetPos> 2`.
5. Run `/mbd2booster base buff activate <basePos> mbd2_booster:overdrive`.
6. Start a target recipe and confirm the next setup uses speed, FE and output changes.
7. Confirm `/mbd2booster dirty <targetPos>` causes the next setup to refresh effects.
8. Test `/reload`, buff stop, target unbind and base removal.
9. Repeat with MBD2Thread if available.
