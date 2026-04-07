package com.modmaker;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

public class ModMakerApp extends JFrame {

    private JTextField modIdField;
    private JTextField modNameField;
    private JTextField outputDirField;
    private JButton generateButton;
    private DefaultListModel<ModElement> listModel;
    private JList<ModElement> elementList;
    private List<ModElement> queuedElements;
    private PreviewPanel previewPanel;
    
    private File selectedOutputDir;

    // Block Fields
    private JTextField blockNameField;
    private JTextField blockTopTexturePathField;
    private JTextField blockBottomTexturePathField;
    private JTextField blockSideTexturePathField;
    private JComboBox<String> blockToolDropdown;
    private JSpinner blockBreakTimeSpinner;
    private JSpinner blockResistanceSpinner;
    private JSpinner blockLightLevelSpinner;
    private JComboBox<String> blockSoundDropdown;
    private JCheckBox blockIsTransparentBox;
    private File blockTopTexture;
    private File blockBottomTexture;
    private File blockSideTexture;

    // Item Fields
    private JTextField itemNameField;
    private JTextField itemTexturePathField;
    private File itemSelectedTexture;
    
    private JCheckBox itemIsFoodBox;
    private JSpinner itemNutritionSpinner;
    private JSpinner itemSaturationSpinner;
    
    private JCheckBox itemIsToolBox;
    private JComboBox<String> itemToolTypeDropdown;
    private JComboBox<String> itemToolTierDropdown;
    
    private JSpinner itemMaxStackSpinner;
    private JComboBox<String> itemRarityDropdown;
    private JCheckBox itemIsGlowingBox;
    
    // Entity Tab Components
    private JTextField entityNameField;
    private JTextField entityTexturePathField;
    private JTextField entityModelPathField;
    private JSpinner entityHpSpinner;
    private JSpinner entitySpeedSpinner;
    private JSpinner entityDamageSpinner;
    
    private JCheckBox entityCanMeleeBox;
    private JCheckBox entityCanLeapBox;
    private JCheckBox entityIsFollowerBox;
    private JCheckBox entityAvoidsWaterBox;
    private JCheckBox entityBurnsInSunBox;
    private JCheckBox entityIsTimidBox;
    
    private JTextField entityPrimaryColorField;
    private JTextField entitySecondaryColorField;
    private File entityTexture;
    private File entityModelFile;
    
    private JCheckBox useSameTextureCheckBox;
    private JCheckBox useCreativeTabCheckBox;
    
    // New Texture Container Fields
    private JPanel textureContainer;
    private CardLayout textureCardLayout;
    private JTextField blockSingleTexturePathField;
    private File blockSingleTexture;
    
    private Recipe currentBlockRecipe = new Recipe();
    private Recipe currentItemRecipe = new Recipe();

    public ModMakerApp() {
        queuedElements = new ArrayList<>();
        listModel = new DefaultListModel<>();
        
        setTitle("Minecraft 1.20.1 Forge Mod Maker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // --- Left Panel (Config & Tabs) ---
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        
        // 1. Global Config (Top)
        JPanel globalPanel = new JPanel(new GridBagLayout());
        globalPanel.setBorder(BorderFactory.createTitledBorder("Global Mod Config"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        globalPanel.add(new JLabel("Mod ID:"), gbc);
        modIdField = new JTextField(15);
        gbc.gridx = 1;
        globalPanel.add(modIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        globalPanel.add(new JLabel("Mod Name:"), gbc);
        modNameField = new JTextField(15);
        gbc.gridx = 1;
        globalPanel.add(modNameField, gbc);

        useCreativeTabCheckBox = new JCheckBox("Use Dedicated Creative Tab");
        useCreativeTabCheckBox.setSelected(true);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        globalPanel.add(useCreativeTabCheckBox, gbc);

        leftPanel.add(globalPanel, BorderLayout.NORTH);
        
        // 2. Tabbed Pane (Center)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Add Block", createScrollableTab(createBlockTab()));
        tabbedPane.addTab("Add Item", createScrollableTab(createItemTab()));
        tabbedPane.addTab("Add Entity", createScrollableTab(createEntityTab()));
        tabbedPane.addChangeListener(e -> updatePreviewMode(tabbedPane.getSelectedIndex()));
        leftPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Initial preview
        updateBlockPreview();
        
        // 3. Generation (Bottom)
        JPanel genPanel = new JPanel(new GridBagLayout());
        genPanel.setBorder(BorderFactory.createTitledBorder("Build & Output"));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; // Reset gridwidth
        gbc.weightx = 0.0;
        genPanel.add(new JLabel("Output Dir:"), gbc);
        
        outputDirField = new JTextField(15);
        outputDirField.setEditable(false);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        genPanel.add(outputDirField, gbc);
        
        JButton outDirBtn = new JButton("Browse");
        outDirBtn.addActionListener(this::browseOutputDir);
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        genPanel.add(outDirBtn, gbc);
        
        generateButton = new JButton("Generate Mod");
        generateButton.addActionListener(this::generateMod);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 5, 5, 5);
        genPanel.add(generateButton, gbc);
        leftPanel.add(genPanel, BorderLayout.SOUTH);
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        
        // --- Right Queue Panel ---
        JPanel queuePanel = new JPanel(new BorderLayout(5, 5));
        queuePanel.setBorder(BorderFactory.createTitledBorder("Mod Elements Queue"));
        
        previewPanel = new PreviewPanel();
        queuePanel.add(previewPanel, BorderLayout.NORTH);
        
        elementList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(elementList);
        queuePanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel queueButtons = new JPanel(new GridLayout(1, 2, 5, 0));
        JButton editButton = new JButton("Edit Selected");
        editButton.addActionListener(e -> editSelected(tabbedPane));
        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> {
            int idx = elementList.getSelectedIndex();
            if (idx != -1) {
                listModel.remove(idx);
                queuedElements.remove(idx);
            }
        });
        queueButtons.add(editButton);
        queueButtons.add(removeButton);
        queuePanel.add(queueButtons, BorderLayout.SOUTH);
        
        mainPanel.add(queuePanel, BorderLayout.CENTER);
        
        add(mainPanel);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save Project...");
        saveItem.addActionListener(e -> saveProject());
        JMenuItem loadItem = new JMenuItem("Load Project...");
        loadItem.addActionListener(e -> loadProject());
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }

    private JScrollPane createScrollableTab(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }
    
    private JPanel createBlockTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 1. Block Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Block Name:"), gbc);
        blockNameField = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(blockNameField, gbc);
        
        // 2. Multi-Texture Toggle
        useSameTextureCheckBox = new JCheckBox("Use same texture for all sides");
        useSameTextureCheckBox.addActionListener(e -> {
            boolean selected = useSameTextureCheckBox.isSelected();
            textureCardLayout.show(textureContainer, selected ? "SINGLE" : "MULTI");
            if (selected) {
                // If switching to single, sync all sides to the last selected or current single
                File f = blockSingleTexture != null ? blockSingleTexture : (blockSideTexture != null ? blockSideTexture : blockTopTexture);
                if (f != null) syncTextures(f, f.getAbsolutePath());
            }
        });
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        panel.add(useSameTextureCheckBox, gbc);

        // 3. Texture Container (CardLayout)
        textureCardLayout = new CardLayout();
        textureContainer = new JPanel(textureCardLayout);

        // --- Multi-Texture Panel ---
        JPanel multiTexturePanel = new JPanel(new GridBagLayout());
        GridBagConstraints mgbc = new GridBagConstraints();
        mgbc.fill = GridBagConstraints.HORIZONTAL;
        mgbc.insets = new Insets(2, 0, 2, 0);

        // Top Texture
        mgbc.gridx = 0; mgbc.gridy = 0; mgbc.gridwidth = 1;
        multiTexturePanel.add(new JLabel("Top:"), mgbc);
        blockTopTexturePathField = new JTextField(10);
        blockTopTexturePathField.setEditable(false);
        mgbc.gridx = 1; mgbc.weightx = 1.0;
        multiTexturePanel.add(blockTopTexturePathField, mgbc);
        
        JPanel topBtnRow = new JPanel(new GridLayout(1, 2, 2, 0));
        JButton topBrowseBtn = new JButton("Browse");
        topBrowseBtn.addActionListener(e -> {
            File f = browseTextureFile();
            if (f != null) {
                blockTopTexture = f;
                blockTopTexturePathField.setText(f.getAbsolutePath());
                updateBlockPreview();
            }
        });
        JButton topCreateBtn = new JButton("Create");
        topCreateBtn.addActionListener(e -> {
            PixelArtDialog dialog = new PixelArtDialog(ModMakerApp.this);
            File f = dialog.showDialog();
            if (f != null) {
                blockTopTexture = f;
                blockTopTexturePathField.setText(f.getAbsolutePath());
                updateBlockPreview();
            }
        });
        topBtnRow.add(topBrowseBtn);
        topBtnRow.add(topCreateBtn);
        mgbc.gridx = 2; mgbc.weightx = 0.0;
        multiTexturePanel.add(topBtnRow, mgbc);

        // Bottom Texture
        mgbc.gridx = 0; mgbc.gridy = 1;
        multiTexturePanel.add(new JLabel("Bottom:"), mgbc);
        blockBottomTexturePathField = new JTextField(10);
        blockBottomTexturePathField.setEditable(false);
        mgbc.gridx = 1; mgbc.weightx = 1.0;
        multiTexturePanel.add(blockBottomTexturePathField, mgbc);
        
        JPanel bottomBtnRow = new JPanel(new GridLayout(1, 2, 2, 0));
        JButton bottomBrowseBtn = new JButton("Browse");
        bottomBrowseBtn.addActionListener(e -> {
            File f = browseTextureFile();
            if (f != null) {
                blockBottomTexture = f;
                blockBottomTexturePathField.setText(f.getAbsolutePath());
                updateBlockPreview();
            }
        });
        JButton bottomCreateBtn = new JButton("Create");
        bottomCreateBtn.addActionListener(e -> {
            PixelArtDialog dialog = new PixelArtDialog(ModMakerApp.this);
            File f = dialog.showDialog();
            if (f != null) {
                blockBottomTexture = f;
                blockBottomTexturePathField.setText(f.getAbsolutePath());
                updateBlockPreview();
            }
        });
        bottomBtnRow.add(bottomBrowseBtn);
        bottomBtnRow.add(bottomCreateBtn);
        mgbc.gridx = 2; mgbc.weightx = 0.0;
        multiTexturePanel.add(bottomBtnRow, mgbc);

        // Side Texture
        mgbc.gridx = 0; mgbc.gridy = 2;
        multiTexturePanel.add(new JLabel("Sides:"), mgbc);
        blockSideTexturePathField = new JTextField(10);
        blockSideTexturePathField.setEditable(false);
        mgbc.gridx = 1; mgbc.weightx = 1.0;
        multiTexturePanel.add(blockSideTexturePathField, mgbc);
        
        JPanel sideBtnRow = new JPanel(new GridLayout(1, 2, 2, 0));
        JButton sideBrowseBtn = new JButton("Browse");
        sideBrowseBtn.addActionListener(e -> {
            File f = browseTextureFile();
            if (f != null) {
                blockSideTexture = f;
                blockSideTexturePathField.setText(f.getAbsolutePath());
                updateBlockPreview();
            }
        });
        JButton sideCreateBtn = new JButton("Create");
        sideCreateBtn.addActionListener(e -> {
            PixelArtDialog dialog = new PixelArtDialog(ModMakerApp.this);
            File f = dialog.showDialog();
            if (f != null) {
                blockSideTexture = f;
                blockSideTexturePathField.setText(f.getAbsolutePath());
                updateBlockPreview();
            }
        });
        sideBtnRow.add(sideBrowseBtn);
        sideBtnRow.add(sideCreateBtn);
        mgbc.gridx = 2; mgbc.weightx = 0.0;
        multiTexturePanel.add(sideBtnRow, mgbc);

        // --- Single-Texture Panel ---
        JPanel singleTexturePanel = new JPanel(new GridBagLayout());
        mgbc.gridy = 0; // Reset for single panel
        
        mgbc.gridx = 0; mgbc.gridwidth = 1;
        singleTexturePanel.add(new JLabel("Texture:"), mgbc);
        blockSingleTexturePathField = new JTextField(10);
        blockSingleTexturePathField.setEditable(false);
        mgbc.gridx = 1; mgbc.weightx = 1.0;
        singleTexturePanel.add(blockSingleTexturePathField, mgbc);
        
        JPanel singleBtnRow = new JPanel(new GridLayout(1, 2, 2, 0));
        JButton singleBrowseBtn = new JButton("Browse");
        singleBrowseBtn.addActionListener(e -> {
            File f = browseTextureFile();
            if (f != null) {
                syncTextures(f, f.getAbsolutePath());
            }
        });
        JButton singleCreateBtn = new JButton("Create");
        singleCreateBtn.addActionListener(e -> {
            PixelArtDialog dialog = new PixelArtDialog(ModMakerApp.this);
            File f = dialog.showDialog();
            if (f != null) {
                syncTextures(f, f.getAbsolutePath());
            }
        });
        singleBtnRow.add(singleBrowseBtn);
        singleBtnRow.add(singleCreateBtn);
        mgbc.gridx = 2; mgbc.weightx = 0.0;
        singleTexturePanel.add(singleBtnRow, mgbc);

        textureContainer.add(multiTexturePanel, "MULTI");
        textureContainer.add(singleTexturePanel, "SINGLE");
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        panel.add(textureContainer, gbc);

        // 4. Tool Needed Section
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("Tool Needed:"), gbc);
        String[] tools = {"Hand", "Pickaxe", "Axe", "Shovel", "Hoe", "Sword"};
        blockToolDropdown = new JComboBox<>(tools);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(blockToolDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("Break Time (s):"), gbc);
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1.5, 0.0, 1000.0, 0.1);
        blockBreakTimeSpinner = new JSpinner(spinnerModel);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(blockBreakTimeSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        panel.add(new JLabel("Resistance:"), gbc);
        blockResistanceSpinner = new JSpinner(new SpinnerNumberModel(3.0, 0.0, 1000.0, 0.1));
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(blockResistanceSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        panel.add(new JLabel("Light Level (0-15):"), gbc);
        blockLightLevelSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 15, 1));
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(blockLightLevelSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        panel.add(new JLabel("Sound Type:"), gbc);
        String[] blockSounds = {"STONE", "WOOD", "GRAVEL", "METAL", "GRASS", "GLASS", "SLIME", "SAND"};
        blockSoundDropdown = new JComboBox<>(blockSounds);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(blockSoundDropdown, gbc);

        blockIsTransparentBox = new JCheckBox("Is Transparent (Glass-like)?");
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 3;
        panel.add(blockIsTransparentBox, gbc);
        
        JButton addBtn = new JButton("Add Block To Queue");
        addBtn.addActionListener(this::addBlock);
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(addBtn, gbc);
        
        // Recipe Button
        gbc.gridy = 10; gbc.weighty = 0.0;
        JButton recipeBtn = new JButton("Edit Crafting Recipe...");
        recipeBtn.addActionListener(e -> {
            RecipeDialog dialog = new RecipeDialog(ModMakerApp.this, blockNameField.getText(), blockSideTexture, currentBlockRecipe);
            if (dialog.showDialog()) {
                currentBlockRecipe = dialog.getRecipe();
            }
        });
        panel.add(recipeBtn, gbc);
        
        return panel;
    }
    
    private JPanel createItemTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Item Name:"), gbc);
        itemNameField = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(itemNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("Texture (PNG):"), gbc);
        itemTexturePathField = new JTextField(10);
        itemTexturePathField.setEditable(false);
        gbc.gridx = 1;
        panel.add(itemTexturePathField, gbc);
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        JButton textureBtn = new JButton("Browse");
        textureBtn.addActionListener(e -> {
            File f = browseTextureFile();
            if (f != null) {
                itemSelectedTexture = f;
                itemTexturePathField.setText(f.getAbsolutePath());
                updateItemPreview();
            }
        });
        
        JButton createBtn = new JButton("Create");
        createBtn.addActionListener(e -> {
            PixelArtDialog dialog = new PixelArtDialog(ModMakerApp.this);
            File f = dialog.showDialog();
            if (f != null) {
                itemSelectedTexture = f;
                itemTexturePathField.setText(f.getAbsolutePath());
                updateItemPreview();
            }
        });
        
        btnPanel.add(textureBtn);
        btnPanel.add(createBtn);
        
        gbc.gridx = 2;
        panel.add(btnPanel, gbc);
        
        // Food Section
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        panel.add(new JSeparator(), gbc);
        
        itemIsFoodBox = new JCheckBox("Is Edible Food?");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        panel.add(itemIsFoodBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("Hunger Points:"), gbc);
        itemNutritionSpinner = new JSpinner(new SpinnerNumberModel(4, 0, 20, 1)); // 4 = 2 meat haunches
        itemNutritionSpinner.setEnabled(false);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(itemNutritionSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        panel.add(new JLabel("Saturation:"), gbc);
        itemSaturationSpinner = new JSpinner(new SpinnerNumberModel(0.3, 0.0, 5.0, 0.1));
        itemSaturationSpinner.setEnabled(false);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(itemSaturationSpinner, gbc);
        
        itemIsFoodBox.addActionListener(e -> {
            boolean selected = itemIsFoodBox.isSelected();
            itemNutritionSpinner.setEnabled(selected);
            itemSaturationSpinner.setEnabled(selected);
        });
        
        // Tool Section
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
        panel.add(new JSeparator(), gbc);
        
        itemIsToolBox = new JCheckBox("Is Tool/Weapon?");
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        panel.add(itemIsToolBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1;
        panel.add(new JLabel("Tool Type:"), gbc);
        String[] types = {"Sword", "Pickaxe", "Axe", "Shovel", "Hoe"};
        itemToolTypeDropdown = new JComboBox<>(types);
        itemToolTypeDropdown.setEnabled(false);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(itemToolTypeDropdown, gbc);
        
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1;
        panel.add(new JLabel("Tool Tier:"), gbc);
        String[] tiers = {"Wood", "Stone", "Iron", "Gold", "Diamond", "Netherite"};
        itemToolTierDropdown = new JComboBox<>(tiers);
        itemToolTierDropdown.setEnabled(false);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(itemToolTierDropdown, gbc);
        
        itemIsToolBox.addActionListener(e -> {
            boolean selected = itemIsToolBox.isSelected();
            itemToolTypeDropdown.setEnabled(selected);
            itemToolTierDropdown.setEnabled(selected);
        });

        // Advanced Item Properties
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 3;
        panel.add(new JSeparator(), gbc);

        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 1;
        panel.add(new JLabel("Max Stack Size:"), gbc);
        itemMaxStackSpinner = new JSpinner(new SpinnerNumberModel(64, 1, 64, 1));
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(itemMaxStackSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 1;
        panel.add(new JLabel("Rarity:"), gbc);
        String[] rarities = {"COMMON", "UNCOMMON", "RARE", "EPIC"};
        itemRarityDropdown = new JComboBox<>(rarities);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(itemRarityDropdown, gbc);

        itemIsGlowingBox = new JCheckBox("Has Enchantment Glow?");
        gbc.gridx = 0; gbc.gridy = 13; gbc.gridwidth = 3;
        panel.add(itemIsGlowingBox, gbc);

        JButton addBtn = new JButton("Add Item To Queue");
        addBtn.addActionListener(this::addItem);
        gbc.gridx = 0; gbc.gridy = 14; gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(addBtn, gbc);
        
        gbc.gridy = 15; gbc.weighty = 0.0;
        JButton recipeBtn = new JButton("Edit Crafting Recipe...");
        recipeBtn.addActionListener(e -> {
            RecipeDialog dialog = new RecipeDialog(ModMakerApp.this, itemNameField.getText(), itemSelectedTexture, currentItemRecipe);
            if (dialog.showDialog()) {
                currentItemRecipe = dialog.getRecipe();
            }
        });
        panel.add(recipeBtn, gbc);
        
        return panel;
    }
    
    private File browseTextureFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Texture (PNG)");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private void applySideTexture(File f) {
        blockSideTexture = f;
        blockSideTexturePathField.setText(f.getAbsolutePath());
        // Auto-fill top/bottom if empty
        if (blockBottomTexture == null || (useSameTextureCheckBox != null && useSameTextureCheckBox.isSelected())) {
            blockBottomTexture = f;
            blockBottomTexturePathField.setText(f.getAbsolutePath());
        }
        updateBlockPreview();
    }

    private void syncTextures(File f, String path) {
        if (f == null) return;
        blockTopTexture = f;
        blockTopTexturePathField.setText(path);
        blockBottomTexture = f;
        blockBottomTexturePathField.setText(path);
        blockSideTexture = f;
        blockSideTexturePathField.setText(path);
        
        if (blockSingleTexturePathField != null) {
            blockSingleTexture = f;
            blockSingleTexturePathField.setText(path);
        }
        updateBlockPreview();
    }

    private void updatePreviewMode(int tabIndex) {
        if (tabIndex == 0) updateBlockPreview();       // Add Block Tab
        else if (tabIndex == 1) updateItemPreview();   // Add Item Tab
        else previewPanel.clearPreview();               // Add Entity Tab
    }

    private void updateBlockPreview() {
        if (previewPanel != null) {
            previewPanel.setBlockTextures(blockTopTexture, blockBottomTexture, blockSideTexture);
        }
    }

    private void updateItemPreview() {
        if (previewPanel != null) {
            previewPanel.setItemTexture(itemSelectedTexture);
        }
    }


    private JPanel createEntityTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Entity Name:"), gbc);
        entityNameField = new JTextField(15);
        gbc.gridx = 1; panel.add(entityNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Texture (PNG from Blockbench):"), gbc);
        entityTexturePathField = new JTextField(15);
        entityTexturePathField.setEditable(false);
        JButton texBrowseBtn = new JButton("Browse...");
        texBrowseBtn.addActionListener(e -> {
            File f = browseTextureFile();
            if (f != null) {
                entityTexture = f;
                entityTexturePathField.setText(f.getName());
            }
        });
        JPanel texPathPanel = new JPanel(new BorderLayout());
        texPathPanel.add(entityTexturePathField, BorderLayout.CENTER);
        texPathPanel.add(texBrowseBtn, BorderLayout.EAST);
        gbc.gridx = 1; panel.add(texPathPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Model (.java from Blockbench):"), gbc);
        entityModelPathField = new JTextField(15);
        entityModelPathField.setEditable(false);
        JButton modelBrowseBtn = new JButton("Browse...");
        modelBrowseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Java Files", "java"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                entityModelFile = chooser.getSelectedFile();
                entityModelPathField.setText(entityModelFile.getName());
            }
        });
        JPanel modelPathPanel = new JPanel(new BorderLayout());
        modelPathPanel.add(entityModelPathField, BorderLayout.CENTER);
        modelPathPanel.add(modelBrowseBtn, BorderLayout.EAST);
        gbc.gridx = 1; panel.add(modelPathPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Max Health:"), gbc);
        entityHpSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 1000, 1));
        gbc.gridx = 1; panel.add(entityHpSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Speed (0.1 - 2.0):"), gbc);
        entitySpeedSpinner = new JSpinner(new SpinnerNumberModel(0.25, 0.01, 2.0, 0.01));
        gbc.gridx = 1; panel.add(entitySpeedSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Attack Damage:"), gbc);
        entityDamageSpinner = new JSpinner(new SpinnerNumberModel(3.0, 0.0, 100.0, 0.5));
        gbc.gridx = 1; panel.add(entityDamageSpinner, gbc);

        // AI Behaviors
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JPanel aiPanel = new JPanel(new GridLayout(3, 2));
        aiPanel.setBorder(BorderFactory.createTitledBorder("AI Behaviors"));
        entityCanMeleeBox = new JCheckBox("Melee Attack");
        entityCanLeapBox = new JCheckBox("Leap at Target");
        entityIsFollowerBox = new JCheckBox("Follow Player (Tame)");
        entityAvoidsWaterBox = new JCheckBox("Avoids Water");
        entityBurnsInSunBox = new JCheckBox("Burns in Sunlight");
        entityIsTimidBox = new JCheckBox("Flee from Player");
        aiPanel.add(entityCanMeleeBox); aiPanel.add(entityCanLeapBox);
        aiPanel.add(entityIsFollowerBox); aiPanel.add(entityAvoidsWaterBox);
        aiPanel.add(entityBurnsInSunBox); aiPanel.add(entityIsTimidBox);
        panel.add(aiPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        panel.add(new JLabel("Egg Primary Color:"), gbc);
        entityPrimaryColorField = new JTextField("#FFFFFF");
        gbc.gridx = 1; panel.add(entityPrimaryColorField, gbc);

        gbc.gridx = 0; gbc.gridy = 8; panel.add(new JLabel("Egg Secondary Color:"), gbc);
        entitySecondaryColorField = new JTextField("#000000");
        gbc.gridx = 1; panel.add(entitySecondaryColorField, gbc);

        JButton addBtn = new JButton("Add Entity To Queue");
        addBtn.addActionListener(this::addEntity);
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(addBtn, gbc);

        return panel;
    }

    private void addEntity(ActionEvent e) {
        String name = entityNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide an entity name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!name.matches("[a-zA-Z][a-zA-Z0-9 _]*")) {
            JOptionPane.showMessageDialog(this, "Entity name must start with a letter and contain only letters, numbers, spaces, or underscores.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (entityTexture == null) {
            JOptionPane.showMessageDialog(this, "Please select a texture.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        EntityData data = new EntityData(
            name, entityTexture, entityModelFile,
            (Integer) entityHpSpinner.getValue(), 
            ((Double) entitySpeedSpinner.getValue()).floatValue(),
            ((Double) entityDamageSpinner.getValue()).floatValue(),
            entityCanMeleeBox.isSelected(),
            entityCanLeapBox.isSelected(),
            entityIsFollowerBox.isSelected(),
            entityAvoidsWaterBox.isSelected(),
            entityBurnsInSunBox.isSelected(),
            entityIsTimidBox.isSelected(),
            entityPrimaryColorField.getText(),
            entitySecondaryColorField.getText()
        );

        queuedElements.add(data);
        listModel.addElement(data);

        // Reset fields
        entityNameField.setText("");
        entityTexturePathField.setText("");
        entityModelPathField.setText("");
        entityTexture = null;
        entityModelFile = null;
        entityHpSpinner.setValue(20);
        entitySpeedSpinner.setValue(0.25);
        entityDamageSpinner.setValue(3.0);
        entityCanMeleeBox.setSelected(false);
        entityCanLeapBox.setSelected(false);
        entityIsFollowerBox.setSelected(false);
        entityAvoidsWaterBox.setSelected(false);
        entityBurnsInSunBox.setSelected(false);
        entityIsTimidBox.setSelected(false);
        entityPrimaryColorField.setText("#FFFFFF");
        entitySecondaryColorField.setText("#000000");
    }
    
    private void addBlock(ActionEvent e) {
        String name = blockNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a block name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!name.matches("[a-zA-Z][a-zA-Z0-9 _]*")) {
            JOptionPane.showMessageDialog(this, "Block name must start with a letter and contain only letters, numbers, spaces, or underscores.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (blockTopTexture == null || blockBottomTexture == null || blockSideTexture == null) {
            JOptionPane.showMessageDialog(this, "Please select all textures.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String toolType = (String) blockToolDropdown.getSelectedItem();
        float breakingTime = ((Double) blockBreakTimeSpinner.getValue()).floatValue();
        float resistance = ((Double) blockResistanceSpinner.getValue()).floatValue();
        int lightLevel = (Integer) blockLightLevelSpinner.getValue();
        String soundType = (String) blockSoundDropdown.getSelectedItem();
        boolean isTransparent = blockIsTransparentBox.isSelected();
        Recipe recipe = currentBlockRecipe;

        BlockData data = new BlockData(name, blockTopTexture, blockBottomTexture, blockSideTexture, 
                                      toolType, breakingTime, resistance, lightLevel, soundType, isTransparent, recipe);
        queuedElements.add(data);
        listModel.addElement(data);
        
        blockNameField.setText("");
        blockTopTexturePathField.setText("");
        blockBottomTexturePathField.setText("");
        blockSideTexturePathField.setText("");
        blockSingleTexturePathField.setText(""); // New
        
        blockResistanceSpinner.setValue(3.0);
        blockLightLevelSpinner.setValue(0);
        blockSoundDropdown.setSelectedIndex(0);
        blockIsTransparentBox.setSelected(false);
        
        blockTopTexture = null;
        blockBottomTexture = null;
        blockSideTexture = null;
        blockSingleTexture = null; // New
        
        currentBlockRecipe = new Recipe();
        updateBlockPreview();
    }
    
    private void addItem(ActionEvent e) {
        String name = itemNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an item name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!name.matches("[a-zA-Z][a-zA-Z0-9 _]*")) {
            JOptionPane.showMessageDialog(this, "Item name must start with a letter and contain only letters, numbers, spaces, or underscores.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (itemSelectedTexture == null) {
            JOptionPane.showMessageDialog(this, "Please select a texture.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean isFood = itemIsFoodBox.isSelected();
        int nutrition = (Integer) itemNutritionSpinner.getValue();
        float saturation = ((Double) itemSaturationSpinner.getValue()).floatValue();
        
        boolean isTool = itemIsToolBox.isSelected();
        String type = (String) itemToolTypeDropdown.getSelectedItem();
        String tier = (String) itemToolTierDropdown.getSelectedItem();
        
        int maxStack = (Integer) itemMaxStackSpinner.getValue();
        String rarity = (String) itemRarityDropdown.getSelectedItem();
        boolean isGlowing = itemIsGlowingBox.isSelected();
        
        Recipe recipe = currentItemRecipe;
        
        ItemData data = new ItemData(name, itemSelectedTexture, isFood, nutrition, saturation, 
                                    isTool, type, tier, maxStack, rarity, isGlowing, recipe);
        queuedElements.add(data);
        listModel.addElement(data);
        
        itemNameField.setText("");
        itemTexturePathField.setText("");
        itemMaxStackSpinner.setValue(64);
        itemRarityDropdown.setSelectedIndex(0);
        itemIsGlowingBox.setSelected(false);
        itemSelectedTexture = null;
        currentItemRecipe = new Recipe();
        updateItemPreview();
    }
    
    private void editSelected(JTabbedPane tabbedPane) {
        int idx = elementList.getSelectedIndex();
        if (idx == -1) {
            JOptionPane.showMessageDialog(this, "Select an element to edit.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ModElement el = queuedElements.get(idx);

        if (el instanceof BlockData) {
            BlockData b = (BlockData) el;
            blockNameField.setText(b.getName());
            blockTopTexture = b.getTopTexture();
            blockBottomTexture = b.getBottomTexture();
            blockSideTexture = b.getSideTexture();
            blockTopTexturePathField.setText(b.getTopTexture() != null ? b.getTopTexture().getAbsolutePath() : "");
            blockBottomTexturePathField.setText(b.getBottomTexture() != null ? b.getBottomTexture().getAbsolutePath() : "");
            blockSideTexturePathField.setText(b.getSideTexture() != null ? b.getSideTexture().getAbsolutePath() : "");
            blockToolDropdown.setSelectedItem(b.getToolType());
            blockBreakTimeSpinner.setValue((double) b.getBreakingTime());
            blockResistanceSpinner.setValue((double) b.getResistance());
            blockLightLevelSpinner.setValue(b.getLightLevel());
            blockSoundDropdown.setSelectedItem(b.getSoundType());
            blockIsTransparentBox.setSelected(b.isTransparent());
            currentBlockRecipe = b.getRecipe() != null ? b.getRecipe() : new Recipe();
            updateBlockPreview();
            tabbedPane.setSelectedIndex(0);
        } else if (el instanceof ItemData) {
            ItemData it = (ItemData) el;
            itemNameField.setText(it.getName());
            itemSelectedTexture = it.getTextureFile();
            itemTexturePathField.setText(it.getTextureFile() != null ? it.getTextureFile().getAbsolutePath() : "");
            itemIsFoodBox.setSelected(it.isFood());
            itemNutritionSpinner.setEnabled(it.isFood());
            itemNutritionSpinner.setValue(it.getNutrition());
            itemSaturationSpinner.setEnabled(it.isFood());
            itemSaturationSpinner.setValue((double) it.getSaturation());
            itemIsToolBox.setSelected(it.isTool());
            itemToolTypeDropdown.setEnabled(it.isTool());
            itemToolTypeDropdown.setSelectedItem(it.getToolType());
            itemToolTierDropdown.setEnabled(it.isTool());
            itemToolTierDropdown.setSelectedItem(it.getToolTier());
            itemMaxStackSpinner.setValue(it.getMaxStackSize());
            itemRarityDropdown.setSelectedItem(it.getRarity());
            itemIsGlowingBox.setSelected(it.isGlowing());
            currentItemRecipe = it.getRecipe() != null ? it.getRecipe() : new Recipe();
            updateItemPreview();
            tabbedPane.setSelectedIndex(1);
        } else if (el instanceof EntityData) {
            EntityData en = (EntityData) el;
            entityNameField.setText(en.getName());
            entityTexture = en.getTextureFile();
            entityTexturePathField.setText(en.getTextureFile() != null ? en.getTextureFile().getName() : "");
            entityModelFile = en.getModelJavaFile();
            entityModelPathField.setText(en.getModelJavaFile() != null ? en.getModelJavaFile().getName() : "");
            entityHpSpinner.setValue(en.getMaxHealth());
            entitySpeedSpinner.setValue((double) en.getMovementSpeed());
            entityDamageSpinner.setValue((double) en.getAttackDamage());
            entityCanMeleeBox.setSelected(en.canMelee());
            entityCanLeapBox.setSelected(en.canLeap());
            entityIsFollowerBox.setSelected(en.isFollower());
            entityAvoidsWaterBox.setSelected(en.avoidsWater());
            entityBurnsInSunBox.setSelected(en.burnsInSun());
            entityIsTimidBox.setSelected(en.isTimid());
            entityPrimaryColorField.setText(en.getPrimaryColor());
            entitySecondaryColorField.setText(en.getSecondaryColor());
            tabbedPane.setSelectedIndex(2);
        }

        // Remove from queue so re-adding replaces it
        listModel.remove(idx);
        queuedElements.remove(idx);
    }

    private void saveProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Project");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Mod Maker Project (*.mmp)", "mmp"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".mmp")) file = new File(file.getAbsolutePath() + ".mmp");

        ProjectData data = new ProjectData();
        data.modId = modIdField.getText().trim();
        data.modName = modNameField.getText().trim();
        data.useCreativeTab = useCreativeTabCheckBox.isSelected();
        data.outputDir = selectedOutputDir != null ? selectedOutputDir.getAbsolutePath() : null;

        for (ModElement el : queuedElements) {
            if (el instanceof BlockData)  data.elements.add(ProjectData.fromBlock((BlockData) el));
            else if (el instanceof ItemData)   data.elements.add(ProjectData.fromItem((ItemData) el));
            else if (el instanceof EntityData) data.elements.add(ProjectData.fromEntity((EntityData) el));
        }

        try {
            ProjectData.save(data, file);
            JOptionPane.showMessageDialog(this, "Project saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Project");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Mod Maker Project (*.mmp)", "mmp"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            ProjectData data = ProjectData.load(chooser.getSelectedFile());
            modIdField.setText(data.modId != null ? data.modId : "");
            modNameField.setText(data.modName != null ? data.modName : "");
            useCreativeTabCheckBox.setSelected(data.useCreativeTab);
            if (data.outputDir != null) {
                selectedOutputDir = new File(data.outputDir);
                outputDirField.setText(data.outputDir);
            }

            queuedElements.clear();
            listModel.clear();

            for (ProjectData.SerializedElement se : data.elements) {
                ModElement el = null;
                if ("block".equals(se.type)) {
                    el = new BlockData(se.name,
                        se.topTexturePath    != null ? new File(se.topTexturePath)    : null,
                        se.bottomTexturePath != null ? new File(se.bottomTexturePath) : null,
                        se.sideTexturePath   != null ? new File(se.sideTexturePath)   : null,
                        se.toolType, se.breakingTime, se.resistance, se.lightLevel,
                        se.soundType, se.isTransparent, se.recipe);
                } else if ("item".equals(se.type)) {
                    el = new ItemData(se.name,
                        se.texturePath != null ? new File(se.texturePath) : null,
                        se.isFood, se.nutrition, se.saturation,
                        se.isTool, se.itemToolType, se.toolTier,
                        se.maxStackSize, se.rarity, se.isGlowing, se.recipe);
                } else if ("entity".equals(se.type)) {
                    el = new EntityData(se.name,
                        se.texturePath != null ? new File(se.texturePath) : null,
                        se.modelPath   != null ? new File(se.modelPath)   : null,
                        se.maxHealth, se.movementSpeed, se.attackDamage,
                        se.canMelee, se.canLeap, se.isFollower,
                        se.avoidsWater, se.burnsInSun, se.isTimid,
                        se.primaryColor, se.secondaryColor);
                }
                if (el != null) {
                    queuedElements.add(el);
                    listModel.addElement(el);
                }
            }
            JOptionPane.showMessageDialog(this, "Project loaded.", "Loaded", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void browseOutputDir(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Output Directory");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedOutputDir = chooser.getSelectedFile();
            outputDirField.setText(selectedOutputDir.getAbsolutePath());
        }
    }
    
    private void generateMod(ActionEvent e) {
        String modId = modIdField.getText().trim();
        String modName = modNameField.getText().trim();
        
        if (modId.isEmpty() || modName.isEmpty() || selectedOutputDir == null) {
            JOptionPane.showMessageDialog(this, "Please fill in Global Mod Config and Output Directory.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (queuedElements.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one block or item to the queue.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!modId.matches("[a-z][a-z0-9_]{1,63}")) {
            JOptionPane.showMessageDialog(this, "Invalid Mod ID. Must be lowercase and simple.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        generateButton.setEnabled(false);
        generateButton.setText("Generating and Building...");
        
        final List<ModElement> elementsToGenerate = new ArrayList<>(queuedElements);
        final boolean useCreativeTab = useCreativeTabCheckBox.isSelected();
        final LogDialog logDialog = new LogDialog(this, modName);
        logDialog.setVisible(true);

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ModGenerator.generateMod(modId, modName, elementsToGenerate, selectedOutputDir, useCreativeTab, this::publish);
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String line : chunks) {
                    logDialog.appendLog(line);
                }
            }

            @Override
            protected void done() {
                generateButton.setEnabled(true);
                generateButton.setText("Generate Mod");
                logDialog.setFinished();
                try {
                    get(); 
                    JOptionPane.showMessageDialog(ModMakerApp.this, "Mod generated and built successfully!\nCheck the output directory for your compiled JAR.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ModMakerApp.this, "Error generating mod:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private static class LogDialog extends JDialog {
        private JTextArea textArea;
        
        public LogDialog(JFrame parent, String modName) {
            super(parent, "Building Mod: " + modName, false);
            setSize(600, 400);
            setLocationRelativeTo(parent);
            
            textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setBackground(Color.BLACK);
            textArea.setForeground(new Color(0, 255, 0)); // Green text on black
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            add(scrollPane);
            
            // Auto-scroll to bottom
            ((javax.swing.text.DefaultCaret)textArea.getCaret()).setUpdatePolicy(javax.swing.text.DefaultCaret.ALWAYS_UPDATE);
        }
        
        public void appendLog(String line) {
            textArea.append(line + "\n");
        }
        
        public void setFinished() {
            setTitle(getTitle() + " - FINISHED");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        
        SwingUtilities.invokeLater(() -> {
            new ModMakerApp().setVisible(true);
        });
    }
}
