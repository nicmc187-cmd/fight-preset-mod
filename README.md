# Fight Preset — Fabric 1.21.11

Client-side starter mod for saved PvP inventory layouts.

## Features
- Save the current inventory as a named preset.
- Keep multiple presets in `.minecraft/config/fight-preset.json`.
- Clickable icon picker with common PvP items, plus a custom item-ID field.
- Select a saved preset, activate it, update its name/icon, or delete it.
- Translucent green/red slot backgrounds and dimmed ghost icons for missing/wrong items.
- Checks player inventory, armor and offhand slots.
- Press **H** to toggle the overlay; press **J** to open the preset manager.
- Does not move items or modify server state.

## Build
Requires JDK 21 and internet access for Gradle dependencies.

Windows:
```bat
gradlew.bat build
```
macOS/Linux:
```bash
./gradlew build
```

The JAR should be created under `build/libs/`.

## Install
1. Install Fabric Loader for Minecraft Java 1.21.11.
2. Install Fabric API for 1.21.11.
3. Build the project and copy the resulting JAR to `.minecraft/mods/`.
4. Start the Fabric 1.21.11 profile.

## Important
This is source code and has not been compiled or tested against a live Minecraft 1.21.11 client in this environment. The ghost item effect is approximated by dimming the rendered item with a translucent overlay; a true alpha-controlled item renderer would require a more involved rendering implementation. Inventory comparison checks item/components, not stack counts. Preset icons can be selected from the built-in grid or entered as an item ID.
