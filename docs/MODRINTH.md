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

The regression suite uses a **−128 to 1024** world and checks blocks at **Y=−96** and **Y=512**. Separate dedicated GameTest runs at 32K and without Sable hit a lighting-thread failure; this addon does not fix that issue. Start with a backed-up test world at the tested range.

Future releases can add further focused compatibility fixes. Report reproducible issues with logs and exact mod versions on [GitHub](https://github.com/jens-hj/dynamic-height-compat/issues).
