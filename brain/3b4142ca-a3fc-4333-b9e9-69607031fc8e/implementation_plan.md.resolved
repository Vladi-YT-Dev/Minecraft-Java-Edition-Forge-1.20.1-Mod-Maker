# Add Separate Textures for Blocks

This plan outlines the changes needed to allow users to specify different textures for the top, bottom, and lateral sides of a block in the Minecraft Mod Maker.

## User Review Required

> [!IMPORTANT]
> The block model will be changed from `cube_all` to `cube_bottom_top`. This means every block will now require (or at least support) three textures.

## Proposed Changes

### [Component Name] Block Data Model

#### [MODIFY] [BlockData.java](file:///home/vladimir/.gemini/antigravity/scratch/minecraft_mod_maker/src/main/java/com/modmaker/BlockData.java)
- Replace `textureFile` with `topTexture`, `bottomTexture`, and `sideTexture`.
- Update the constructor to accept all three.
- Update `getTextureFile()` to return `sideTexture` (for UI previews).
- Add specific getters for each texture.

---

### [Component Name] Mod Generation Logic

#### [MODIFY] [ModGenerator.java](file:///home/vladimir/.gemini/antigravity/scratch/minecraft_mod_maker/src/main/java/com/modmaker/ModGenerator.java)
- In `createAssets`, change the block model JSON:
    - Use `minecraft:block/cube_bottom_top` as parent.
    - Define `top`, `bottom`, and `side` textures.
- Update texture copying logic to copy three files: `{id}_top.png`, `{id}_bottom.png`, and `{id}_side.png`.

---

### [Component Name] User Interface

#### [MODIFY] [ModMakerApp.java](file:///home/vladimir/.gemini/antigravity/scratch/minecraft_mod_maker/src/main/java/com/modmaker/ModMakerApp.java)
- Update `createBlockTab()` to include three texture selection fields: **Top**, **Bottom**, and **Sides**.
- Update `addBlock()` to pass all three textures to the `BlockData` constructor.

## Open Questions

- Should I automatically default the top and bottom textures to the side texture if they are not selected? (I recommend this for better UX).

## Verification Plan

### Automated Tests
- I will check the generated JSON files in a temporary run to ensure the texture mapping is correct.

### Manual Verification
- Run the application and verify that the "Add Block" tab shows three texture options.
- Add a block with three different textures and check the "Mod Elements Queue".
- Verify the generated asset structure (textures/block/...) contains all three textures.
