/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import javax.swing.*;
import java.awt.*;
/**
 *
 * @author 58412
 */
public final class PlotLinePanel extends JPanel {
    private String titulo;
    private double[] datos;

    public PlotLinePanel(String titulo) {
        this.titulo = titulo;
        this.datos = new double[0];
        setPreferredSize(new Dimension(380, 220));
    }

    public void setDatos(double[] y) {
        if (y == null) y = new double[0];
        this.datos = y;
        repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth(), h = getHeight();
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(getBackground());
        g2.fillRect(0, 0, w, h);

        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(8, 8, w - 16, h - 16);
        g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
        g2.drawString(titulo, 16, 22);

        int left = 50, right = 12, top = 28, bottom = 28;
        int pw = w - left - right;
        int ph = h - top - bottom;
        if (pw <= 10 || ph <= 10) return;

        g2.setColor(new Color(230,230,230));
        for (int i = 0; i <= 5; i++) {
            int y = top + i * ph / 5;
            g2.drawLine(left, y, left + pw, y);
        }

        g2.setColor(Color.BLACK);
        g2.drawRect(left, top, pw, ph);

        if (datos.length == 0) return;

        double max = 0.0;
        for (int i = 0; i < datos.length; i++) if (datos[i] > max) max = datos[i];
        if (max <= 0) max = 1;

        int n = datos.length;
        int prevx = -1, prevy = -1;
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(30,144,255));

        for (int i = 0; i < n; i++) {
            double vx = (n == 1) ? 0.0 : (i / (double) (n - 1));
            int x = left + (int) Math.round(vx * pw);
            int y = top + ph - (int) Math.round((datos[i] / max) * ph);
            if (prevx >= 0) g2.drawLine(prevx, prevy, x, y);
            prevx = x;
            prevy = y;
        }

        g2.setColor(Color.GRAY);
        g2.setFont(getFont().deriveFont(11f));
        g2.drawString("0", left - 20, top + ph + 4);
        g2.drawString(String.format("%.0f", max), left - 32, top + 4);
    }
}
