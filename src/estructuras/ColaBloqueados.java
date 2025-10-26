/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

import modelo.EstadoProceso;
import modelo.Proceso;

/**
 *
 * @author Daniel Fariña
 */
public class ColaBloqueados {
    private static final class Nodo {
        Proceso p;
        int espera;
        Nodo sig;
        Nodo(Proceso p, int e) {
            this.p = p; this.espera = e;
        }
    }

    private Nodo head, tail;
    private int size;

    public void bloquear(Proceso p, int esperaCiclos) {
        if (p == null) return;
        int w = Math.max(1, esperaCiclos);
        Nodo n = new Nodo(p, w);
        if (tail == null) { head = tail = n; } else { tail.sig = n; tail = n; }
        size++;
        p.setEstado(EstadoProceso.BLOQUEADO);
    }

    public int getSize() {
        return size;
    }
    
    public boolean esVacia() {
        return size == 0;
    }

    public void decrementarUnCiclo() {
        for (Nodo t = head; t != null; t = t.sig) if (t.espera > 0) t.espera--;
    }

    public void liberarPorPid(int pid) {
        Nodo prev = null, cur = head;
        while (cur != null) {
            if (cur.p.getPid() == pid) {
                if (cur == head) head = cur.sig; else prev.sig = cur.sig;
                if (cur == tail) tail = prev;
                size--;
                return;
            }
            prev = cur;
            cur = cur.sig;
        }
    }

    public String[] toDisplayStrings() {
        String[] arr = new String[size];
        int i = 0;
        for (Nodo t = head; t != null; t = t.sig) {
            Proceso p = t.p;
            arr[i++] = "PID " + p.getPid() + " · " + p.getNombre() + " · esperaIO=" + t.espera;
        }
        return arr;
    }

    public Object[][] toTableData() {
        Object[][] data = new Object[size][7];
        int i = 0;
        for (Nodo t = head; t != null; t = t.sig) {
            Proceso p = t.p;
            data[i][0] = p.getPid();
            data[i][1] = p.getNombre();
            data[i][2] = p.getTipo().name();
            data[i][3] = p.getRestantes();
            data[i][4] = p.getTotalInstrucciones();
            data[i][5] = p.getPrioridad();
            data[i][6] = t.espera;
            i++;
        }
        return data;
    }
}