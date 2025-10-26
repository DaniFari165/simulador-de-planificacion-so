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
public class ColaSuspendidos {
    private static final class Nodo {
        Proceso p;
        boolean esSB;
        int ioRestante;
        Nodo sig;
        Nodo(Proceso p, boolean sb, int io) { this.p = p; this.esSB = sb; this.ioRestante = io; }
    }

    private Nodo head, tail;
    private int size;

    public int getSize() { return size; }
    public boolean esVacia() { return size == 0; }

    public void retirarPorPid(int pid) {
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

    private void encolarNodoUnico(Nodo n) {
        retirarPorPid(n.p.getPid());
        if (tail == null) { head = tail = n; } else { tail.sig = n; tail = n; }
        size++;
    }

    public void suspenderListo(Proceso p) {
        if (p == null) return;
        p.setEstado(EstadoProceso.SUSPENDIDO);
        encolarNodoUnico(new Nodo(p, false, 0));
    }

    public void suspenderBloqueado(Proceso p, int ioRestante) {
        if (p == null) return;
        p.setEstado(EstadoProceso.SUSPENDIDO);
        int val = Math.max(1, ioRestante);
        encolarNodoUnico(new Nodo(p, true, val));
    }

    public Proceso reactivarListo() {
        Nodo prev = null, cur = head;
        while (cur != null) {
            if (!cur.esSB) {
                Proceso p = cur.p;
                if (cur == head) head = cur.sig; else prev.sig = cur.sig;
                if (cur == tail) tail = prev;
                size--;
                return p;
            }
            prev = cur;
            cur = cur.sig;
        }
        return null;
    }

    public Proceso[] avanzarUnCicloYReactivarSiCorresponde() {
        int posibles = 0;
        for (Nodo t = head; t != null; t = t.sig) if (t.esSB && t.ioRestante > 0) posibles++;
        Proceso[] tmp = new Proceso[posibles];
        int out = 0;

        Nodo prev = null, cur = head;
        while (cur != null) {
            if (cur.esSB && cur.ioRestante > 0) {
                cur.ioRestante--;
                if (cur.ioRestante == 0) {
                    Proceso p = cur.p;
                    p.setEstado(EstadoProceso.SUSPENDIDO);
                    tmp[out++] = p;
                    if (cur == head) head = cur.sig; else prev.sig = cur.sig;
                    if (cur == tail) tail = prev;
                    size--;
                    cur = (prev == null) ? head : prev.sig;
                    continue;
                }
            }
            prev = cur;
            cur = cur.sig;
        }
        Proceso[] res = new Proceso[out];
        for (int i = 0; i < out; i++) res[i] = tmp[i];
        return res;
    }

    public Object[][] toTableData() {
        Object[][] data = new Object[size][8];
        int i = 0;
        for (Nodo t = head; t != null; t = t.sig) {
            Proceso p = t.p;
            data[i][0] = p.getPid();
            data[i][1] = p.getNombre();
            data[i][2] = p.getTipo().name();
            data[i][3] = t.esSB ? "SB" : "SL";
            data[i][4] = p.getRestantes();
            data[i][5] = p.getTotalInstrucciones();
            data[i][6] = p.getPrioridad();
            data[i][7] = t.esSB ? t.ioRestante : 0;
            i++;
        }
        return data;
    }
}