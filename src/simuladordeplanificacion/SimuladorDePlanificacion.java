/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladordeplanificacion;

import control.Kernel;
import javax.swing.SwingUtilities;
import vista.VentanaPrincipal;

/**
 *
 * @author 58412
 */
public class SimuladorDePlanificacion {

    /**
     * @param args the command line arguments
     */
        public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal(new Kernel());
            ventana.setVisible(true);
        });
    }
}
