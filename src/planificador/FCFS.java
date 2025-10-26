/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificador;

import control.Kernel;
import modelo.ProcesoEvento;

/**
 *
 * @author Daniel Fariña
 */
public class FCFS implements Scheduler {
    public void onTick(Kernel k) {
        if (k.cpuLibre() && !k.listosVacio()) k.asignarCPU(k.desencolarListo());
        ProcesoEvento ev = k.ejecutarCPU();
        k.manejarEvento(ev);
        k.liberarBloqueadosAListos();
    }
    public String name() {
        return "FCFS";
    }
}
