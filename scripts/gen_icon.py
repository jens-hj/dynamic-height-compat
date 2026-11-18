"""Original compatibility icon, composed on one absolute pixel grid."""
from pathlib import Path
from PIL import Image, ImageDraw
from compat_symbol import BACKGROUND, draw_compat_symbol

root = Path(__file__).resolve().parents[1]
image = Image.new("RGB", (32,32), BACKGROUND)
d = ImageDraw.Draw(image)
# A tall terrain column and height arrow leave space for the links.
d.rectangle((4,6,17,25), fill="#687b8d")
d.rectangle((4,6,17,13), fill="#805b43")
d.rectangle((4,5,17,7), fill="#84c77a")
for x,y in [(6,10),(13,11),(7,17),(13,21)]:
    d.rectangle((x,y,x+1,y+1), fill="#b6c8c7")
d.rectangle((23,6,24,14), fill="#d9ad73")
for box in [(22,5,25,6),(21,6,26,7),(22,14,25,15),(21,13,26,14)]:
    d.rectangle(box, fill="#d9ad73")
d.rectangle((23,4,24,4), fill="#d9ad73")
d.rectangle((23,16,24,16), fill="#d9ad73")

draw_compat_symbol(image)
for path, size in ((root / "src/main/resources/assets/dynamic_height_compat/icon.png", 128), (root / "docs/icon.png", 512)):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.resize((size,size), Image.Resampling.NEAREST).save(path)
