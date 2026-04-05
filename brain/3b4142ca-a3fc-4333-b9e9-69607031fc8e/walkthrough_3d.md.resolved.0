# 3D Rotating Preview Implementation

I have added a live, 3D rotating preview to the Minecraft Mod Maker. This feature allows you to see exactly how your custom blocks and items will look in-game as you design them.

## Key Features

### 1. Real-Time 3D Block Preview
- When you are in the **Add Block** tab, the preview shows a 3D cube.
- It uses your selected **Top**, **Bottom**, and **Side** textures.
- The cube is rendered using a custom 3D projection engine built with standard `Graphics2D`.
- Textures are realistically warped onto the cube's faces using affine transformations.

### 2. Item Preview
- When you switch to the **Add Item** tab, the preview automatically switches to a 2D plane mode.
- It shows your selected item texture spinning around its center, giving it a dynamic feel.

### 3. Integrated UI
- The preview is located at the top of the **Mod Elements Queue** panel on the right.
- It updates instantly as you:
    - Browse for a texture.
    - Create a new texture with the Pixel Art Maker.
    - Switch between Block and Item tabs.
    - Clear the fields (it resets to a default gray state).

## Technical Implementation

- **`PreviewPanel.java`**: A custom high-performance rendering component. It uses a 3D-to-2D projection matrix and triangle-based texture mapping to achieve the 3D effect without needing external libraries like OpenGL.
- **`ModMakerApp.java`**: Orchestrates the updates between the input fields and the preview component.

> [!TIP]
> Try designing a block with different top and side textures (like a grass block) and watch it spin in the preview!
