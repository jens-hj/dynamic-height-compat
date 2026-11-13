# Dynamic Height Compat

Compatibility fixes for [Dynamic Height](https://modrinth.com/mod/dynamic-height), starting with world startup alongside [Sable](https://modrinth.com/mod/sable).

## Current fix

Prevents the `GenerationBounds.from` crash when Sable creates its empty chunk before the world's chunk cache is available. Uses the world's actual generator bounds during that construction window and leaves normal Dynamic Height behavior intact.

## Requirements

- Minecraft **1.21.1**, NeoForge **21.1.266** or newer in the 21.1 series.
- Dynamic Height **0.3.5** for NeoForge 1.21.1.
- Sable **2.0.6** is the targeted integration; it is optional for loading this mod.

Install alongside the original mods on the server and clients, or in your singleplayer instance. This addon does not replace either mod.

## Scope

Version 0.1.0 addresses the initial world-startup crash. It does not claim complete compatibility with all Sable physics, extreme heights, other mods, or future releases. Dynamic Height lists Lithium as incompatible; this addon does not address that separate issue.

The client regression uses a **−128 to 1024** world and checks blocks at **Y=−96** and **Y=512**, including a full save/restart. Dedicated GameTest runs also exposed an intermittent lighting-thread failure, including at this range and without Sable. This addon does not fix that issue. Use backed-up test worlds; the initial alpha only fixes the startup exception.

Future releases can add further focused compatibility fixes. Report reproducible issues with logs and exact mod versions on [GitHub](https://github.com/jens-hj/dynamic-height-compat/issues).
