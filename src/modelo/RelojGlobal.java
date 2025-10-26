/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.function.BiConsumer;

/**
 *
 * @author Daniel Fariña
 */
public class RelojGlobal extends Thread {
    private volatile int cicloActual = 0;
    private volatile int duracionCiclo = 300;
    private volatile boolean activo = false;
    private volatile boolean pausado = false;

    private final Object lockPause = new Object();
    private final BiConsumer<Integer,Long>[] listeners = new BiConsumer[10];
    private int listenerCount = 0;

    public RelojGlobal() {
        setDaemon(true);
        setName("RelojGlobal");
    }

    public void addListener(BiConsumer<Integer,Long> l) {
        if (l == null) return;
        synchronized (listeners) {
            if (listenerCount < listeners.length) listeners[listenerCount++] = l;
        }
    }

    public void iniciar() {
        if (activo) return;
        activo = true;
        start();
    }

    public void detener() {
        activo = false;
        synchronized (lockPause) { pausado = false; lockPause.notifyAll(); }
        interrupt();
    }

    public void pausar() {
        synchronized (lockPause) { pausado = true; }
    }

    public void continuar() {
        synchronized (lockPause) { pausado = false; lockPause.notifyAll(); }
    }

    public boolean isPausado() {
        return pausado;
    }

    public void setDuracionCiclo(int ms) {
        duracionCiclo = Math.max(10, ms);
    }
    public int getDuracionCiclo() {
        return duracionCiclo;
    }
    public int getCicloActual() {
        return cicloActual;
    }

    @Override
    public void run() {
        long next = System.currentTimeMillis();
        while (activo) {
            synchronized (lockPause) {
                while (activo && pausado) {
                    try { lockPause.wait(); } catch (InterruptedException ignored) {}
                }
            }
            if (!activo) break;
            cicloActual++;
            long ts = System.currentTimeMillis();
            for (int i = 0; i < listenerCount; i++) {
                try { listeners[i].accept(cicloActual, ts); } catch (Exception ignored) {}
            }
            next += duracionCiclo;
            long sleep = next - System.currentTimeMillis();
            if (sleep > 0) {
                try { Thread.sleep(sleep); } catch (InterruptedException ignored) {}
            } else {
                next = System.currentTimeMillis();
            }
        }
    }
}