/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

import java.util.concurrent.locks.ReentrantLock;
import modelo.EstadoProceso;
import modelo.Proceso;
import modelo.TipoProceso;

/**
 *
 * @author 58412
 */
public class ColaSuspendidos {
    private static final class Nodo {
        Proceso p;
        boolean bloqueado;
        int esperaIO;
        Nodo sig;
        Nodo(Proceso p, boolean bloqueado, int esperaIO) { this.p = p; this.bloqueado = bloqueado; this.esperaIO = esperaIO; }
    }

    private Nodo head, tail;
    private int size;
    private final ReentrantLock lock = new ReentrantLock();

    public void suspenderListo(Proceso p) {
        if (p == null) return;
        lock.lock();
        try {
            p.setEstado(EstadoProceso.SUSPENDIDO);
            Nodo n = new Nodo(p, false, 0);
            if (tail == null) { head = tail = n; } else { tail.sig = n; tail = n; }
            size++;
        } finally { lock.unlock(); }
    }

    public void suspenderBloqueado(Proceso p, int espera) {
        if (p == null) return;
        int w = Math.max(1, espera);
        lock.lock();
        try {
            p.setEstado(EstadoProceso.SUSPENDIDO);
            Nodo n = new Nodo(p, true, w);
            if (tail == null) { head = tail = n; } else { tail.sig = n; tail = n; }
            size++;
        } finally { lock.unlock(); }
    }

    public boolean esVacia() {
        lock.lock();
        try { return size == 0; } finally { lock.unlock(); }
    }

    public int getSize() {
        return size;
    }

    public Proceso reactivarListo() {
        lock.lock();
        try {
            Nodo prev = null, cur = head;
            while (cur != null) {
                if (!cur.bloqueado) {
                    Proceso p = cur.p;
                    if (cur == head) head = cur.sig; else prev.sig = cur.sig;
                    if (cur == tail) tail = prev;
                    size--;
                    p.setEstado(EstadoProceso.LISTO);
                    return p;
                }
                prev = cur;
                cur = cur.sig;
            }
            return null;
        } finally { lock.unlock(); }
    }

    public Proceso[] avanzarUnCicloYReactivarSiCorresponde() {
        lock.lock();
        try {
            java.util.ArrayList<Proceso> list = new java.util.ArrayList<>();
            Nodo prev = null, cur = head;
            while (cur != null) {
                if (cur.bloqueado) {
                    cur.esperaIO--;
                    if (cur.esperaIO <= 0) {
                        Proceso p = cur.p;
                        if (cur == head) head = cur.sig; else prev.sig = cur.sig;
                        if (cur == tail) tail = prev;
                        size--;
                        p.setEstado(EstadoProceso.SUSPENDIDO);
                        list.add(p);
                        cur = (prev == null) ? head : prev.sig;
                        continue;
                    }
                }
                prev = cur;
                cur = cur.sig;
            }
            return list.toArray(new Proceso[0]);
        } finally { lock.unlock(); }
    }

    public Object[][] toTableData() {
        lock.lock();
        try {
            Object[][] data = new Object[size][8];
            int i = 0;
            for (Nodo t = head; t != null; t = t.sig) {
                Proceso p = t.p;
                data[i][0] = p.getPid();
                data[i][1] = p.getNombre();
                data[i][2] = p.getTipo() == TipoProceso.CPU_BOUND ? "CPU" : "IO";
                data[i][3] = t.bloqueado ? "SB" : "SL";
                data[i][4] = p.getRestantes();
                data[i][5] = p.getTotalInstrucciones();
                data[i][6] = p.getPrioridad();
                data[i][7] = t.bloqueado ? t.esperaIO : 0;
                i++;
            }
            return data;
        } finally { lock.unlock(); }
    }
}