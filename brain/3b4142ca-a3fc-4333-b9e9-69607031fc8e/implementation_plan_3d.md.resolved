# 3D Rotating Preview

Adding a live, 3D rotating preview to the Mod Maker to help users visualize their blocks and items before adding them to the queue.

## User Review Required

> [!IMPORTANT]
> The preview will use standard Java `Graphics2D` for 3D projection. While this won't be a full GL-based 3D engine, it will provide a high-quality "Minecraft-style" visualization of the block with its actual textures.

## Proposed Changes

### [Component Name] Preview Component

#### [NEW] [PreviewPanel.java](file:///home/vladimir/.gemini/antigravity/scratch/minecraft_mod_maker/src/main/java/com/modmaker/PreviewPanel.java)
- A custom `JPanel` that handles 3D rendering.
- **Block Rendering**: A rotating cube using `top`, `bottom`, and `side` textures.
- **Item Rendering**: A rotating 2D plane (or slightly extruded) for items.
- A `javax.swing.Timer` will update the rotation angle at ~60 FPS.
- Textures will be mapped to the 3D faces using triangle-based `AffineTransform` warping.

---

### [Component Name] Application Integration

#### [MODIFY] [ModMakerApp.java](file:///home/vladimir/.gemini/antigravity/scratch/minecraft_mod_maker/src/main/java/com/modmaker/ModMakerApp.java)
- Add the `PreviewPanel` to the UI, likely placed in the right-side panel above the element queue.
- Update the preview when:
    - Textures are selected or created.
    - User switches between "Add Block" and "Add Item" tabs.
    - Name field is updated (to show the name in the preview label).

## Verification Plan

### Automated Tests
- Non-visual: Verify that the `PreviewPanel` correctly loads texture files and handles nulls gracefully.

### Manual Verification
- Run the app and verify the preview spins smoothly.
- Select different textures for top/bottom/sides and check the visualization.
- Switch to the "Add Item" tab and verify the item preview works.
