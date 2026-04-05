package com.modmaker;

import java.io.File;

public class BlockData implements ModElement {
    private String blockName;
    private File topTexture;
    private File bottomTexture;
    private File sideTexture;
    private String toolType;
    private float breakingTime;
    private float resistance;
    private int lightLevel;
    private String soundType;
    private boolean isTransparent;
    private Recipe recipe;

    public BlockData(String blockName, File topTexture, File bottomTexture, File sideTexture, 
                     String toolType, float breakingTime, float resistance, int lightLevel, 
                     String soundType, boolean isTransparent, Recipe recipe) {
        this.blockName = blockName;
        this.topTexture = topTexture;
        this.bottomTexture = bottomTexture;
        this.sideTexture = sideTexture;
        this.toolType = toolType;
        this.breakingTime = breakingTime;
        this.resistance = resistance;
        this.lightLevel = lightLevel;
        this.soundType = soundType;
        this.isTransparent = isTransparent;
        this.recipe = recipe;
    }

    @Override
    public String getName() {
        return blockName;
    }

    @Override
    public File getTextureFile() {
        return sideTexture;
    }

    public File getTopTexture() {
        return topTexture;
    }

    public File getBottomTexture() {
        return bottomTexture;
    }

    public File getSideTexture() {
        return sideTexture;
    }

    public String getToolType() {
        return toolType;
    }

    public float getBreakingTime() {
        return breakingTime;
    }

    public float getResistance() {
        return resistance;
    }

    public int getLightLevel() {
        return lightLevel;
    }

    public String getSoundType() {
        return soundType;
    }

    public boolean isTransparent() {
        return isTransparent;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    public boolean isBlock() {
        return true;
    }

    @Override
    public boolean isItem() {
        return false;
    }

    @Override
    public boolean isEntity() {
        return false;
    }

    @Override
    public String getDisplayString() {
        return String.format("[Block] %s (%s, %.1fs, R:%.1f, L:%d, %s)", 
                blockName, toolType, breakingTime, resistance, lightLevel, soundType);
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}
