/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificador;

import control.Kernel;
import modelo.Proceso;
import modelo.ProcesoEvento; 
/**
 *
 * @author Usuario
 */
public class HRRN implements Scheduler {
    
    public void onTick(Kernel k) {
        if (k.cpuLibre()) {
            Proceso[] listos = k.snapshotListos();
            if (listos.length == 0) return;
            int idx = 0;
            double best = ratio(listos[0]);
            for (int i = 1; i < listos.length; i++) {
                double r = ratio(listos[i]);
                if (r > best || (r == best && listos[i].getRestantes() < listos[idx].getRestantes())) {
                    best = r; idx = i;
                }
            }
            Proceso elegido = takeByPid(k, listos[idx].getPid());
            if (elegido != null) k.asignarCPU(elegido);
            return;
        }
        ProcesoEvento ev = k.ejecutarCPU();
        if (ev.getTipo() != ProcesoEvento.Tipo.NINGUNO) k.manejarEvento(ev);
    }

    private double ratio(Proceso p) {
        int s = Math.max(1, p.getRestantes());
        int w = Math.max(0, p.getTiempoEsperaAcumulado());
        return (w + s) / (double) s;
    }

    private Proceso takeByPid(Kernel k, int pid) {
        Proceso[] arr = k.snapshotListos();
        int n = arr.length;
        Proceso picked = null;
        for (int i = 0; i < n; i++) {
            Proceso x = k.desencolarListo();
            if (picked == null && x.getPid() == pid) picked = x; else k.encolarListo(x);
        }
        return picked;
    }
    
    public String name() {
        return "HRRN";
    }
}
