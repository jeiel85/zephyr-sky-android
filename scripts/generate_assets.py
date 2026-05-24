"""Generate Play Store graphic assets for Zephyr Sky.

Outputs (relative to repo root):
  play_store/assets/icon_512.png             - 512x512 PNG, 32-bit
  play_store/assets/feature_1024x500.png     - 1024x500 PNG
  play_store/assets/screenshots/01-home.png  - 1080x1920 mock screenshot
  play_store/assets/screenshots/02-forecast.png
  play_store/assets/screenshots/03-detail.png

Design language:
  - Dark navy gradient (matches DarkWeatherThemes.ClearSkyGrad in code)
  - Soft moon + scattered stars
  - Crescent or sun-burst depending on asset
"""
from __future__ import annotations

import math
import os
import random
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "play_store" / "assets"
SCREENSHOTS = ASSETS / "screenshots"
ASSETS.mkdir(parents=True, exist_ok=True)
SCREENSHOTS.mkdir(parents=True, exist_ok=True)

# Brand palette pulled from WeatherScreen.kt
DARK_TOP = (15, 17, 30)        # 0xFF0F111E
DARK_BOTTOM = (30, 34, 53)     # 0xFF1E2235
LIGHT_TOP = (253, 251, 255)    # 0xFFFDFBFF
LIGHT_BOTTOM = (255, 242, 226) # 0xFFFFF2E2
ACCENT_GOLD = (255, 214, 138)
ACCENT_WHITE = (245, 247, 252)
ACCENT_NAVY = (75, 95, 145)


def make_vertical_gradient(size: tuple[int, int], top: tuple[int, int, int], bottom: tuple[int, int, int]) -> Image.Image:
    w, h = size
    img = Image.new("RGB", size, top)
    px = img.load()
    for y in range(h):
        t = y / max(h - 1, 1)
        r = int(top[0] + (bottom[0] - top[0]) * t)
        g = int(top[1] + (bottom[1] - top[1]) * t)
        b = int(top[2] + (bottom[2] - top[2]) * t)
        for x in range(w):
            px[x, y] = (r, g, b)
    return img


def make_diagonal_gradient(size: tuple[int, int], top: tuple[int, int, int], bottom: tuple[int, int, int]) -> Image.Image:
    w, h = size
    img = Image.new("RGB", size, top)
    px = img.load()
    diag = math.sqrt(w * w + h * h)
    for y in range(h):
        for x in range(w):
            t = (x + y) / max(diag, 1)
            t = max(0.0, min(1.0, t))
            r = int(top[0] + (bottom[0] - top[0]) * t)
            g = int(top[1] + (bottom[1] - top[1]) * t)
            b = int(top[2] + (bottom[2] - top[2]) * t)
            px[x, y] = (r, g, b)
    return img


def draw_soft_dot(img: Image.Image, center: tuple[int, int], radius: int, color: tuple[int, int, int, int]) -> None:
    cx, cy = center
    box = (cx - radius * 2, cy - radius * 2, cx + radius * 2, cy + radius * 2)
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    d.ellipse(box, fill=color)
    layer = layer.filter(ImageFilter.GaussianBlur(radius=radius * 0.6))
    img.alpha_composite(layer)


def draw_star(img: Image.Image, center: tuple[int, int], radius: int, color: tuple[int, int, int, int]) -> None:
    cx, cy = center
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    d.ellipse((cx - radius, cy - radius, cx + radius, cy + radius), fill=color)
    layer = layer.filter(ImageFilter.GaussianBlur(radius=max(1, radius * 0.4)))
    img.alpha_composite(layer)


def load_font(size: int, *, bold: bool = False) -> ImageFont.FreeTypeFont:
    candidates = [
        r"C:\Windows\Fonts\malgunbd.ttf" if bold else r"C:\Windows\Fonts\malgun.ttf",
        r"C:\Windows\Fonts\seguibl.ttf" if bold else r"C:\Windows\Fonts\segoeui.ttf",
        r"C:\Windows\Fonts\arial.ttf",
    ]
    for p in candidates:
        if os.path.exists(p):
            try:
                return ImageFont.truetype(p, size=size)
            except OSError:
                continue
    return ImageFont.load_default()


def make_icon_512() -> Image.Image:
    size = (512, 512)
    base = make_vertical_gradient(size, DARK_TOP, DARK_BOTTOM).convert("RGBA")

    # Crescent moon (subtle, off-center)
    moon_layer = Image.new("RGBA", size, (0, 0, 0, 0))
    md = ImageDraw.Draw(moon_layer)
    moon_center = (348, 168)
    md.ellipse(
        (moon_center[0] - 96, moon_center[1] - 96, moon_center[0] + 96, moon_center[1] + 96),
        fill=(255, 240, 210, 255),
    )
    md.ellipse(
        (moon_center[0] - 70, moon_center[1] - 110, moon_center[0] + 122, moon_center[1] + 82),
        fill=(15, 17, 30, 255),
    )
    moon_layer = moon_layer.filter(ImageFilter.GaussianBlur(radius=0.7))
    base.alpha_composite(moon_layer)
    draw_soft_dot(base, moon_center, 110, (255, 220, 170, 60))

    # Stars
    rng = random.Random(11)
    for _ in range(28):
        x = rng.randint(40, 472)
        y = rng.randint(40, 470)
        r = rng.choice([2, 2, 3, 3, 4])
        alpha = rng.randint(120, 230)
        draw_star(base, (x, y), r, (245, 247, 252, alpha))

    # Big "Z" wordmark
    draw = ImageDraw.Draw(base)
    font = load_font(290, bold=True)
    text = "Z"
    bbox = draw.textbbox((0, 0), text, font=font)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    tx = (size[0] - tw) // 2 - bbox[0]
    ty = (size[1] - th) // 2 - bbox[1] + 16

    # Soft glow behind Z
    glow = Image.new("RGBA", size, (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(glow)
    gdraw.text((tx, ty), text, font=font, fill=(255, 214, 138, 110))
    glow = glow.filter(ImageFilter.GaussianBlur(radius=18))
    base.alpha_composite(glow)

    draw.text((tx, ty), text, font=font, fill=(245, 247, 252, 255))

    # Subtle wordmark below
    sub_font = load_font(36, bold=True)
    sub_text = "Zephyr Sky"
    sb = draw.textbbox((0, 0), sub_text, font=sub_font)
    sw = sb[2] - sb[0]
    draw.text(
        ((size[0] - sw) // 2 - sb[0], 432),
        sub_text,
        font=sub_font,
        fill=(220, 226, 240, 220),
    )

    return base


def make_feature_1024x500() -> Image.Image:
    size = (1024, 500)
    base = make_diagonal_gradient(size, DARK_TOP, (40, 50, 90)).convert("RGBA")

    # Scattered stars
    rng = random.Random(7)
    for _ in range(80):
        x = rng.randint(0, size[0])
        y = rng.randint(0, size[1])
        r = rng.choice([1, 1, 2, 2, 3])
        alpha = rng.randint(80, 220)
        draw_star(base, (x, y), r, (245, 247, 252, alpha))

    # Soft sun-disc on right
    draw_soft_dot(base, (820, 250), 130, (255, 214, 138, 75))
    sun_layer = Image.new("RGBA", size, (0, 0, 0, 0))
    sd = ImageDraw.Draw(sun_layer)
    sd.ellipse((760, 190, 880, 310), fill=(255, 236, 198, 235))
    sun_layer = sun_layer.filter(ImageFilter.GaussianBlur(radius=0.6))
    base.alpha_composite(sun_layer)

    # Headline + tagline (Korean)
    draw = ImageDraw.Draw(base)
    title_font = load_font(82, bold=True)
    sub_font = load_font(34, bold=False)

    title = "Zephyr Sky"
    sub = "아름다운 그라데이션과 미니멀한 디자인의 날씨 앱"

    draw.text((72, 168), title, font=title_font, fill=(248, 250, 255, 255))
    draw.text((76, 274), sub, font=sub_font, fill=(210, 218, 238, 230))

    # Accent underline
    draw.rectangle((76, 252, 232, 258), fill=(255, 214, 138, 255))

    return base


def make_phone_screenshot(out_path: Path, *, title: str, accent: str, temp_label: str, condition: str, hours: list[tuple[str, int]]) -> None:
    """Render a 1080x1920 mock phone screenshot."""
    size = (1080, 1920)
    base = make_vertical_gradient(size, DARK_TOP, DARK_BOTTOM).convert("RGBA")

    # Stars
    rng = random.Random(hash(title) & 0xFFFF)
    for _ in range(60):
        x = rng.randint(0, size[0])
        y = rng.randint(0, size[1] // 2)
        r = rng.choice([1, 1, 2, 2, 3])
        alpha = rng.randint(80, 200)
        draw_star(base, (x, y), r, (245, 247, 252, alpha))

    draw = ImageDraw.Draw(base)

    # Status bar mock
    status_font = load_font(36, bold=True)
    draw.text((70, 60), "9:41", font=status_font, fill=(255, 255, 255, 240))
    draw.text((950, 60), "100%", font=status_font, fill=(255, 255, 255, 240))

    # Title bar
    title_font = load_font(56, bold=True)
    draw.text((70, 160), title, font=title_font, fill=(245, 247, 252, 255))

    # Accent chip
    chip_font = load_font(28, bold=True)
    chip_bbox = draw.textbbox((0, 0), accent, font=chip_font)
    chip_w = chip_bbox[2] - chip_bbox[0] + 56
    chip_h = chip_bbox[3] - chip_bbox[1] + 26
    draw.rounded_rectangle((70, 248, 70 + chip_w, 248 + chip_h), radius=24, fill=(255, 214, 138, 230))
    draw.text((98, 254), accent, font=chip_font, fill=(40, 32, 18, 255))

    # Big temperature
    big_font = load_font(360, bold=True)
    draw.text((70, 380), temp_label, font=big_font, fill=(255, 255, 255, 255))

    # Condition text
    cond_font = load_font(60, bold=False)
    draw.text((90, 820), condition, font=cond_font, fill=(220, 226, 240, 240))

    # Hourly forecast row
    hour_font = load_font(34, bold=True)
    temp_font = load_font(40, bold=True)
    base_y = 1050
    card_w = 138
    card_h = 240
    spacing = 18
    total_w = len(hours) * (card_w + spacing) - spacing
    start_x = (size[0] - total_w) // 2
    for idx, (h, t) in enumerate(hours):
        x = start_x + idx * (card_w + spacing)
        # Card background
        card_layer = Image.new("RGBA", size, (0, 0, 0, 0))
        cd = ImageDraw.Draw(card_layer)
        cd.rounded_rectangle((x, base_y, x + card_w, base_y + card_h), radius=28, fill=(255, 255, 255, 28))
        base.alpha_composite(card_layer)
        # Hour label
        hb = draw.textbbox((0, 0), h, font=hour_font)
        hw = hb[2] - hb[0]
        draw.text((x + (card_w - hw) // 2, base_y + 30), h, font=hour_font, fill=(220, 226, 240, 230))
        # Dot icon
        dx = x + card_w // 2
        dy = base_y + 120
        draw.ellipse((dx - 22, dy - 22, dx + 22, dy + 22), fill=(255, 214, 138, 235))
        # Temp
        temp_txt = f"{t}°"
        tb = draw.textbbox((0, 0), temp_txt, font=temp_font)
        tw = tb[2] - tb[0]
        draw.text((x + (card_w - tw) // 2, base_y + 170), temp_txt, font=temp_font, fill=(245, 247, 252, 255))

    # Details panel
    panel_y = 1370
    panel_h = 420
    panel_layer = Image.new("RGBA", size, (0, 0, 0, 0))
    pd = ImageDraw.Draw(panel_layer)
    pd.rounded_rectangle((70, panel_y, 1010, panel_y + panel_h), radius=36, fill=(255, 255, 255, 24))
    base.alpha_composite(panel_layer)

    detail_label_font = load_font(30, bold=True)
    detail_value_font = load_font(56, bold=True)
    items = [
        ("대기질", "보통"),
        ("UV", "낮음"),
        ("습도", "62%"),
        ("바람", "3.6m/s"),
    ]
    for i, (label, value) in enumerate(items):
        col = i % 2
        row = i // 2
        ix = 130 + col * 440
        iy = panel_y + 50 + row * 170
        draw.text((ix, iy), label, font=detail_label_font, fill=(180, 192, 220, 220))
        draw.text((ix, iy + 50), value, font=detail_value_font, fill=(245, 247, 252, 255))

    # Brand footer
    brand_font = load_font(36, bold=True)
    bb = draw.textbbox((0, 0), "Zephyr Sky", font=brand_font)
    bw = bb[2] - bb[0]
    draw.text(((size[0] - bw) // 2, 1830), "Zephyr Sky", font=brand_font, fill=(220, 226, 240, 220))

    base.convert("RGB").save(out_path, "PNG", optimize=True)


def main() -> None:
    icon = make_icon_512().convert("RGB")
    icon.save(ASSETS / "icon_512.png", "PNG", optimize=True)

    feature = make_feature_1024x500().convert("RGB")
    feature.save(ASSETS / "feature_1024x500.png", "PNG", optimize=True)

    make_phone_screenshot(
        SCREENSHOTS / "01-home.png",
        title="서울",
        accent="Clear",
        temp_label="22°",
        condition="맑음 · 체감 21°",
        hours=[("지금", 22), ("13시", 23), ("14시", 24), ("15시", 24), ("16시", 23), ("17시", 22)],
    )
    make_phone_screenshot(
        SCREENSHOTS / "02-forecast.png",
        title="제주",
        accent="Sunny",
        temp_label="26°",
        condition="청정 제주 한라산 무공해 공기",
        hours=[("지금", 26), ("13시", 27), ("14시", 28), ("15시", 28), ("16시", 27), ("17시", 26)],
    )
    make_phone_screenshot(
        SCREENSHOTS / "03-detail.png",
        title="부산",
        accent="Clouds",
        temp_label="24°",
        condition="흐리고 서늘한 바람",
        hours=[("지금", 24), ("13시", 25), ("14시", 25), ("15시", 24), ("16시", 23), ("17시", 22)],
    )

    print("OK")
    for p in [ASSETS / "icon_512.png", ASSETS / "feature_1024x500.png", SCREENSHOTS / "01-home.png", SCREENSHOTS / "02-forecast.png", SCREENSHOTS / "03-detail.png"]:
        print(f"- {p} ({p.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
