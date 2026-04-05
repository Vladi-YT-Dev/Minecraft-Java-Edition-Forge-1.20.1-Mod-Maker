# Separate Block Textures Implementation

I have successfully updated the Minecraft Mod Maker to support three separate textures for custom blocks: **Top**, **Bottom**, and **Lateral Sides**.

## Changes

### 1. Block Data Model (`BlockData.java`)
- Updated `BlockData` to store three `File` objects for textures.
- The `getTextureFile()` method now returns the **Side** texture, which is used for the UI queue preview.

### 2. Mod Generation Logic (`ModGenerator.java`)
- Changed the generated block model JSON to use `minecraft:block/cube_bottom_top` as parent.
- Mapped textures as follows:
    - `"top"` -> `{block_id}_top`
    - `"bottom"` -> `{block_id}_bottom`
    - `"side"` -> `{block_id}_side`
- All three texture files are now copied to the mod's `assets` directory.

### 3. User Interface (`ModMakerApp.java`)
- Updated the "Add Block" tab with three texture selection rows.
- **Create Button**: Re-added the "Create" button for each texture field, allowing you to use the built-in Pixel Art Maker to design your textures on the fly.
- **Improved UX**: If you select or create the **Sides** texture first, it will automatically fill the **Top** and **Bottom** fields if they are empty. This speeds up the process for blocks that use the same texture on all sides.

## Verification

- [x] Verified `BlockData` constructor and getters.
- [x] Verified `ModGenerator` correctly outputs `cube_bottom_top` JSON.
- [x] Verified `ModMakerApp` UI layout and auto-fill logic.

> [!TIP]
> You can now create blocks like grass (green top, dirt bottom, grass-over-dirt sides) or logs (circular top/bottom, bark sides)!
