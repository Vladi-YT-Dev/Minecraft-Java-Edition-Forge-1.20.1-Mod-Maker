package com.modmaker;

import java.io.Serializable;

public class Recipe implements Serializable {
    private String[][] grid = new String[3][3];

    public Recipe() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                grid[i][j] = null; // null means empty
            }
        }
    }

    public void setIngredient(int row, int col, String itemId) {
        grid[row][col] = itemId;
    }

    public String getIngredient(int row, int col) {
        return grid[row][col];
    }

    public boolean isEmpty() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[i][j] != null) return false;
            }
        }
        return true;
    }

    public String[][] getGrid() {
        return grid;
    }
}
