# Minecraft 1.20.1 Mod Maker Completed

I have finished upgrading the Mod Maker to officially support powerful, distinct 2D Items. You can now craft foods and highly customized mining/combat tools from scratch.

## What was built:

1. **`ModElement.java` Architecture**: The system internally migrated from a `BlockData`-only setup to a polymorphic list. This means our tool queue can seamlessly hold a dozen custom blocks interleaved with twenty custom swords, handling them identically during processing.
2. **Major GUI Revamp (`ModMakerApp.java`)**:
    - **Tabbed Interface**: Notice the clean new layout. The left pane is now driven by a "Add Block" tab and an "Add Item" tab, reducing vertical clutter.
    - **Food Physics**: A dedicated checkbox for "Is Edible Food?". If toggled, it unlocks variables for standard Minecraft Food Properties like integer `Hunger` restoration bars and `Saturation` modifiers.
    - **Tool Mechanics**: A dedicated checkbox for "Is Tool/Weapon?". If toggled, it unlocks two powerful dropdowns to pick your exact weapon behavior (e.g. `Axe`, `Hoe`, `Sword`) and map it automatically to a Vanilla Tier profile (`Iron`, `Netherite`, `Wood`).
3. **`ModGenerator.java` Item Injector**: The underlying code generation engine underwent massive functional expansion:
    - **Java Registration Engine**: Capable of detecting items on the fly, applying native `import net.minecraft.world.food.FoodProperties`, and spawning explicit subclass registrations exactly identical to native vanilla formats (`new PickaxeItem(...)`, `new SwordItem(...)`, `new Item(...)`).
    - **Item JSON Formats**: Uses identical JSON rendering parameters but hooks directly into `parent: "minecraft:item/generated"` formats so that your items lay flat in 2D unlike Block representations.

## How to use it:

Restart the application by running:

```bash
cd /home/vladimir/.gemini/antigravity/scratch/minecraft_mod_maker
./run.sh
```

**Workflow Sandbox Overview:**
1. Execute the app and provide a Global Mod ID/Name.
2. **To Make a Block**: Use the "Add Block" tab just like before!
3. **To Make an Item**: Click the "Add Item" Tab. Give it a proper name and an image file. 
    - Want an ultimate weapon? Click **Is Tool/Weapon?**, pick "Sword" under Type, and pick "Netherite" under tier. 
    - Want a snack? Check **Is Edible Food** and crank up the `Saturation` to 5.0 for a golden-apple-like regeneration boost!
    - Want just plain items (like crafting materials)? Don't check either box.
4. Push everything to the Queue layout, set the target folder, and hit **Generate**. The custom JAR will drop perfectly into your folder, brimming with your custom items!

> [!NOTE]
> Tool damage values and speed stats strictly obey Vanilla constants based on your chosen Tier. (An Iron Sword you create will do exactly as much damage over time as a vanilla Iron Sword).
