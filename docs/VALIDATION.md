# Validation

## 0.1.1

Minecraft 1.21.1 / NeoForge 21.1.266 with Dynamic Height 0.3.5, Sable 2.0.6,
Moonlight 3.8.3 (`yBea1yLM`), and Supplementaries 4.0.2 (`rWq0ko29`).

- A controlled constructor-replacement fixture reproduces the user's new
  `GenerationBounds.from` null-cache crash with the old 0.1.0 implementation.
  It forces an outer cache replacement so development mixin ordering cannot
  accidentally hide the production bug.
- The revised implementation passes that same forced-replacement check using
  Moonlight's actual `FakeServerLevel` and dummy chunk cache. It checks inherited
  generator identity/bounds, fake empty-chunk behavior, temporary-state cleanup,
  and isolation from the parent level.
- The isolated client with all four mods starts successfully, including
  Supplementaries' automatic fake-world initialization. It passes block placement
  at Y=512 and Y=-96, client synchronization, and a full save/restart preserving
  the extended -128..1024 range and both blocks.
- Dedicated GameTests were attempted but were blocked by the already-documented
  `LayerLightSectionStorage.getStoredLevel` lighting exception before all three
  tests could complete. They are not reported as passing. A test-only initial
  height fixture did not resolve that issue and was removed.

This alpha carries forward the existing lighting limitation; the update targets
the newly reported Moonlight/Supplementaries startup path only.

## 0.1.0

Minecraft 1.21.1, NeoForge 21.1.266, Dynamic Height 0.3.5 (`KUDC8Jhh`),
Sable 2.0.6 (`fg9dTRz9`), Java 21 on Linux. Sable's embedded Veil 4.3.2 and
Sable Companion 1.6.0 are included in the integration runtime.

- `./gradlew build compileTestmodJava`: passed.
- `./gradlew runGameTestServer -Pbaseline`: reproduced the original
  `GenerationBounds.from` / unassigned `ServerLevel.getChunkSource()` crash.
- `./gradlew runGameTestServer`: both required tests passed with the fix.
  Checks generator bounds, cleanup of the temporary generator, basic block
  placement, and unchanged handling of non-server height accessors.
  A final fresh-world run with an explicit spawn argument passed both tests again.
  Sable still relocates the test arena, so the argument does not make that arena
  deterministic; other dedicated runs exposed the lighting issue below.
- Isolated Xvfb client test: created a -128..1024 world, placed a diamond block
  at Y=512 and a gold block at Y=-96, saved all dimensions, and verified client
  height and block synchronization.
- A second isolated client process reopened that saved world and verified both
  blocks and extended height settings survived the restart.
- `./gradlew modrinth -PmodrinthDryRun`: generated the alpha upload metadata
  without uploading. Includes the required Dynamic Height version and optional
  Sable integration version.
- Jar inspection: metadata version 0.1.0, original icon, no test classes,
  no bundled Dynamic Height/Sable classes.

Each client invocation used its actual `build/run/clientTest/options.txt` with
master/music volume zero, accessibility onboarding off, narrator off, and an
isolated Xvfb display. The Engineering instance and player saves were untouched.

## Known limits

Dedicated GameTest runs at Dynamic Height's default 32K upper limit, a repeated
-128..1024 run, and a separate run without Sable hit a `LayerLightSectionStorage.getStoredLevel`
null-DataLayer exception. Those runs were stopped and are not recorded as passing
tests. The addon loaded without the optional Sable dependency, but that test did
not finish. This startup fix does not address that lighting issue. The accepted
lower bound of this upstream build is -128, even when a lower floor is requested.

The whole Engineering modpack, Lithium compatibility, moving Sable structures,
and extreme heights have not been certified. This is an alpha for the initial
startup failure, not a claim that every interaction is fixed.
