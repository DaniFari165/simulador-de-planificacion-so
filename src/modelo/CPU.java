/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.concurrent.Semaphore;

/**
 *
 * @author 58412
 */
public class CPU {
    private final Semaphore mutex = new Semaphore(1);
    private volatile Proceso actual;

    public boolean estaLibre() {
        return actual == null && mutex.availablePermits() == 1;
    }
    public Proceso getActual() {
        return actual;
    }

    public void asignar(Proceso p) {
        if (p == null) return;
        if (!mutex.tryAcquire()) return;
        actual = p;
    }

    public Proceso preempt() {
        Proceso prev = actual;
        actual = null;
        if (mutex.availablePermits() == 0) mutex.release();
        return prev;
    }

    public void liberar() {
        actual = null;
        if (mutex.availablePermits() == 0) mutex.release();
    }

    public ProcesoEvento tick() {
        if (actual == null) return new ProcesoEvento(ProcesoEvento.Tipo.NINGUNO, null, 0);
        actual.solicitarPaso();
        actual.esperarFinPaso();
        ProcesoEvento ev = actual.getUltimoEvento();
        if (ev.getTipo() == ProcesoEvento.Tipo.TERMINADO || ev.getTipo() == ProcesoEvento.Tipo.BLOQUEADO) {
            actual = null;
            if (mutex.availablePermits() == 0) mutex.release();
        }
        return ev;
    }
}
