# Minecraft Mod Maker

A desktop GUI tool for creating Minecraft 1.20.1 Forge mods without writing any code. Design your blocks, items, and entities visually, then hit Generate — it handles all the boilerplate, downloads the Forge MDK, and compiles a ready-to-use `.jar`.

![Platform](https://img.shields.io/badge/platform-Linux-blue) ![Java](https://img.shields.io/badge/Java-17-orange) ![Forge](https://img.shields.io/badge/Forge-1.20.1--47.3.0-green)

---

## Features

**Blocks**
- Custom top, bottom, and side textures (or one texture for all sides)
- Tool type, break time, blast resistance, light level, sound type
- Transparent/glass-like rendering
- Crafting recipe editor

**Items**
- Custom texture with built-in pixel art editor
- Food properties (hunger, saturation)
- Tool/weapon support (Sword, Pickaxe, Axe, Shovel, Hoe) with tier selection
- Stack size, rarity, enchantment glow
- Crafting recipe editor

**Entities**
- Custom texture and optional Blockbench-exported Java model
- Stats: health, movement speed, attack damage
- AI behaviours: melee attack, leap, follow player, avoid water, burn in sunlight, flee from player
- Spawn egg with custom primary/secondary colours

**Project management**
- Save and load projects (`.mmp` files) so you can pick up where you left off
- Edit or remove any element from the queue before generating
- Input validation with clear error messages

**Build**
- Downloads the Forge MDK automatically
- Runs a full Gradle build and outputs a compiled `.jar` ready to drop into your mods folder
- Live build log window

---

A pre-compiled AppImage is available in the [Releases](../../releases) tab.

---

## Requirements

- Java 17 (newer versions may not be compatible)
- Internet connection (to download the Forge MDK on first generate)
- Linux (AppImage) or run directly from source with Maven

---

## Running

**From the AppImage:**
```bash
chmod +x Minecraft_Mod_Maker-x86_64.AppImage
./Minecraft_Mod_Maker-x86_64.AppImage
```

**From source:**
```bash
mvn package
java -jar target/minecraft_mod_maker-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Or use the helper script:
```bash
./run.sh
```

---

## Building the AppImage

```bash
./package_appimage.sh
```

This will compile the project, create a custom JRE with `jlink`, and package everything into a self-contained AppImage. Requires `jlink` and `wget` to be available.

---

## How to use

1. Fill in **Mod ID** (lowercase, no spaces, e.g. `mymod`) and **Mod Name** in the top panel
2. Use the **Add Block / Add Item / Add Entity** tabs to configure each element and click the Add button to queue it
3. Optionally click **Edit Crafting Recipe...** before adding to set a shaped crafting recipe
4. Use **File > Save Project** to save your work at any time
5. Select an **Output Directory** where the compiled `.jar` will be placed
6. Click **Generate Mod** and watch the build log — when it finishes, your mod jar is in the output directory

To edit a queued element, select it in the list on the right and click **Edit Selected**. It will be loaded back into the form for changes.

---

## Project structure

```
src/main/java/com/modmaker/
├── ModMakerApp.java       # Main UI
├── ModGenerator.java      # Forge project generation + Gradle build
├── BlockData.java         # Block element model
├── ItemData.java          # Item element model
├── EntityData.java        # Entity element model
├── ModElement.java        # Common interface
├── Recipe.java            # Crafting recipe model
├── RecipeDialog.java      # Recipe editor dialog
├── RecipeGridPanel.java   # Recipe grid UI component
├── PixelArtDialog.java    # Built-in pixel art texture editor
├── PreviewPanel.java      # Live block/item preview
├── ProjectData.java       # Project save/load serialization
└── IconSystem.java        # Icon utilities
```

---

## Notes

- Entity names must start with a letter and contain only letters, numbers, spaces, or underscores
- Mod ID must be lowercase letters, numbers or underscores (e.g. `cool_mod`)
- Custom entity models should be exported from [Blockbench](https://www.blockbench.net/) as Modded Entity model `.java` files
- The first generate will take a while — Forge MDK download + Gradle dependency resolution can take several minutes
