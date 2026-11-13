# Changelog

## 0.1.1

- Fix the startup crash when Supplementaries creates Moonlight fake worlds alongside Dynamic Height and Sable.
- Keep generator bounds available even when another mod replaces the chunk-cache constructor.

## 0.1.0

- Initial alpha: prevent Dynamic Height 0.3.5 from crashing during world startup with Sable 2.0.6.
- Preserve the world's actual generator bounds while Sable initializes its empty chunk.
- Scope: startup exception only; separate intermittent lighting failures remain unresolved.
