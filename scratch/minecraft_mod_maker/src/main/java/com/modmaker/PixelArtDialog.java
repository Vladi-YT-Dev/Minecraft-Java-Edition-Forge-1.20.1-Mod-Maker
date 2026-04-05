package com.modmaker;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

public class PixelArtDialog extends JDialog {

    private static final int GRID_SIZE = 16;
    private static final int CELL_SIZE = 24;

    private Color[][] gridColors = new Color[GRID_SIZE][GRID_SIZE];
    private File savedFile = null;

    private Stack<Color[][]> undoStack = new Stack<>();
    private Stack<Color[][]> redoStack = new Stack<>();

    private SimpleCanvas canvas;
    private JColorChooser colorChooser;
    private JPanel recentColorsPanel;
    private List<Color> recentColors = new ArrayList<>();

    private enum Tool { BRUSH, FILL }
    private Tool currentTool = Tool.BRUSH;
    private int brushSize = 1;

    public PixelArtDialog(Frame owner) {
        super(owner, "Pixel Art Editor", true);

        // Initialize grid with transparent
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                gridColors[i][j] = new Color(0, 0, 0, 0);
            }
        }

        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Center: Canvas
        canvas = new SimpleCanvas();
        mainPanel.add(canvas, BorderLayout.CENTER);

        // Right: Color Management
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        
        colorChooser = new JColorChooser(Color.BLACK);
        colorChooser.setPreviewPanel(new JPanel());
        
        // Try to select HSV tab
        SwingUtilities.invokeLater(() -> selectHSVTab(colorChooser));
        
        rightPanel.add(colorChooser, BorderLayout.CENTER);

        // Recent Colors
        recentColorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recentColorsPanel.setBorder(BorderFactory.createTitledBorder("Recent Colors"));
        recentColorsPanel.setPreferredSize(new Dimension(200, 70));
        updateRecentColorsUI();
        rightPanel.add(recentColorsPanel, BorderLayout.SOUTH);
        
        mainPanel.add(rightPanel, BorderLayout.EAST);

        // North: Tool settings
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolBar.setBorder(BorderFactory.createTitledBorder("Tools"));
        
        JToggleButton brushBtn = new JToggleButton("Brush", true);
        JToggleButton fillBtn = new JToggleButton("Fill");
        ButtonGroup toolGroup = new ButtonGroup();
        toolGroup.add(brushBtn);
        toolGroup.add(fillBtn);
        
        brushBtn.addActionListener(e -> currentTool = Tool.BRUSH);
        fillBtn.addActionListener(e -> currentTool = Tool.FILL);
        
        toolBar.add(brushBtn);
        toolBar.add(fillBtn);
        
        toolBar.add(new JLabel("  Size:"));
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        sizeSpinner.addChangeListener(e -> brushSize = (int) sizeSpinner.getValue());
        toolBar.add(sizeSpinner);
        
        mainPanel.add(toolBar, BorderLayout.NORTH);

        // Bottom: Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton undoBtn = new JButton("Undo");
        undoBtn.addActionListener(e -> undo());

        JButton redoBtn = new JButton("Redo");
        redoBtn.addActionListener(e -> redo());

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            pushState();
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    gridColors[i][j] = new Color(0, 0, 0, 0);
                }
            }
            canvas.repaint();
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> {
            savedFile = null;
            dispose();
        });

        JButton saveBtn = new JButton("Save As Temp Texture");
        saveBtn.addActionListener(e -> {
            try {
                savedFile = saveToTempFile();
                dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to save image:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        controlPanel.add(undoBtn);
        controlPanel.add(redoBtn);
        controlPanel.add(clearBtn);
        controlPanel.add(cancelBtn);
        controlPanel.add(saveBtn);
        
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setupKeyBindings();
        pack();
        setLocationRelativeTo(owner);
    }

    private void setupKeyBindings() {
        JPanel contentPane = (JPanel) getContentPane();
        InputMap im = contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = contentPane.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "Redo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "Redo");

        am.put("Undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });

        am.put("Redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });
    }

    private Color[][] cloneGrid(Color[][] source) {
        Color[][] copy = new Color[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(source[i], 0, copy[i], 0, GRID_SIZE);
        }
        return copy;
    }

    private void pushState() {
        undoStack.push(cloneGrid(gridColors));
        redoStack.clear();
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(cloneGrid(gridColors));
            gridColors = undoStack.pop();
            canvas.repaint();
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(cloneGrid(gridColors));
            gridColors = redoStack.pop();
            canvas.repaint();
        }
    }

    private void selectHSVTab(JColorChooser chooser) {
        for (Component c : chooser.getComponents()) {
            if (c instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) c;
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    if (tabbedPane.getTitleAt(i).endsWith("HSV")) {
                        tabbedPane.setSelectedIndex(i);
                        return;
                    }
                }
            }
        }
    }

    private void updateRecentColorsUI() {
        recentColorsPanel.removeAll();
        for (Color c : recentColors) {
            JButton swatch = new JButton();
            swatch.setPreferredSize(new Dimension(20, 20));
            swatch.setBackground(c);
            swatch.setToolTipText(String.format("RGB: %d, %d, %d", c.getRed(), c.getGreen(), c.getBlue()));
            swatch.addActionListener(e -> colorChooser.setColor(c));
            recentColorsPanel.add(swatch);
        }
        recentColorsPanel.revalidate();
        recentColorsPanel.repaint();
    }

    private void addRecentColor(Color c) {
        if (c.getAlpha() == 0) return; // Ignore eraser
        recentColors.remove(c); // Remove if exists to move to front
        recentColors.add(0, c);
        if (recentColors.size() > 10) {
            recentColors.remove(recentColors.size() - 1);
        }
        updateRecentColorsUI();
    }

    private void floodFill(int x, int y, Color targetColor) {
        Color baseColor = gridColors[x][y];
        if (baseColor.equals(targetColor)) return;
        
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));
        
        while (!queue.isEmpty()) {
            Point p = queue.remove();
            if (p.x < 0 || p.x >= GRID_SIZE || p.y < 0 || p.y >= GRID_SIZE) continue;
            if (!gridColors[p.x][p.y].equals(baseColor)) continue;
            
            gridColors[p.x][p.y] = targetColor;
            
            queue.add(new Point(p.x + 1, p.y));
            queue.add(new Point(p.x - 1, p.y));
            queue.add(new Point(p.x, p.y + 1));
            queue.add(new Point(p.x, p.y - 1));
        }
    }

    public File showDialog() {
        setVisible(true);
        return savedFile; // Will be null if canceled
    }

    private File saveToTempFile() throws IOException {
        BufferedImage image = new BufferedImage(GRID_SIZE, GRID_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                g2d.setColor(gridColors[x][y]);
                g2d.fillRect(x, y, 1, 1);
            }
        }
        g2d.dispose();

        File tempFile = File.createTempFile("drawn_texture_", ".png");
        tempFile.deleteOnExit();
        ImageIO.write(image, "PNG", tempFile);
        return tempFile;
    }

    private class SimpleCanvas extends JPanel {

        public SimpleCanvas() {
            setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
            
            MouseAdapter mouseAdapter = new MouseAdapter() {
                private boolean isErasing = false;
                
                @Override
                public void mousePressed(MouseEvent e) {
                    pushState();
                    isErasing = SwingUtilities.isRightMouseButton(e);
                    paintPixel(e.getX(), e.getY(), isErasing);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    paintPixel(e.getX(), e.getY(), isErasing);
                }
            };
            
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        private void paintPixel(int x, int y, boolean erase) {
            int gridX = x / CELL_SIZE;
            int gridY = y / CELL_SIZE;
            
            if (gridX >= 0 && gridX < GRID_SIZE && gridY >= 0 && gridY < GRID_SIZE) {
                Color drawColor = erase ? new Color(0, 0, 0, 0) : colorChooser.getColor();
                
                if (!erase && currentTool == Tool.FILL) {
                    floodFill(gridX, gridY, drawColor);
                    addRecentColor(drawColor);
                } else {
                    // Brush or Eraser with size
                    int startX = gridX - (brushSize - 1) / 2;
                    int startY = gridY - (brushSize - 1) / 2;
                    for (int i = 0; i < brushSize; i++) {
                        for (int j = 0; j < brushSize; j++) {
                            int px = startX + i;
                            int py = startY + j;
                            if (px >= 0 && px < GRID_SIZE && py >= 0 && py < GRID_SIZE) {
                                gridColors[px][py] = drawColor;
                            }
                        }
                    }
                    if (!erase) addRecentColor(drawColor);
                }
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            // Draw checkered background to indicate transparency
            for (int x = 0; x < GRID_SIZE; x++) {
                for (int y = 0; y < GRID_SIZE; y++) {
                    if ((x + y) % 2 == 0) {
                        g2d.setColor(Color.LIGHT_GRAY);
                    } else {
                        g2d.setColor(Color.WHITE);
                    }
                    g2d.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    
                    // Draw actual color
                    Color c = gridColors[x][y];
                    if (c.getAlpha() > 0) {
                        g2d.setColor(c);
                        g2d.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                }
            }

            // Draw grid lines
            g2d.setColor(Color.DARK_GRAY);
            for (int x = 0; x <= GRID_SIZE; x++) {
                g2d.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, GRID_SIZE * CELL_SIZE);
            }
            for (int y = 0; y <= GRID_SIZE; y++) {
                g2d.drawLine(0, y * CELL_SIZE, GRID_SIZE * CELL_SIZE, y * CELL_SIZE);
            }
        }
    }
}
