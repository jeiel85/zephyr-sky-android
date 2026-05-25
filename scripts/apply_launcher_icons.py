from PIL import Image
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
icon_src = ROOT / "play_store" / "assets" / "icon_512.png"

# Target android launcher icon sizes based on density folders
sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192
}

def apply_icons():
    if not icon_src.exists():
        print(f"Error: Source icon not found at {icon_src}")
        return
        
    img = Image.open(icon_src)
    print(f"Loaded source icon: {icon_src} ({img.size})")
    
    for folder, size in sizes.items():
        dest_dir = ROOT / "app" / "src" / "main" / "res" / folder
        dest_dir.mkdir(parents=True, exist_ok=True)
        
        # Resize using high-quality LANCZOS resampler
        resized = img.resize((size, size), Image.Resampling.LANCZOS)
        
        # Save as webp format to replace existing launcher webp icons
        resized.save(dest_dir / "ic_launcher.webp", "WEBP", quality=100)
        resized.save(dest_dir / "ic_launcher_round.webp", "WEBP", quality=100)
        print(f"Successfully applied to {folder} at {size}x{size} px")

if __name__ == "__main__":
    apply_icons()
