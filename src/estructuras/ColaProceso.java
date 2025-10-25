/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

import java.util.concurrent.locks.ReentrantLock;
import modelo.Proceso;

/**
 *
 * @author 58412
 */
public class ColaProceso {
    private static final class Nodo {
        Proceso p;
        Nodo sig;
        Nodo(Proceso p) { this.p = p; }
    }

    private Nodo head, tail;
    private int size;
    private final ReentrantLock lock = new ReentrantLock();

    public void encolar(Proceso p) {
        if (p == null) return;
        lock.lock();
        try {
            Nodo n = new Nodo(p);
            if (tail == null) { head = tail = n; } else { tail.sig = n; tail = n; }
            size++;
        } finally { lock.unlock(); }
    }

    public Proceso desencolar() {
        lock.lock();
        try {
            if (head == null) return null;
            Proceso p = head.p;
            head = head.sig;
            if (head == null) tail = null;
            size--;
            return p;
        } finally { lock.unlock(); }
    }

    public boolean estaVacia() {
        lock.lock();
        try { return size == 0; } finally { lock.unlock(); }
    }

    public int size() {
        lock.lock();
        try { return size; } finally { lock.unlock(); }
    }

    public Proceso[] toArray() {
        lock.lock();
        try {
            Proceso[] arr = new Proceso[size];
            int i = 0;
            for (Nodo t = head; t != null; t = t.sig) arr[i++] = t.p;
            return arr;
        } finally { lock.unlock(); }
    }

    public Proceso retirarMinPorTotal() {
        lock.lock();
        try {
            if (head == null) return null;
            Nodo prevMin = null, prev = null, cur = head;
            Proceso min = head.p;
            Nodo minNodo = head;
            while (cur != null) {
                if (cur.p.getTotalInstrucciones() < min.getTotalInstrucciones()) {
                    min = cur.p;
                    minNodo = cur;
                    prevMin = prev;
                }
                prev = cur;
                cur = cur.sig;
            }
            if (prevMin == null) {
                head = head.sig;
                if (head == null) tail = null;
            } else {
                prevMin.sig = minNodo.sig;
                if (minNodo == tail) tail = prevMin;
            }
            size--;
            return min;
        } finally { lock.unlock(); }
    }

    public Proceso retirarMinPorRestantes() {
        lock.lock();
        try {
            if (head == null) return null;
            Nodo prevMin = null, prev = null, cur = head;
            Proceso min = head.p;
            Nodo minNodo = head;
            while (cur != null) {
                if (cur.p.getRestantes() < min.getRestantes()) {
                    min = cur.p;
                    minNodo = cur;
                    prevMin = prev;
                }
                prev = cur;
                cur = cur.sig;
            }
            if (prevMin == null) {
                head = head.sig;
                if (head == null) tail = null;
            } else {
                prevMin.sig = minNodo.sig;
                if (minNodo == tail) tail = prevMin;
            }
            size--;
            return min;
        } finally { lock.unlock(); }
    }

    public Proceso retirarMinPorPrioridad() {
        lock.lock();
        try {
            if (head == null) return null;
            Nodo prevMin = null, prev = null, cur = head;
            Proceso min = head.p;
            Nodo minNodo = head;
            while (cur != null) {
                if (cur.p.getPrioridad() < min.getPrioridad()) {
                    min = cur.p;
                    minNodo = cur;
                    prevMin = prev;
                }
                prev = cur;
                cur = cur.sig;
            }
            if (prevMin == null) {
                head = head.sig;
                if (head == null) tail = null;
            } else {
                prevMin.sig = minNodo.sig;
                if (minNodo == tail) tail = prevMin;
            }
            size--;
            return min;
        } finally { lock.unlock(); }
    }
}