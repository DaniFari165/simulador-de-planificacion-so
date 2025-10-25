/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io;

import control.Kernel;
import modelo.Proceso;

/**
 *
 * @author 58412
 */
public class IODevice extends Thread {
    private static final class Nodo {
        Proceso p;
        int ciclosServicio;
        int msPorCiclo;
        Nodo sig;
        Nodo(Proceso p, int serv, int ms) { this.p = p; this.ciclosServicio = serv; this.msPorCiclo = ms; }
    }

    private final Kernel kernel;
    private final Object qlock = new Object();
    private Nodo head, tail;
    private boolean activo = true;

    public IODevice(Kernel k) {
        this.kernel = k;
        setDaemon(true);
        setName("IODevice");
    }

    public void encolarIO(Proceso p, int ciclosServicio, int msPorCiclo) {
        if (p == null) return;
        if (ciclosServicio < 1) ciclosServicio = 1;
        Nodo n = new Nodo(p, ciclosServicio, msPorCiclo);
        synchronized (qlock) {
            if (tail == null) { head = tail = n; } else { tail.sig = n; tail = n; }
            qlock.notifyAll();
        }
    }

    public void detener() {
        activo = false;
        synchronized (qlock) { qlock.notifyAll(); }
        interrupt();
    }

    @Override
    public void run() {
        while (activo) {
            Nodo job = null;
            synchronized (qlock) {
                while (activo && head == null) {
                    try { qlock.wait(); } catch (InterruptedException ignored) {}
                }
                if (!activo) break;
                job = head;
                head = head.sig;
                if (head == null) tail = null;
            }
            if (job != null) {
                try {
                    long ms = (long) job.ciclosServicio * (long) job.msPorCiclo;
                    Thread.sleep(Math.max(1L, ms));
                } catch (InterruptedException ignored) {}
                kernel.ioCompleto(job.p);
            }
        }
    }
}
