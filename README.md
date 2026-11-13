# Dynamic Height Compat

Focused compatibility patches for Dynamic Height on Minecraft 1.21.1 / NeoForge.
The addon fixes Dynamic Height 0.3.5 / Sable 2.0.6 startup crashes, including
Moonlight 3.8.3 fake worlds used by Supplementaries 4.0.2.
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
`-PpackMods=/path/to/instance/mods` adds other mods to an isolated test run through
file links; it never modifies the instance or its saves.

Graphical test runs use an isolated Xvfb display and muted options. Test code and
upstream dependency jars are excluded from the published jar.

## Scope

The initial release addresses world startup, not complete compatibility with all
physics, extreme heights, or other mods. Dynamic Height's documented Lithium
incompatibility is outside this fix. See [the player-facing page](docs/MODRINTH.md).

Client tests use a supported -128..1024 range. Dedicated GameTest runs exposed
an intermittent lighting-thread null-section failure at both this range and 32K,
which this startup patch does not fix. The complete Engineering modpack and moving contraptions are not certified
by these focused tests.
