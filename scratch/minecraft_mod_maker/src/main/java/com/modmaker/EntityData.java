package com.modmaker;

import java.io.File;

public class EntityData implements ModElement {
    private String name;
    private File texture;
    private File modelJavaFile; // Optional custom .java model
    private int maxHealth;
    private float movementSpeed;
    private float attackDamage;
    private String aiType; // Deprecated, but keeping for compatibility if needed
    
    // AI Flags
    private boolean canMelee;
    private boolean canLeap;
    private boolean isFollower;
    private boolean avoidsWater;
    private boolean burnsInSun;
    private boolean isTimid;
    
    private String primaryColor; // Hex string e.g. #FFFFFF
    private String secondaryColor;

    public EntityData(String name, File texture, File modelJavaFile, int maxHealth, float movementSpeed, float attackDamage, 
                      boolean canMelee, boolean canLeap, boolean isFollower, boolean avoidsWater, boolean burnsInSun, boolean isTimid,
                      String primaryColor, String secondaryColor) {
        this.name = name;
        this.texture = texture;
        this.modelJavaFile = modelJavaFile;
        this.maxHealth = maxHealth;
        this.movementSpeed = movementSpeed;
        this.attackDamage = attackDamage;
        this.canMelee = canMelee;
        this.canLeap = canLeap;
        this.isFollower = isFollower;
        this.avoidsWater = avoidsWater;
        this.burnsInSun = burnsInSun;
        this.isTimid = isTimid;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.aiType = canMelee ? "Hostile" : "Passive"; // Mapping for simplicity
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public File getTextureFile() {
        return texture;
    }

    public File getModelJavaFile() {
        return modelJavaFile;
    }

    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public boolean isItem() {
        return false;
    }

    @Override
    public boolean isEntity() {
        return true;
    }

    public int getMaxHealth() { return maxHealth; }
    public float getMovementSpeed() { return movementSpeed; }
    public float getAttackDamage() { return attackDamage; }
    public String getAiType() { return aiType; }
    
    public boolean canMelee() { return canMelee; }
    public boolean canLeap() { return canLeap; }
    public boolean isFollower() { return isFollower; }
    public boolean avoidsWater() { return avoidsWater; }
    public boolean burnsInSun() { return burnsInSun; }
    public boolean isTimid() { return isTimid; }

    public String getPrimaryColor() { return primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }

    @Override
    public String getDisplayString() {
        return String.format("[Entity] %s (HP:%d, Model:%s, AI:%s)", 
                name, maxHealth, (modelJavaFile != null ? "Custom" : "Cube"), 
                (canMelee ? "Hostile" : "Passive"));
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}

