# Implementation Plan: Custom Items, Food, and Tools

This plan describes how we will introduce the creation of custom 2D items, completely decoupled from blocks, which can be configured as edible food or functional tools.

## Proposed Changes

### [NEW] com.modmaker.ItemData
A new data class to store item-specific attributes.
- `String itemName` and `File textureFile`
- `boolean isFood`, `int nutrition` (hunger points), `float saturation`
- `boolean isTool`, `String toolType` (Sword, Pickaxe, Axe, Shovel, Hoe), `String toolTier` (Wood, Stone, Iron, Gold, Diamond, Netherite)

### [MODIFY] com.modmaker.ModMakerApp.java (GUI)
Because the UI is getting crowded, I will revamp the left side using a **Tabbed Layout**:
- **Tab 1: Mod Config** (Mod ID, Mod Name, Output Dir, Generate Button)
- **Tab 2: Add Block** (The existing block name, texture, tool needed, and break time options)
- **Tab 3: Add Item** (New inputs for Item Name, Texture, checkboxes for "Is Food" and "Is Tool", with their respective sliders/spinners)

Both the "Add Block" and "Add Item" buttons will push to the same universal Queue on the right side of the screen.

### [MODIFY] com.modmaker.ModGenerator.java
- **Java Registration Update**: 
  - Standard items will use `() -> new Item(new Item.Properties())`.
  - Edible items will inject `.food(new FoodProperties.Builder().nutrition(x).saturationMod(y).build())` into the properties.
  - Tool items will be cast as returning `new PickaxeItem(Tiers.IRON, 1, -2.8f, new Item.Properties())` or equivalent mappings.
- **JSON Assets Update**: The generator will create standard `item/generated` models for items (giving them the classic 2D flat appearance rather than the blocky 3D cube). Item textures will be deposited into the `textures/item/` folder correctly.

## User Review Required

> [!IMPORTANT]
> To keep the tool creation simple, standard vanilla attack damage and attack speed modifiers will be baked in based on the tier and type of tool you choose (e.g., an Iron Axe will naturally hit harder but swing slower than an Iron Sword, exactly matching Minecraft's vanilla balancing). Is this acceptable, or do you want raw inputs to customize the exactly numeric attack damage?
