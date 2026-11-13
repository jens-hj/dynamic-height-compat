# Changelog

## 0.1.3

- Keep Voxy rendering enabled in 32K worlds by extending its vertical coordinates and matching shader decoding.
- Fix the `Position not valid at all levels` crash during terrain loading with Voxy 0.2.16-beta and Roxy 0.4.1.
- Rebuild extended-height LODs in a separate cache directory while preserving existing caches and Minecraft world data.

## 0.1.2

- Fix loading hangs with Jake's Build Tools by relocating its helper area into Dynamic Height's supported horizontal range.
- Keep tool functions, macros, startup, and uninstall commands on the same relocated coordinates.
- Straighten and center the icon's height arrow.

## 0.1.1

- Fix the startup crash when Supplementaries creates Moonlight fake worlds alongside Dynamic Height and Sable.
- Keep generator bounds available even when another mod replaces the chunk-cache constructor.

## 0.1.0

- Initial alpha: prevent Dynamic Height 0.3.5 from crashing during world startup with Sable 2.0.6.
- Preserve the world's actual generator bounds while Sable initializes its empty chunk.
- Scope: startup exception only; separate intermittent lighting failures remain unresolved.
