package com.modmaker;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class RecipeDialog extends JDialog {

    private RecipeGridPanel gridPanel;
    private boolean saved = false;

    public RecipeDialog(Frame owner, String elementName, File textureFile, Recipe currentRecipe) {
        super(owner, "Crafting Recipe: " + elementName, true);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Result Preview
        JPanel previewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        previewPanel.setBorder(BorderFactory.createTitledBorder("Result: " + elementName));
        
        JLabel resultIcon = new JLabel();
        if (textureFile != null && textureFile.exists()) {
            resultIcon.setIcon(IconSystem.getScaledIcon(textureFile, 64));
        } else {
            resultIcon.setText("No Texture");
            resultIcon.setPreferredSize(new Dimension(64, 64));
            resultIcon.setOpaque(true);
            resultIcon.setBackground(Color.LIGHT_GRAY);
            resultIcon.setHorizontalAlignment(SwingConstants.CENTER);
        }
        previewPanel.add(resultIcon);
        
        content.add(previewPanel, BorderLayout.NORTH);

        // Grid Panel
        gridPanel = new RecipeGridPanel();
        if (currentRecipe != null) {
            // Populate grid from currentRecipe
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    gridPanel.getRecipe().setIngredient(r, c, currentRecipe.getIngredient(r, c));
                }
            }
            // Force buttons to update
            gridPanel.resetUI(); 
        }
        content.add(gridPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        
        JButton saveBtn = new JButton("Save Recipe");
        saveBtn.addActionListener(e -> {
            saved = true;
            dispose();
        });
        
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        content.add(btnPanel, BorderLayout.SOUTH);

        add(content);
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean showDialog() {
        setVisible(true);
        return saved;
    }

    public Recipe getRecipe() {
        return gridPanel.getRecipe();
    }
}
