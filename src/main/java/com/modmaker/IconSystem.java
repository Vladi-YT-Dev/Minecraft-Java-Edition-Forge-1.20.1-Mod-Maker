package com.modmaker;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;

public class IconSystem {
    private static final Map<String, Color> COLOR_MAP = new HashMap<>();
    private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>();
    private static final ExecutorService FETCH_SERVICE = Executors.newFixedThreadPool(4);
    private static final String WIKI_BASE = "https://minecraft.wiki/w/Special:FilePath/";
    private static final String LOCAL_CACHE_DIR = "cache/icons/";

    static {
        COLOR_MAP.put("iron", new Color(200, 200, 200));
        COLOR_MAP.put("gold", new Color(255, 230, 0));
        COLOR_MAP.put("diamond", new Color(80, 240, 255));
        COLOR_MAP.put("emerald", new Color(50, 255, 100));
        COLOR_MAP.put("netherite", new Color(60, 50, 60));
        COLOR_MAP.put("redstone", new Color(255, 0, 0));
        COLOR_MAP.put("lapis", new Color(0, 0, 255));
        COLOR_MAP.put("coal", new Color(30, 30, 30));
        COLOR_MAP.put("quartz", new Color(240, 240, 240));

        COLOR_MAP.put("log", new Color(139, 69, 19));
        COLOR_MAP.put("planks", new Color(210, 180, 140));
        COLOR_MAP.put("stone", new Color(128, 128, 128));
        COLOR_MAP.put("cobblestone", new Color(100, 100, 100));
        COLOR_MAP.put("dirt", new Color(160, 82, 45));
        COLOR_MAP.put("grass", new Color(34, 139, 34));
        COLOR_MAP.put("sand", new Color(238, 232, 170));
        COLOR_MAP.put("gravel", new Color(105, 105, 105));
        COLOR_MAP.put("obsidian", new Color(30, 10, 40));

        COLOR_MAP.put("stick", new Color(139, 69, 19));
        COLOR_MAP.put("bucket", new Color(192, 192, 192));
        COLOR_MAP.put("chest", new Color(139, 69, 19));
        COLOR_MAP.put("furnace", new Color(80, 80, 80));
    }

    private static void setupCacheDir() {
        File dir = new File(LOCAL_CACHE_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    public static ImageIcon getIcon(String itemId, int size) {
        return getIcon(itemId, size, null);
    }

    public static ImageIcon getIcon(String itemId, int size, Runnable onLoaded) {
        if (itemId == null || itemId.isEmpty() || "minecraft:air".equals(itemId)) return null;

        String cacheKey = itemId + "_" + size;
        if (CACHE.containsKey(cacheKey)) return CACHE.get(cacheKey);

        // Immediate Fallback: Generate stylized icon
        ImageIcon fallback = generateFallbackIcon(itemId, size);
        CACHE.put(cacheKey, fallback);

        // Async Fetch from Wiki
        FETCH_SERVICE.submit(() -> {
            ImageIcon remote = fetchFromWiki(itemId, size);
            if (remote != null) {
                CACHE.put(cacheKey, remote);
                if (onLoaded != null) {
                    SwingUtilities.invokeLater(onLoaded);
                }
            }
        });

        return fallback;
    }

    private static ImageIcon fetchFromWiki(String itemId, int size) {
        setupCacheDir();
        String name = itemId.replace("minecraft:", "");
        // Format: iron_ingot -> Iron_Ingot.png
        String[] parts = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0)))
              .append(parts[i].substring(1));
            if (i < parts.length - 1) sb.append("_");
        }
        String wikiFile = sb.toString() + ".png";
        
        File cachedFile = new File(LOCAL_CACHE_DIR, wikiFile);
        if (!cachedFile.exists()) {
            try {
                URL url = new URL(WIKI_BASE + wikiFile);
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                try (InputStream in = conn.getInputStream();
                     OutputStream out = new FileOutputStream(cachedFile)) {
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
                }
            } catch (Exception e) {
                // If special characters fail, we just don't cache
                return null;
            }
        }

        if (cachedFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(cachedFile);
                if (img != null) {
                    Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return null;
    }

    private static ImageIcon generateFallbackIcon(String itemId, int size) {
        String name = itemId.replace("minecraft:", "").toLowerCase();
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        
        Color baseColor = Color.GRAY;
        for (Map.Entry<String, Color> entry : COLOR_MAP.entrySet()) {
            if (name.contains(entry.getKey())) {
                baseColor = entry.getValue();
                break;
            }
        }

        g2d.setColor(baseColor.darker());
        g2d.fillRect(0, 0, size, size);
        g2d.setColor(baseColor);
        g2d.fillRect(2, 2, size - 4, size - 4);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, (int)(size * 0.4)));
        String label = name.substring(0, Math.min(2, name.length())).toUpperCase();
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(label, (size - fm.stringWidth(label)) / 2, (size + fm.getAscent()) / 2 - 2);
        
        g2d.dispose();
        return new ImageIcon(img);
    }
    
    public static ImageIcon getScaledIcon(File file, int size) {
        if (file == null || !file.exists()) return null;
        try {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }
}
