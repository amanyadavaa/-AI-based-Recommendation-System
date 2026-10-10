package com.guvi.recommend.gui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Custom Swing component rendering a tri-color sentiment analysis bar.
 */
public class SentimentBarComponent extends JComponent {
    private int positivePercent = 85;
    private int neutralPercent = 10;
    private int negativePercent = 5;

    public SentimentBarComponent() {
        setPreferredSize(new Dimension(300, 24));
    }

    public void setPercentages(int positive, int neutral, int negative) {
        this.positivePercent = Math.max(0, positive);
        this.neutralPercent = Math.max(0, neutral);
        this.negativePercent = Math.max(0, negative);
        repaint();
    }

    public int getPositivePercent() { return positivePercent; }
    public int getNeutralPercent() { return neutralPercent; }
    public int getNegativePercent() { return negativePercent; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int total = positivePercent + neutralPercent + negativePercent;
        if (total == 0) total = 100;

        int posW = (int) ((double) positivePercent / total * w);
        int neuW = (int) ((double) neutralPercent / total * w);
        int negW = w - posW - neuW;

        // Clip rounded rectangle
        g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 12, 12));

        // 1. Positive (Green)
        g2.setColor(new Color(16, 185, 129));
        g2.fillRect(0, 0, posW, h);

        // 2. Neutral (Amber)
        g2.setColor(new Color(245, 158, 11));
        g2.fillRect(posW, 0, neuW, h);

        // 3. Negative (Rose)
        g2.setColor(new Color(244, 63, 94));
        g2.fillRect(posW + neuW, 0, negW, h);

        g2.dispose();
    }
}
