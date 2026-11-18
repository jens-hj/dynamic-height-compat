"""Original pixel art. Each character is exactly one cell of a 16x16 grid."""
from pathlib import Path
from PIL import Image
from compat_symbol import compose

root = Path(__file__).resolve().parents[1]
ART = (
    '................',
    '................',
    '...........A....',
    '..GGGGGG..AAA...',
    '..DDDDDD...A....',
    '..DLDDLD...A....',
    '..DDDDDD...A....',
    '..SSSSSS..AAA...',
    '..SLSSSS...A....',
    '..SSSSLS........',
    '..SSSSSS........',
    '..SLSSSS........',
    '..SSSSSS........',
    '................',
    '................',
    '................',
)
COLORS = {'G': '#84c77a', 'D': '#997251', 'L': '#c6d1c8', 'S': '#687f8c', 'A': '#e6b978'}
image = compose(ART, COLORS)
for path, size in ((root / "src/main/resources/assets/dynamic_height_compat/icon.png", 128), (root / "docs/icon.png", 512)):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.resize((size,size), Image.Resampling.NEAREST).save(path)
