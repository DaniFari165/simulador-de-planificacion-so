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
public class Feedback implements Scheduler{
    private int runningPid = -1;
    private int nivelActual = 0;
    private int qRestante = 0;

    public void onTick(Kernel k) {
        if (k.cpuLibre()) {
            pickNext(k);
            return;
        }
        qRestante = Math.max(0, qRestante - 1);
        ProcesoEvento ev = k.ejecutarCPU();
        if (ev.getTipo() == ProcesoEvento.Tipo.TERMINADO) {
            runningPid = -1;
            qRestante = 0;
            k.manejarEvento(ev);
            pickNext(k);
            return;
        }
        if (ev.getTipo() == ProcesoEvento.Tipo.BLOQUEADO) {
            runningPid = -1;
            qRestante = 0;
            k.manejarEvento(ev);
            pickNext(k);
            return;
        }
        if (qRestante == 0) {
            if (k.snapshotListos().length > 0) {
                Proceso p = k.preemptarCPU();
                if (p != null) {
                    int lvl = Math.min(2, Math.max(0, p.getPrioridad()) + 1);
                    p.setPrioridad(lvl);
                    k.encolarListo(p);
                }
                runningPid = -1;
                pickNext(k);
            } else {
                qRestante = quantumByLevel(nivelActual);
            }
        }
    }

    private void pickNext(Kernel k) {
        Proceso[] listos = k.snapshotListos();
        if (listos.length == 0) return;
        int bestIdx = -1;
        int bestLvl = Integer.MAX_VALUE;
        for (int i = 0; i < listos.length; i++) {
            int lvl = clampLevel(listos[i].getPrioridad());
            if (lvl < bestLvl) { bestLvl = lvl; bestIdx = i; }
        }
        Proceso elegido = takeByPid(k, listos[bestIdx].getPid());
        if (elegido != null) {
            int lvl = clampLevel(elegido.getPrioridad());
            nivelActual = lvl;
            qRestante = quantumByLevel(lvl);
            runningPid = elegido.getPid();
            k.asignarCPU(elegido);
        }
    }

    private int clampLevel(int p) { return p < 0 ? 0 : (p > 2 ? 2 : p); }

    private int quantumByLevel(int lvl) {
        if (lvl <= 0) return 2;
        if (lvl == 1) return 4;
        return 8;
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
        return "Feedback";
    }
    
}
