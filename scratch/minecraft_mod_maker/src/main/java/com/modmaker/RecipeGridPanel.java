package com.modmaker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RecipeGridPanel extends JPanel {

    private Recipe recipe;
    private JButton[][] slotButtons = new JButton[3][3];
    private String selectedIngredient = "minecraft:air";
    
    private DefaultListModel<String> itemModel;
    private JList<String> itemList;
    private JTextField searchField;
    
    private static final List<String> ALL_MC_ITEMS = loadAllItems();

    private static List<String> loadAllItems() {
        List<String> items = new ArrayList<>();
        try (InputStream is = RecipeGridPanel.class.getResourceAsStream("/items.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains("\"minecraft:")) {
                    // Extract "minecraft:..." from the JSON line
                    int start = line.indexOf("\"") + 1;
                    int end = line.lastIndexOf("\"");
                    if (start > 0 && end > start) {
                        items.add(line.substring(start, end));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load items.json: " + e.getMessage());
            // Fallback for safety
            items.add("minecraft:air");
            items.add("minecraft:stone");
            items.add("minecraft:dirt");
            items.add("minecraft:iron_ingot");
            items.add("minecraft:gold_ingot");
            items.add("minecraft:diamond");
        }
        return items;
    }

    public RecipeGridPanel() {
        this.recipe = new Recipe();
        setLayout(new BorderLayout(10, 10));
        
        // Left: 3x3 Grid
        JPanel gridContainer = new JPanel(new GridBagLayout());
        gridContainer.setBorder(BorderFactory.createTitledBorder("Crafting Pattern"));
        
        JPanel grid = new JPanel(new GridLayout(3, 3, 2, 2));
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                final int row = r;
                final int col = c;
                JButton btn = new JButton("");
                btn.setPreferredSize(new Dimension(64, 64));
                btn.setBackground(new Color(60, 60, 60)); // Dark slot
                btn.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40), 1));
                btn.setFocusPainted(false);
                btn.addActionListener(e -> setSlot(row, col));
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            clearSlot(row, col);
                        }
                    }
                });
                slotButtons[r][c] = btn;
                grid.add(btn);
            }
        }
        gridContainer.add(grid);
        add(gridContainer, BorderLayout.WEST);
        
        // Center: Item Selector
        JPanel selectorPanel = new JPanel(new BorderLayout(5, 5));
        selectorPanel.setBorder(BorderFactory.createTitledBorder("Item Browser (Select Ingredient)"));
        
        searchField = new JTextField();
        searchField.setToolTipText("Filter items...");
        searchField.addCaretListener(e -> filterItems());
        selectorPanel.add(searchField, BorderLayout.NORTH);
        
        itemModel = new DefaultListModel<>();
        for (String item : ALL_MC_ITEMS) itemModel.addElement(item);
        
        itemList = new JList<>(itemModel);
        itemList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String id = (String) value;
                // Repaint list when icon loads
                l.setIcon(IconSystem.getIcon(id, 16, list::repaint));
                l.setText(id.replace("minecraft:", ""));
                return l;
            }
        });
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String val = itemList.getSelectedValue();
                if (val != null) selectedIngredient = val;
            }
        });
        
        JScrollPane scroll = new JScrollPane(itemList);
        selectorPanel.add(scroll, BorderLayout.CENTER);
        
        JPanel customInputPanel = new JPanel(new BorderLayout(5, 0));
        JTextField customField = new JTextField();
        JButton addCustomBtn = new JButton("Use Custom ID");
        addCustomBtn.addActionListener(e -> {
            String id = customField.getText().trim();
            if (!id.isEmpty()) {
                selectedIngredient = id;
                if (!id.contains(":")) selectedIngredient = "minecraft:" + id;
            }
        });
        customInputPanel.add(customField, BorderLayout.CENTER);
        customInputPanel.add(addCustomBtn, BorderLayout.EAST);
        selectorPanel.add(customInputPanel, BorderLayout.SOUTH);
        
        add(selectorPanel, BorderLayout.CENTER);
    }
    
    private void filterItems() {
        String filter = searchField.getText().toLowerCase();
        itemModel.clear();
        for (String item : ALL_MC_ITEMS) {
            if (item.toLowerCase().contains(filter)) {
                itemModel.addElement(item);
            }
        }
    }
    
    private void setSlot(int row, int col) {
        if ("minecraft:air".equals(selectedIngredient)) {
            clearSlot(row, col);
            return;
        }
        recipe.setIngredient(row, col, selectedIngredient);
        updateButtonText(row, col);
    }
    
    private void clearSlot(int row, int col) {
        recipe.setIngredient(row, col, null);
        slotButtons[row][col].setText("");
        slotButtons[row][col].setToolTipText(null);
    }
    
    private void updateButtonText(int r, int c) {
        String id = recipe.getIngredient(r, c);
        if (id == null) {
            slotButtons[r][c].setIcon(null);
            slotButtons[r][c].setToolTipText(null);
        } else {
            // Repaint button when icon loads
            slotButtons[r][c].setIcon(IconSystem.getIcon(id, 32, () -> slotButtons[r][c].repaint()));
            slotButtons[r][c].setToolTipText(id);
        }
    }
    
    public Recipe getRecipe() {
        // Return a clone/copy to avoid mutation issues if needed, 
        // but for this app a simple getter is fine.
        return recipe;
    }
    
    public void reset() {
        this.recipe = new Recipe();
        resetUI();
    }
    
    public void resetUI() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                updateButtonText(r, c);
            }
        }
    }
}
