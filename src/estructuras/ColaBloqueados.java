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
public class ColaBloqueados {
    private static final class Nodo {
        Proceso p;
        int espera;
        Nodo sig;
        Nodo(Proceso p, int espera) { this.p = p; this.espera = espera; }
    }

    private Nodo head, tail;
    private int size;
    private final ReentrantLock lock = new ReentrantLock();

    public void bloquear(Proceso p, int esperaCiclos) {
        if (p == null) return;
        int w = Math.max(1, esperaCiclos);
        lock.lock();
        try {
            Nodo n = new Nodo(p, w);
            if (tail == null) { head = tail = n; } else { tail.sig = n; tail = n; }
            size++;
            p.setEstado(EstadoProceso.BLOQUEADO);
        } finally { lock.unlock(); }
    }

    public boolean esVacia() {
        lock.lock();
        try { return size == 0; } finally { lock.unlock(); }
    }

    public int getSize() {
        return size;
    }

    public Proceso[] avanzarUnCicloYLiberar() {
        lock.lock();
        try {
            java.util.ArrayList<Proceso> libres = new java.util.ArrayList<>();
            Nodo prev = null, cur = head;
            while (cur != null) {
                cur.espera--;
                if (cur.espera <= 0) {
                    Proceso p = cur.p;
                    if (cur == head) head = cur.sig; else prev.sig = cur.sig;
                    if (cur == tail) tail = prev;
                    size--;
                    p.setEstado(EstadoProceso.LISTO);
                    libres.add(p);
                    cur = (prev == null) ? head : prev.sig;
                } else {
                    prev = cur;
                    cur = cur.sig;
                }
            }
            return libres.toArray(new Proceso[0]);
        } finally { lock.unlock(); }
    }

    public String[] toDisplayStrings() {
        lock.lock();
        try {
            String[] arr = new String[size];
            int i = 0;
            for (Nodo t = head; t != null; t = t.sig) {
                Proceso p = t.p;
                arr[i++] = "PID " + p.getPid() + " · " + p.getNombre() + " · esperaIO=" + t.espera;
            }
            return arr;
        } finally { lock.unlock(); }
    }

    public Object[][] toTableData() {
        lock.lock();
        try {
            Object[][] data = new Object[size][7];
            int i = 0;
            for (Nodo t = head; t != null; t = t.sig) {
                Proceso p = t.p;
                data[i][0] = p.getPid();
                data[i][1] = p.getNombre();
                data[i][2] = p.getTipo() == TipoProceso.CPU_BOUND ? "CPU" : "IO";
                data[i][3] = p.getRestantes();
                data[i][4] = p.getTotalInstrucciones();
                data[i][5] = p.getPrioridad();
                data[i][6] = t.espera;
                i++;
            }
            return data;
        } finally { lock.unlock(); }
    }
}
