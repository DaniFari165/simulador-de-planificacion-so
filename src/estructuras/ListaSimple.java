/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 *
 * @author Usuario
 */
public class ListaSimple {
    
    private Nodo pFirst; 
    private Nodo pLast; 
    private int size;
    
    public ListaSimple(Nodo pFirst, Nodo pLast, int size) {
        this.pFirst = pFirst;
        this.pLast = pLast;
        this.size = size;
    }
    
    public ListaSimple() {
        this.pFirst = null;
        this.pLast = null;
        this.size = 0;
    }
    
    public boolean EsVacia(){ 
        return (getpFirst() == null);               
    }
    
    public void InsertAtTheEnd(Object x){ 
        Nodo nuevo = new Nodo(x);
        if (this.EsVacia()){ 
            setpFirst(pLast = nuevo); 
        }else { 
            Nodo aux = pLast; 
            aux.setpNext(nuevo);
            pLast = nuevo;    
        }
        size++; 
    }
    
    public void RemoveElement(Object elemento) {
        // Verificar si la lista está vacía
        if (EsVacia()) {
            return;
        }
        
        // Caso especial: si el primer nodo contiene el elemento
        if (pFirst.getInfo().equals(elemento)) {
            pFirst = pFirst.getpNext();
            size--;
            if (pFirst == null) {
                pLast = null; // Si el primer nodo es el único nodo en la lista, pLast también debe ser null
            }
            return;
        }
        
        // Buscar el nodo que contiene el elemento y el nodo anterior
        Nodo prev = pFirst;
        Nodo current = pFirst.getpNext();
        while (current != null) {
            if (current.getInfo().equals(elemento)) {
                prev.setpNext(current.getpNext());
                size--;
                if (current == pLast) {
                    pLast = prev; // Si el nodo eliminado era el último, actualizamos pLast
                }
                return;
            }
            prev = current;
            current = current.getpNext();
        }
        
        // Si no se encontró el elemento, no se hace nada
    }
    
    public void vaciar(){
        this.pFirst = null;
        this.pLast = null;
        this.size = 0;
    }

    public Nodo getpFirst() {
        return pFirst;
    }
     
    public void setpFirst(Nodo pFirst) {
        this.pFirst = pFirst;
    }
    
    public Nodo getpLast() {
        return pLast;
    }
    
    public void setpLast(Nodo pLast) {
        this.pLast = pLast;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
}
