# Add UI Options and Creative Tab Support

The user wants two new features:
1.  A checkbox to use the same texture for all sides of a block.
2.  A checkbox to include all mod elements in a dedicated creative inventory tab.

## User Review Required

> [!IMPORTANT]
> - The **"Same Texture"** option will automatically sync the Top, Bottom, and Side texture selections in the UI when enabled.
> - The **"Creative Tab"** option will modify the generated Forge code to register a `CreativeModeTab` and automatically populate it with all items and blocks from the mod.

## Proposed Changes

### [Component Name]

#### [MODIFY] [ModMakerApp.java](file:///home/vladimir/.gemini/antigravity/scratch/minecraft_mod_maker/src/main/java/com/modmaker/ModMakerApp.java)
- Add `useSameTextureCheckBox` to the Block tab.
- Implement logic to sync textures when `useSameTextureCheckBox` is selected.
- Add `useCreativeTabCheckBox` to the Global Mod Config panel.
- Update `generateMod` call to pass the new boolean.

#### [MODIFY] [ModGenerator.java](file:///home/vladimir/.gemini/antigravity/scratch/minecraft_mod_maker/src/main/java/com/modmaker/ModGenerator.java)
- Update `generateMod` signature to accept `boolean useCustomCreativeTab`.
- Update `writeJavaClasses` to generate `CreativeModeTab` registration and population code if enabled.
- If enabled, add a translation entry for the creative tab name.

## Open Questions

- What should the name of the creative tab be? (Defaulting to "Mod Name" tab).
- Which item/block should be the icon for the creative tab? (Defaulting to the first element in the queue).

## Verification Plan

### Automated Tests
- None.

### Manual Verification
- Run the app.
- Check "Use same texture for all sides" and verify that selecting one texture updates all three.
- Check "Include in dedicated creative inventory tab".
- Generate the mod and inspect the generated `MainMod.java` to ensure the `CreativeModeTab` code is present and correct for Forge 1.20.1.
