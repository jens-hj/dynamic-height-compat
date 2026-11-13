#!/usr/bin/env python3
"""Original pixel art: extended world layers joined by a copper brace."""
from pathlib import Path
from PIL import Image, ImageDraw

root = Path(__file__).resolve().parents[1]
image = Image.new("RGB", (32, 32), "#102335")
draw = ImageDraw.Draw(image)
draw.rectangle((6, 7, 21, 23), fill="#70503d")
draw.rectangle((6, 7, 21, 9), fill="#78b66b")
draw.rectangle((6, 15, 21, 23), fill="#687b8d")
for x, y in ((8, 12), (17, 11), (12, 21), (19, 18), (8, 17)):
    draw.rectangle((x, y, x + 1, y + 1), fill="#b3bbba")
draw.rectangle((25, 5, 26, 25), fill="#e5ae6b")
draw.polygon(((22, 7), (25, 3), (29, 7)), fill="#e5ae6b")
draw.polygon(((22, 24), (25, 28), (29, 24)), fill="#e5ae6b")
draw.rectangle((3, 4, 10, 5), fill="#8be3cf")
draw.rectangle((3, 4, 4, 11), fill="#8be3cf")
draw.rectangle((3, 25, 10, 26), fill="#8be3cf")
draw.rectangle((3, 19, 4, 26), fill="#8be3cf")
for path, size in ((root / "src/main/resources/assets/dynamic_height_compat/icon.png", 128),
                   (root / "docs/icon.png", 512)):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.resize((size, size), Image.Resampling.NEAREST).save(path)
