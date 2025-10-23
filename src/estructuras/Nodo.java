/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 *
 * @author 58412
 */
public class Nodo {
    
    private Nodo pNext; 
    private Object info;
    
    public Nodo(Nodo pNext, Object info){
        this.pNext = pNext;
        this.info = info; 
    }
    
    public Nodo(Object info){
        this.info = info;
    }
    
    public Nodo getpNext(){
        return pNext;
    }
    
    public void setpNext(Nodo pNext){
        this.pNext = pNext;
        
    }
    
    public Object getInfo(){
        return info; 
    }
    
    public void setInfo(Object info){
        this.info = info;
    }
    
}
