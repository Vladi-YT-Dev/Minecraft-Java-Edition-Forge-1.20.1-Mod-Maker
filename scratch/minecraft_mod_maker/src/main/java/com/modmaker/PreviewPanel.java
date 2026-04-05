package com.modmaker;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PreviewPanel extends JPanel {
    private double angle = 0;
    private Timer timer;

    private File topFile, bottomFile, sideFile, itemFile;
    private final Map<File, BufferedImage> textureCache = new HashMap<>();
    private boolean isBlockMode = true;
    private boolean showPreview = true;

    public PreviewPanel() {
        setPreferredSize(new Dimension(200, 300));
        setBackground(new Color(40, 40, 45));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        timer = new Timer(16, e -> {
            angle += 0.01;
            repaint();
        });
        timer.start();
    }

    public void setBlockTextures(File top, File bottom, File side) {
        this.topFile = top;
        this.bottomFile = bottom;
        this.sideFile = side;
        this.isBlockMode = true;
        this.showPreview = true;
        loadToCache(top);
        loadToCache(bottom);
        loadToCache(side);
        repaint();
    }

    public void setItemTexture(File item) {
        this.itemFile = item;
        this.isBlockMode = false;
        this.showPreview = true;
        loadToCache(item);
        repaint();
    }

    public void clearPreview() {
        this.showPreview = false;
        repaint();
    }

    private void loadToCache(File f) {
        if (f == null || !f.exists()) return;
        if (!textureCache.containsKey(f)) {
            try {
                textureCache.put(f, ImageIO.read(f));
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (!showPreview) return;

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        if (isBlockMode) {
            renderCube(g2d, cx, cy, 60);
        } else {
            renderItem(g2d, cx, cy, 80);
        }
    }

    private void renderCube(Graphics2D g2d, int cx, int cy, int size) {
        double[][] points = {
            {-1, -1, -1}, {1, -1, -1}, {1, 1, -1}, {-1, 1, -1},
            {-1, -1, 1}, {1, -1, 1}, {1, 1, 1}, {-1, 1, 1}
        };

        double rx = 0.4;
        double ry = angle;

        double[][] rotated = new double[8][3];
        for (int i = 0; i < 8; i++) {
            double x = points[i][0];
            double y = points[i][1];
            double z = points[i][2];

            double x1 = x * Math.cos(ry) - z * Math.sin(ry);
            double z1 = x * Math.sin(ry) + z * Math.cos(ry);

            double y2 = y * Math.cos(rx) - z1 * Math.sin(rx);
            double z2 = y * Math.sin(rx) + z1 * Math.cos(rx);

            rotated[i][0] = x1;
            rotated[i][1] = y2;
            rotated[i][2] = z2;
        }

        Point[] proj = new Point[8];
        double fov = 400, dist = 4;
        for (int i = 0; i < 8; i++) {
            double s = fov / (rotated[i][2] + dist);
            proj[i] = new Point((int) (cx + rotated[i][0] * size * s / 100), (int) (cy + rotated[i][1] * size * s / 100));
        }

        int[][] faceIndices = {
            {5, 4, 7, 6}, {0, 1, 2, 3}, {4, 0, 3, 7}, {1, 5, 6, 2}, {0, 1, 5, 4}, {7, 6, 2, 3}
        };
        File[] faceFiles = {sideFile, sideFile, sideFile, sideFile, topFile, bottomFile};

        Integer[] order = {0, 1, 2, 3, 4, 5};
        java.util.Arrays.sort(order, (a, b) -> {
            double az = 0, bz = 0;
            for (int i : faceIndices[a]) az += rotated[i][2];
            for (int i : faceIndices[b]) bz += rotated[i][2];
            return Double.compare(bz, az);
        });

        for (int f : order) {
            drawWarpedFace(g2d, proj[faceIndices[f][0]], proj[faceIndices[f][1]], proj[faceIndices[f][2]], proj[faceIndices[f][3]], textureCache.get(faceFiles[f]));
        }
    }

    private void renderItem(Graphics2D g2d, int cx, int cy, int size) {
        double ry = angle;
        double rx = 0.2;
        
        double[][] points = {{-1, -1, 0}, {1, -1, 0}, {1, 1, 0}, {-1, 1, 0}};
        Point[] proj = new Point[4];
        double fov = 400, dist = 4;
        
        for(int i=0; i<4; i++) {
            double x = points[i][0], y = points[i][1], z = points[i][2];
            double x1 = x * Math.cos(ry) - z * Math.sin(ry);
            double z1 = x * Math.sin(ry) + z * Math.cos(ry);
            double y2 = y * Math.cos(rx) - z1 * Math.sin(rx);
            double z2 = y * Math.sin(rx) + z1 * Math.cos(rx);
            double s = fov / (z2 + dist);
            proj[i] = new Point((int)(cx + x1 * size * s / 100), (int)(cy + y2 * size * s / 100));
        }
        
        drawWarpedFace(g2d, proj[0], proj[1], proj[2], proj[3], textureCache.get(itemFile));
    }

    private void drawWarpedFace(Graphics2D g2d, Point p1, Point p2, Point p3, Point p4, BufferedImage img) {
        if (img == null) {
            Path2D path = new Path2D.Double();
            path.moveTo(p1.x, p1.y); path.lineTo(p2.x, p2.y); path.lineTo(p3.x, p3.y); path.lineTo(p4.x, p4.y);
            path.closePath();
            g2d.setColor(Color.GRAY);
            g2d.fill(path);
            g2d.setColor(Color.WHITE);
            g2d.draw(path);
            return;
        }

        int w = img.getWidth(), h = img.getHeight();
        int divisions = 8;
        
        for (int row = 0; row < divisions; row++) {
            for (int col = 0; col < divisions; col++) {
                double u1 = (double) col / divisions;
                double u2 = (double) (col + 1) / divisions;
                double v1 = (double) row / divisions;
                double v2 = (double) (row + 1) / divisions;

                Point q1 = getBilinearPoint(p1, p2, p3, p4, u1, v1);
                Point q2 = getBilinearPoint(p1, p2, p3, p4, u2, v1);
                Point q3 = getBilinearPoint(p1, p2, p3, p4, u2, v2);
                Point q4 = getBilinearPoint(p1, p2, p3, p4, u1, v2);

                drawTriangle(g2d, img, u1 * w, v1 * h, u2 * w, v1 * h, u2 * w, v2 * h, q1.x, q1.y, q2.x, q2.y, q3.x, q3.y);
                drawTriangle(g2d, img, u1 * w, v1 * h, u2 * w, v2 * h, u1 * w, v2 * h, q1.x, q1.y, q3.x, q3.y, q4.x, q4.y);
            }
        }
    }

    private Point getBilinearPoint(Point p1, Point p2, Point p3, Point p4, double u, double v) {
        double x = (1 - u) * (1 - v) * p1.x + u * (1 - v) * p2.x + u * v * p3.x + (1 - u) * v * p4.x;
        double y = (1 - u) * (1 - v) * p1.y + u * (1 - v) * p2.y + u * v * p3.y + (1 - u) * v * p4.y;
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    private void drawTriangle(Graphics2D g2d, BufferedImage img, double u1, double v1, double u2, double v2, double u3, double v3, double x1, double y1, double x2, double y2, double x3, double y3) {
        Path2D path = new Path2D.Double();
        path.moveTo(x1, y1); path.lineTo(x2, y2); path.lineTo(x3, y3); path.closePath();
        
        g2d.setClip(path);
        AffineTransform at = getTransform(u1, v1, u2, v2, u3, v3, x1, y1, x2, y2, x3, y3);
        if (at != null) g2d.drawImage(img, at, null);
        g2d.setClip(null);
    }

    private AffineTransform getTransform(double u1, double v1, double u2, double v2, double u3, double v3, double x1, double y1, double x2, double y2, double x3, double y3) {
        double det = u1 * (v2 - v3) - v1 * (u2 - u3) + (u2 * v3 - u3 * v2);
        if (Math.abs(det) < 0.0001) return null;
        double a = (x1 * (v2 - v3) - v1 * (x2 - x3) + (x2 * v3 - x3 * v2)) / det;
        double b = (u1 * (x2 - x3) - x1 * (u2 - u3) + (u2 * x3 - u3 * x2)) / det;
        double c = (u1 * (v2 * x3 - v3 * x2) - v1 * (u2 * x3 - u3 * x2) + x1 * (u2 * v3 - u3 * v2)) / det;
        double d = (y1 * (v2 - v3) - v1 * (y2 - y3) + (y2 * v3 - y3 * v2)) / det;
        double e = (u1 * (y2 - y3) - y1 * (u2 - u3) + (u2 * y3 - u3 * y2)) / det;
        double f = (u1 * (v2 * y3 - v3 * y2) - v1 * (u2 * y3 - u3 * y2) + y1 * (u2 * v3 - u3 * v2)) / det;
        return new AffineTransform(a, d, b, e, c, f);
    }
}
