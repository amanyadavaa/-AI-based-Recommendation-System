package com.guvi.recommend.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom Swing component drawing interactive 2D Graphics bar charts for recommendation analytics.
 */
public class AnalyticsChartPanel extends JPanel {
    private final Map<String, Double> categoryCtrData = new LinkedHashMap<>();

    public AnalyticsChartPanel() {
        setPreferredSize(new Dimension(500, 240));
        setBackground(new Color(30, 41, 59));
        initSampleData();
    }

    private void initSampleData() {
        categoryCtrData.put("PRODUCT", 26.5);
        categoryCtrData.put("MOVIE", 31.2);
        categoryCtrData.put("PLACE", 22.8);
        categoryCtrData.put("FOOD", 28.4);
    }

    public void updateData(Map<String, Double> data) {
        categoryCtrData.clear();
        categoryCtrData.putAll(data);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int padding = 40;

        // Title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.drawString("Click-Through Rate (CTR %) by Category", padding, 25);

        if (categoryCtrData.isEmpty()) {
            g2.dispose();
            return;
        }

        int barWidth = (w - (2 * padding)) / categoryCtrData.size() - 25;
        double maxVal = 40.0;
        int x = padding + 15;
        int baselineY = h - padding;

        // Axis line
        g2.setColor(new Color(71, 85, 105));
        g2.drawLine(padding, baselineY, w - padding, baselineY);

        Color[] colors = {
                new Color(99, 102, 241),
                new Color(168, 85, 247),
                new Color(245, 158, 11),
                new Color(244, 63, 94)
        };

        int idx = 0;
        for (Map.Entry<String, Double> entry : categoryCtrData.entrySet()) {
            double val = entry.getValue();
            int barHeight = (int) ((val / maxVal) * (baselineY - 50));
            int y = baselineY - barHeight;

            Color c = colors[idx % colors.length];
            g2.setColor(c);
            g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

            // Value text
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String valStr = String.format("%.1f%%", val);
            g2.drawString(valStr, x + (barWidth / 2) - 15, y - 6);

            // Category Label
            g2.setColor(new Color(148, 163, 184));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.drawString(entry.getKey(), x + (barWidth / 2) - 18, baselineY + 18);

            x += barWidth + 25;
            idx++;
        }

        g2.dispose();
    }
}
