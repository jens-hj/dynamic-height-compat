# Validation

## 0.1.3

The Engineering retry reached the renderer and crashed in Voxy 0.2.16-beta:
`Position not valid at all levels: ...4@[54, 12, 26]:0@[864, -64, 416]`.
Level-4 Y=12 expands to base-section Y=192, which does not fit its signed byte.
Xaero's map worker subsequently waited for the render thread while holding a
world capability lock; server shutdown waited for that lock, leaving the loading
screen visible and delaying the crash report. The Xaero shutdown path is not
modified by this release.

- The exact installed Voxy/Roxy/Sodium jars are used in isolated Xvfb tests:
  Voxy `H3w2nVdU` (the 1.21.11 Fabric jar), Roxy `VnJRhuAn` (0.4.1), Sodium
  0.8.13; Minecraft 1.21.1 / NeoForge 21.1.266, DH 0.3.5, Sable 2.0.6,
  Moonlight 3.8.3, Supplementaries 4.0.2, and JBT 2.0.1.5.
- Disabling only the three Voxy hooks reproduces the exact original
  `NodeManager.assertPosValid` exception (`build/voxy-negative-control.log`).
  That negative control is expected to fail.
- A complete 32K startup with the actual enabled Voxy pipeline passes
  (`build/voxy-32k-client.log`). Exhaustive CPU tests cover 4096 distinct Y keys,
  legacy-key preservation, unchanged X/Z/level fields, and all five LOD levels.
- The loaded, Roxy-processed position shader is executed as an OpenGL compute
  shader for all 4096 Y values, checking X, Y, Z, and level. The normal Voxy
  render pipeline remains active, its async worker remains healthy, and the
  diamond block at Y=32720 is found in Voxy's actual section data
  (`build/voxy-gpu-ingest-verified.log`).
- A new JVM reopens the world with Voxy ingestion disabled and still finds that
  high-altitude diamond in the rendered world's stored section data. GPU checks
  and cache isolation also pass (`build/voxy-cold-cache-reload.log`).
- A fresh combined run adds C2ME 0.4.0-alpha.0.124 and enabled Voxy World Gen V2
  2.4.2 (test radius 64, one active generation task). It passes GPU decoding,
  live rendering, actual Y=32720 ingestion, the previous integrations, world
  save, and shutdown (`build/voxy-combined-pack-client.log`). Voxy logs its
  pending section-save count while draining the queue, then completes shutdown.
- All five dedicated tests pass without Voxy installed, with C2ME and the
  previous integrations (`build/voxy-final-gametest.log`). This also checks that
  the new integration remains optional.
- The shader tests run on llvmpipe / Mesa 26.2.5, OpenGL 4.6. One earlier 3G test
  exhausted its heap; the completed Voxy ingestion/cache tests use a 6G heap.
  This is isolated validation, not certification of the full Engineering pack
  or its AMD/Iris/shader combination.

The four low bits are reserved by this Voxy version. Its installed RocksDB
backend's `swizzlePos` returns the complete key unchanged; NodeStore/geometry
uploads retain the complete key, and the shared position shader decodes it.
Both filesystem backends use `ConfigBuildCtx.ensurePathExists`, where the
separate cache directory is selected. Original Voxy jars, cache data, and player
Engineering player worlds are not modified during test runs.

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
- An additional run with Sable but without C2ME completed all four assertions,
  then spent several minutes in synchronous `RegionFile` writes during shutdown.
  It was stopped after 7m44s total runtime; Gradle consequently reports that
  invocation as failed. It is not counted as a completed test run
  (`build/jbt-final-gametest.log`, `build/gametest-shutdown-threads.txt`). The two
  completed dedicated configurations above exited successfully.

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
