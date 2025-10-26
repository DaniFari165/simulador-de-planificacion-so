/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

import modelo.EstadoProceso;
import modelo.Proceso;
import modelo.TipoProceso;

/**
 *
 * @author Daniel Fariña
 */
public class ColaBloqueados {
    private static final class Nodo {
        Proceso p;
        int ioRestante;
        Nodo sig;
        Nodo(Proceso p, int io) { this.p = p; this.ioRestante = io; }
    }

    private Nodo head, tail;
    private int size;

    public int getSize() { return size; }
    public boolean esVacia() { return size == 0; }

    private void retirarPorPidInterno(int pid) {
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

    public void liberarPorPid(int pid) {
        retirarPorPidInterno(pid);
    }

    private void encolarUnico(Proceso p, int ioRestante) {
        retirarPorPidInterno(p.getPid());
        Nodo n = new Nodo(p, ioRestante);
        if (tail == null) { head = tail = n; } else { tail.sig = n; tail = n; }
        size++;
    }

    public void bloquear(Proceso p, int ioCiclos) {
        if (p == null) return;
        int val = Math.max(1, ioCiclos);
        p.setEstado(EstadoProceso.BLOQUEADO);
        encolarUnico(p, val);
    }

    public Proceso[] avanzarUnCicloYLiberar() {
        if (size == 0) return new Proceso[0];
        int posibles = 0;
        for (Nodo t = head; t != null; t = t.sig) if (t.ioRestante > 0) posibles++;
        Proceso[] tmp = new Proceso[posibles];
        int out = 0;

        Nodo prev = null, cur = head;
        while (cur != null) {
            if (cur.ioRestante > 0) cur.ioRestante--;
            if (cur.ioRestante == 0) {
                Proceso p = cur.p;
                p.setEstado(EstadoProceso.LISTO);
                tmp[out++] = p;
                if (cur == head) head = cur.sig; else prev.sig = cur.sig;
                if (cur == tail) tail = prev;
                size--;
                cur = (prev == null) ? head : prev.sig;
                continue;
            }
            prev = cur;
            cur = cur.sig;
        }
        Proceso[] res = new Proceso[out];
        for (int i = 0; i < out; i++) res[i] = tmp[i];
        return res;
    }

    public String[] toDisplayStrings() {
        String[] out = new String[size];
        int i = 0;
        for (Nodo t = head; t != null; t = t.sig) {
            Proceso p = t.p;
            out[i++] = "PID " + p.getPid() + " · " + p.getNombre() + " · esperaIO=" + t.ioRestante;
        }
        return out;
    }

    public Object[][] toTableData() {
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
            data[i][6] = t.ioRestante;
            i++;
        }
        return data;
    }
}