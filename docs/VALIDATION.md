# Validation

## 0.1.2

The Engineering server was parked in a datapack `/forceload` command while C2ME's
workers were idle. Jake's Build Tools 2.0.1.5 loads a helper at X=-20,000,000 in
all three dimensions. With DH 0.3.5's signed 24-bit X packing, that position
decodes as X=-3,222,784. The helper cannot be safely processed at its original
coordinate.

- An isolated client with DH, Sable, Moonlight, Supplementaries, C2ME
  0.4.0-alpha.0.124, and the original JBT jar stalled with a null-DataLayer
  lighting exception before this fix (`build/c2me-forceload-client.log`).
- Relocating JBT helpers at function compilation allows the same combination
  to load and checks high/low blocks and actual server/client block light in a
  -128..1024 world (`build/jbt-fix-client.log`).
- The -64..32752 client checks real JBT forced chunks in all three dimensions,
  executes the actual rotation macro against the relocated scratch block, and
  verifies coordinate packing, macro rewriting, unrelated namespaces, high/low
  blocks, and server/client light (`build/jbt-32k-client.log`).
- A new process reopens that same 32K world and verifies persisted blocks, bounds,
  JBT helpers, and lighting (`build/jbt-32k-reload.log`). A separate fresh run
  also passes placement, client sync, and block lighting at Y=32720
  (`build/jbt-32k-ceiling.log`).
- All four dedicated tests pass with DH, Sable, Moonlight, Supplementaries, JBT,
  and C2ME at the corrected test origin (`build/jbt-gametest.log`). All four
  also pass without Sable or C2ME (`build/jbt-without-sable-gametest.log`).

Vanilla GameTestServer chooses random arena coordinates within ±15M regardless
of its spawn argument. Earlier intermittent dedicated failures used locations
outside DH's ±8M range (e.g. X=9,379,195). The test-only origin mixin addresses
that fixture problem. The bundled `LightStorageWindowPruneMixin` was investigated
but is absent from DH's active mixin configuration; it is not patched.

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

Earlier dedicated failures remain recorded above as failed attempts. Version
0.1.2 identifies their unsupported horizontal test coordinates and uses a valid
test origin. The accepted lower bound of this upstream build is -128, even when
a lower floor is requested. General operation outside DH's horizontal packing
range remains unsupported; the JBT fix only moves that integration's helpers.

The whole Engineering modpack, Lithium compatibility, moving Sable structures,
and extreme heights have not been certified. This is an alpha for the initial
startup failure, not a claim that every interaction is fixed.
