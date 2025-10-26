/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import control.Kernel;
import javax.swing.*;
import java.awt.*;
/**
 *
 * @author Daniel Fariña
 */
public final class GraficasPanel extends JPanel {
    private final Kernel kernel;
    private final PlotLinePanel gUso = new PlotLinePanel("Utilización CPU (%)");
    private final PlotLinePanel gThr = new PlotLinePanel("Throughput (proc/ventana)");
    private final PlotLinePanel gResp = new PlotLinePanel("Respuesta promedio");

    public GraficasPanel(Kernel k) {
        this.kernel = k;
        setLayout(new GridLayout(1,3,8,8));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        add(gUso);
        add(gThr);
        add(gResp);
    }

    public void refrescar() {
        gUso.setDatos(kernel.serieUsoCPU());
        gThr.setDatos(kernel.serieThroughput());
        gResp.setDatos(kernel.serieRespProm());
    }
}