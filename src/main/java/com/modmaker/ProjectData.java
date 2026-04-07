package com.modmaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple project save/load using Java serialization.
 * Wraps global config + element list into one serializable object.
 */
public class ProjectData implements Serializable {
    private static final long serialVersionUID = 1L;

    public String modId;
    public String modName;
    public boolean useCreativeTab;
    public String outputDir;
    public List<SerializedElement> elements = new ArrayList<>();

    // Flat serializable representation of any ModElement
    public static class SerializedElement implements Serializable {
        private static final long serialVersionUID = 1L;

        public String type; // "block", "item", "entity"

        // Common
        public String name;
        public String texturePath;

        // Block
        public String topTexturePath;
        public String bottomTexturePath;
        public String sideTexturePath;
        public String toolType;
        public float breakingTime;
        public float resistance;
        public int lightLevel;
        public String soundType;
        public boolean isTransparent;
        public Recipe recipe;

        // Item
        public boolean isFood;
        public int nutrition;
        public float saturation;
        public boolean isTool;
        public String itemToolType;
        public String toolTier;
        public int maxStackSize;
        public String rarity;
        public boolean isGlowing;

        // Entity
        public String modelPath;
        public int maxHealth;
        public float movementSpeed;
        public float attackDamage;
        public boolean canMelee;
        public boolean canLeap;
        public boolean isFollower;
        public boolean avoidsWater;
        public boolean burnsInSun;
        public boolean isTimid;
        public String primaryColor;
        public String secondaryColor;
    }

    public static SerializedElement fromBlock(BlockData b) {
        SerializedElement e = new SerializedElement();
        e.type = "block";
        e.name = b.getName();
        e.topTexturePath    = b.getTopTexture()    != null ? b.getTopTexture().getAbsolutePath()    : null;
        e.bottomTexturePath = b.getBottomTexture() != null ? b.getBottomTexture().getAbsolutePath() : null;
        e.sideTexturePath   = b.getSideTexture()   != null ? b.getSideTexture().getAbsolutePath()   : null;
        e.toolType = b.getToolType();
        e.breakingTime = b.getBreakingTime();
        e.resistance = b.getResistance();
        e.lightLevel = b.getLightLevel();
        e.soundType = b.getSoundType();
        e.isTransparent = b.isTransparent();
        e.recipe = b.getRecipe();
        return e;
    }

    public static SerializedElement fromItem(ItemData i) {
        SerializedElement e = new SerializedElement();
        e.type = "item";
        e.name = i.getName();
        e.texturePath = i.getTextureFile() != null ? i.getTextureFile().getAbsolutePath() : null;
        e.isFood = i.isFood();
        e.nutrition = i.getNutrition();
        e.saturation = i.getSaturation();
        e.isTool = i.isTool();
        e.itemToolType = i.getToolType();
        e.toolTier = i.getToolTier();
        e.maxStackSize = i.getMaxStackSize();
        e.rarity = i.getRarity();
        e.isGlowing = i.isGlowing();
        e.recipe = i.getRecipe();
        return e;
    }

    public static SerializedElement fromEntity(EntityData en) {
        SerializedElement e = new SerializedElement();
        e.type = "entity";
        e.name = en.getName();
        e.texturePath  = en.getTextureFile()    != null ? en.getTextureFile().getAbsolutePath()    : null;
        e.modelPath    = en.getModelJavaFile()  != null ? en.getModelJavaFile().getAbsolutePath()  : null;
        e.maxHealth = en.getMaxHealth();
        e.movementSpeed = en.getMovementSpeed();
        e.attackDamage = en.getAttackDamage();
        e.canMelee = en.canMelee();
        e.canLeap = en.canLeap();
        e.isFollower = en.isFollower();
        e.avoidsWater = en.avoidsWater();
        e.burnsInSun = en.burnsInSun();
        e.isTimid = en.isTimid();
        e.primaryColor = en.getPrimaryColor();
        e.secondaryColor = en.getSecondaryColor();
        return e;
    }

    public static void save(ProjectData data, File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
        }
    }

    public static ProjectData load(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ProjectData) ois.readObject();
        }
    }
}
