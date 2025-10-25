/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificador;

import control.Kernel;
import modelo.ProcesoEvento;
import modelo.Proceso;
import modelo.EstadoProceso;


/**
 *
 * @author 58412
 */
public class RoundRobin implements Scheduler {
    
    private int quantum;
    private int restante;
    private int pidActual;

    public RoundRobin(int quantum) {
        this.quantum = Math.max(1, quantum);
        this.restante = this.quantum;
        this.pidActual = -1;
    }

    public void setQuantum(int q) {
        this.quantum = Math.max(1, q);
        if (restante > quantum) restante = quantum;
    }

    public void onTick(Kernel k) {
        Proceso actual = k.getProcesoActual();
        if (actual == null) {
            if (!k.listosVacio()) {
                Proceso p = k.desencolarListo();
                k.asignarCPU(p);
                pidActual = p.getPid();
                restante = quantum;
            }
        } else {
            if (actual.getPid() != pidActual) {
                pidActual = actual.getPid();
                restante = quantum;
            }
        }

        ProcesoEvento ev = k.ejecutarCPU();
        if (ev.getTipo() == ProcesoEvento.Tipo.NINGUNO) {
            restante--;
            if (restante <= 0) {
                Proceso prev = k.preemptarCPU();
                if (prev != null) {
                    prev.setEstado(EstadoProceso.LISTO);
                    k.encolarListo(prev);
                }
                if (!k.listosVacio()) {
                    Proceso p = k.desencolarListo();
                    k.asignarCPU(p);
                    pidActual = p.getPid();
                    restante = quantum;
                } else {
                    pidActual = -1;
                    restante = quantum;
                }
            }
        } else {
            k.manejarEvento(ev);
            pidActual = -1;
            restante = quantum;
        }

        k.liberarBloqueadosAListos();

        if (k.cpuLibre() && !k.listosVacio()) {
            Proceso p = k.desencolarListo();
            k.asignarCPU(p);
            pidActual = p.getPid();
            restante = quantum;
        }
    }

    public String name() {
        return "RR(" + quantum + ")";
    }
}
