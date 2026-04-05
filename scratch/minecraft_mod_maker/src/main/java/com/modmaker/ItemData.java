package com.modmaker;

import java.io.File;

public class ItemData implements ModElement {
    private String itemName;
    private File textureFile;
    
    private boolean isFood;
    private int nutrition;
    private float saturation;
    
    private boolean isTool;
    private String toolType;
    private String toolTier;
    private int maxStackSize;
    private String rarity;
    private boolean isGlowing;
    private Recipe recipe;

    public ItemData(String itemName, File textureFile, boolean isFood, int nutrition, float saturation, 
                    boolean isTool, String toolType, String toolTier, int maxStackSize, 
                    String rarity, boolean isGlowing, Recipe recipe) {
        this.itemName = itemName;
        this.textureFile = textureFile;
        this.isFood = isFood;
        this.nutrition = nutrition;
        this.saturation = saturation;
        this.isTool = isTool;
        this.toolType = toolType;
        this.toolTier = toolTier;
        this.maxStackSize = maxStackSize;
        this.rarity = rarity;
        this.isGlowing = isGlowing;
        this.recipe = recipe;
    }

    @Override
    public String getName() {
        return itemName;
    }

    @Override
    public File getTextureFile() {
        return textureFile;
    }

    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public boolean isItem() {
        return true;
    }

    @Override
    public boolean isEntity() {
        return false;
    }

    public boolean isFood() {
        return isFood;
    }

    public int getNutrition() {
        return nutrition;
    }

    public float getSaturation() {
        return saturation;
    }

    public boolean isTool() {
        return isTool;
    }

    public String getToolType() {
        return toolType;
    }

    public String getToolTier() {
        return toolTier;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public String getRarity() {
        return rarity;
    }

    public boolean isGlowing() {
        return isGlowing;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    public String getDisplayString() {
        StringBuilder info = new StringBuilder();
        if (isTool) info.append(" [").append(toolTier).append(" ").append(toolType).append("]");
        if (isFood) info.append(" [Food: ").append(nutrition).append("]");
        info.append(" [").append(rarity).append(", x").append(maxStackSize).append("]");
        if (isGlowing) info.append(" [Glow]");
        return "[Item] " + itemName + info.toString();
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}
