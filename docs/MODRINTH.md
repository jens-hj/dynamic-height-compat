# Dynamic Height Compat

Compatibility fixes for [Dynamic Height](https://modrinth.com/mod/dynamic-height) alongside [Sable](https://modrinth.com/mod/sable), [Moonlight](https://modrinth.com/mod/moonlight) fake worlds used by [Supplementaries](https://modrinth.com/mod/supplementaries), and [Jake's Build Tools](https://modrinth.com/datapack/jakes-build-tools).
Also supports [Voxy](https://modrinth.com/mod/voxy) through [Roxy](https://modrinth.com/mod/roxy).

## Fixes

Prevents the `GenerationBounds.from` crash when Sable creates its empty chunk before the world's chunk cache is available. Version **0.1.1** also handles Moonlight's replacement cache constructors, which could bypass the original fix when Supplementaries created a fake world. Uses the world's actual generator bounds during construction and leaves normal Dynamic Height behavior intact.

Version **0.1.2** fixes loading hangs caused by Jake's Build Tools' helper chunks at X=−20 million. Dynamic Height's packed positions only support horizontal coordinates within roughly ±8.4 million blocks. The addon moves JBT's scratch area to X=−2 million and updates its functions and macros together as they load. The original mod jars stay untouched.

Version **0.1.3** keeps Voxy rendering enabled in tall worlds. It fixes the `Position not valid at all levels` crash by extending Voxy's vertical coordinates and updating the shader decoder to match. High-altitude LOD data is checked near **Y=32720**, including a full restart.

Voxy rebuilds its LOD cache in a separate `dynamic-height-compat-y12-v1` directory. Existing caches are preserved, and Minecraft world data is unchanged. This separation prevents wrapped data from older attempts from being reused.

## Requirements

- Minecraft **1.21.1**, NeoForge **21.1.266** or newer in the 21.1 series.
- Dynamic Height **0.3.5** for NeoForge 1.21.1.
- Sable **2.0.6** is the targeted integration; it is optional for loading this mod.
- Moonlight **3.8.3** and Supplementaries **4.0.2** are additional tested integrations; both are optional.
- Jake's Build Tools **2.0.1.5** is an optional integration. Its relocated startup and tool functions are also tested with C2ME **0.4.0-alpha.0.124**.
- Voxy **0.2.16-beta** through Roxy **0.4.1**, with Sodium **0.8.13**, is an optional client integration. Use the upstream Voxy jar required by Roxy, even though that jar is labelled Minecraft 1.21.11; Roxy provides the 1.21.1 NeoForge bridge.

Install alongside the original mods on the server and clients, or in your singleplayer instance. This addon does not replace either mod.

## Scope

The addon addresses world startup crashes, JBT's unsafe helper coordinates, and Voxy's vertical coordinate limit. It does not expand Dynamic Height's horizontal coordinate range, migrate old helper data, or claim complete compatibility with all Sable physics, other mods, or future releases. Dynamic Height lists Lithium as incompatible; this addon does not address that separate issue.

Client regressions cover **−128 to 1024** and **−64 to 32752**, including high/low blocks, block lighting, and a full save/restart. Earlier intermittent GameTest lighting failures were traced to Minecraft randomly placing its test arena outside Dynamic Height's horizontal range. Tests now use a valid origin; production lighting is unchanged. Use backed-up test worlds while testing this alpha.

Future releases can add further focused compatibility fixes. Report reproducible issues with logs and exact mod versions on [GitHub](https://github.com/jens-hj/dynamic-height-compat/issues).
