# Dynamic Height Compat

Focused compatibility patches for Dynamic Height on Minecraft 1.21.1 / NeoForge.
The initial alpha fixes the Dynamic Height 0.3.5 / Sable 2.0.6 world-startup crash.
Further independently tested compatibility patches can be added as separate mixins.

## Implementation

Sable constructs an `EmptyLevelChunk` inside `ServerChunkCache` construction, before
the cache is assigned to `ServerLevel`. Dynamic Height's `GenerationBounds.from`
dereferences that unassigned cache.

The addon remembers the exact constructor-supplied `ChunkGenerator` on the level
while the cache is being created. During that window only, Dynamic Height receives
the generator's original minimum Y and generation depth. The reference is cleared
in `finally`, and initialized worlds and client height accessors use Dynamic
Height's original method. This also supports custom generators without registry
lookups, guessed vanilla limits, or allocating the full extended height.

Upstream jars are unmodified and are not included in this addon's jar.

## Development

Use the Nix flake (Gradle runs on Java 25; compilation and Minecraft use Java 21):

```sh
nix develop -c ./gradlew build compileTestmodJava
nix develop -c ./gradlew runGameTestServer
nix develop -c python scripts/headless-test.py ./gradlew runClientTest
nix develop -c ./gradlew modrinth -PmodrinthDryRun
```

`-Pbaseline` disables the fix for negative-control runs, which should reproduce the
original startup failure. Do not use it for ordinary gameplay.
`-PpackMods=/path/to/instance/mods` adds other mods to an isolated test run through
file links; it never modifies the instance or its saves.

Graphical test runs use an isolated Xvfb display and muted options. Test code and
upstream dependency jars are excluded from the published jar.

## Scope

The initial release addresses world startup, not complete compatibility with all
physics, extreme heights, or other mods. Dynamic Height's documented Lithium
incompatibility is outside this fix. See [the player-facing page](docs/MODRINTH.md).
