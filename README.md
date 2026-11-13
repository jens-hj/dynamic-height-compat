# Dynamic Height Compat

Focused compatibility patches for Dynamic Height on Minecraft 1.21.1 / NeoForge.
The addon fixes Dynamic Height 0.3.5 / Sable 2.0.6 startup crashes, including
Moonlight 3.8.3 fake worlds used by Supplementaries 4.0.2. It also relocates
Jake's Build Tools 2.0.1.5 helpers to avoid world-loading hangs.
Further independently tested compatibility patches can be added as separate mixins.

## Implementation

Sable constructs an `EmptyLevelChunk` inside `ServerChunkCache` construction, before
the cache is assigned to `ServerLevel`. Dynamic Height's `GenerationBounds.from`
dereferences that unassigned cache.

The addon remembers the actual `LevelStem` generator before cache construction
can be replaced by another mod. During that window only, Dynamic Height receives
the generator's original minimum Y and generation depth. The reference is cleared
when level construction completes, and initialized worlds and client height accessors use Dynamic
Height's original method. This also supports custom generators without registry
lookups, guessed vanilla limits, or allocating the full extended height.

Moonlight can bypass the vanilla cache-constructor operation entirely for fake
levels. Capturing the generator earlier avoids depending on the ordering of
constructor wrappers. The regression fixture forces that bypass and verifies
fake-level bounds and cleanup against the actual Moonlight implementation.

Dynamic Height changes `BlockPos` packing to 24 horizontal bits and 16 vertical
bits. Its signed horizontal range is -8,388,608..8,388,607, so JBT's helper at
X=-20,000,000 cannot round-trip through a packed position. Loading that chunk can
hang or fail in lighting. The addon rewrites standalone `-20000000` coordinate
tokens to `-2000000` in `jbt` functions before compilation, including macro lines.
Load, tool, and uninstall functions therefore agree on the relocated scratch area.
Other namespaces are untouched; no JBT assets or code are redistributed.

Upstream jars are unmodified and are not included in this addon's jar.

## Development

Use the Nix flake (Gradle runs on Java 25; compilation and Minecraft use Java 21):

```sh
nix develop -c ./gradlew build compileTestmodJava
nix develop -c ./gradlew runGameTestServer
nix develop -c ./gradlew runGameTestServer -PwithoutSable
nix develop -c python scripts/headless-test.py ./gradlew runClientTest
nix develop -c python scripts/headless-test.py ./gradlew runClientTest -Preload
nix develop -c python scripts/headless-test.py ./gradlew runClientTest -Psupplementaries
nix develop -c ./gradlew modrinth -PmodrinthDryRun
```

`-Pbaseline` disables the fix for negative-control runs, which should reproduce the
original startup failure. Do not use it for ordinary gameplay.
`-PbaselineBuildTools` independently disables the helper relocation.
`-PminY=-64 -PmaxY=32752` tests the client's 32K world range; use the same options
for its `-Preload` run. Tests verify real JBT startup functions, a tool macro,
high/low blocks, and server/client light propagation.
`-PhighY=32720` additionally checks placement and lighting near the 32K ceiling.
`-PpackMods=/path/to/instance/mods` adds other mods to an isolated test run through
file links; it never modifies the instance or its saves.

Graphical test runs use an isolated Xvfb display and muted options. Test code and
upstream dependency jars are excluded from the published jar.

## Scope

The initial release addresses world startup, not complete compatibility with all
physics, extreme heights, or other mods. Dynamic Height's documented Lithium
incompatibility is outside this fix. See [the player-facing page](docs/MODRINTH.md).

Client tests cover -128..1024 and -64..32752. Vanilla GameTestServer randomly
places its arena as far as ±15M, beyond DH's horizontal packing range; a test-only
mixin places it at the origin. This explains the earlier intermittent test
lighting failures and does not alter production behavior. The complete Engineering
modpack and moving contraptions are not certified by these focused tests.
