/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificador;

import control.Kernel; 
import modelo.Proceso;
import modelo.EstadoProceso;
import modelo.ProcesoEvento;


/**
 *
 * @author Carlos De Freitas
 */
public class SRT implements Scheduler{
    public void onTick(Kernel k) {
        Proceso actual = k.getProcesoActual();
        if (actual == null) {
            if (!k.listosVacio()) k.asignarCPU(k.desencolarListoMinRestantes());
        } else {
            Proceso mejor = k.peekListoMinRestantes();
            if (mejor != null && mejor.getRestantes() < actual.getRestantes()) {
                Proceso prev = k.preemptarCPU();
                if (prev != null) prev.setEstado(EstadoProceso.LISTO);
                if (prev != null) k.encolarListo(prev);
                k.asignarCPU(k.desencolarListoMinRestantes());
            }
        }
        ProcesoEvento ev = k.ejecutarCPU();
        k.manejarEvento(ev);
        k.liberarBloqueadosAListos();
    }
    public String name() {
        return "SRT";
    }
}
