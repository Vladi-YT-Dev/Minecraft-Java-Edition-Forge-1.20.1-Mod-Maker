# Blockbench Model Integration & Advanced AI

To support "real" 3D entities, we will allow importing Java models designed in Blockbench. This is the official way to add complex shapes to Minecraft Java mods.

## How it works:
1. **Design**: You create your mob in [Blockbench](https://www.blockbench.net/).
2. **Export**: In Blockbench, go to **File > Export > Export Java Entity Model**.
3. **Import**: Upload that `.java` file into this Mod Maker.
4. **Result**: Your mod will use your professional 3D model instead of a simple cube.

## Proposed Changes

### Data Layer
#### [MODIFY] [EntityData.java](file:///home/vladimir/.gemini/antigravity/scratch/minecraft_mod_maker/src/main/java/com/modmaker/EntityData.java)
- Add `File modelJavaFile` to store the user-uploaded model.
- Add flags for AI behaviors: `canMelee`, `canLeap`, `isFollower`, `avoidsWater`, `burnsInSun`.

### UI Layer
#### [MODIFY] [ModMakerApp.java](file:///home/vladimir/.gemini/antigravity/scratch/minecraft_mod_maker/src/main/java/com/modmaker/ModMakerApp.java)
- **Model Upload**: Add "Custom Model (.java from Blockbench):" file picker.
- **AI Checklist**: Replace the AI dropdown with a multi-select list or checklist:
    - [ ] Melee Attack (Hostile/Neutral)
    - [ ] Leap at Target (Spider behavior)
    - [ ] Follow Player (Tameable/Pet)
    - [ ] Avoid Water
    - [ ] Burn in Sunlight
    - [ ] Flee from Player (Timid)

### Generation Layer
#### [MODIFY] [ModGenerator.java](file:///home/vladimir/.gemini/antigravity/scratch/minecraft_mod_maker/src/main/java/com/modmaker/ModGenerator.java)
- **Model Processing**: If a `.java` file is uploaded, the generator will:
    - Parse the file and rename the class to match the mod's naming convention.
    - Correct the `LAYER_LOCATION` and `ResourceLocation` paths.
- **AI Goals**: Generate `registerGoals()` logic using Forge's `MeleeAttackGoal`, `LeapAtTargetGoal`, `FollowOwnerGoal`, etc., based on UI selections.

## Verification Plan
1. Create a "Blockbench Mob" in the app.
2. Upload a sample Biped/Creeper `.java` model file.
3. Check `Melee Attack` and `Leap at Target`.
4. Generate the mod and verify that the `{Name}Model.java` contains the complex geometry from the uploaded file and the AI code reflects the checklist.
