/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataStruct;

/**
 *
 * @author vivia
 */
public class LinkedList {
    private Nodo pLast;
    private Nodo pFirst;
    private int tamano;

    public LinkedList() {
        this.pLast = null;
        this.pFirst = null;
        this.tamano = 0;
    } 

    public Nodo getpLast() {
        return pLast;
    }

    public Nodo getpFirst() {
        return pFirst;
    }

    public int getTamano() {
        return tamano;
    }

    public void setpLast(Nodo pLast) {
        this.pLast = pLast;
    }

    public void setpFirst(Nodo pFirst) {
        this.pFirst = pFirst;   
    }
    
    public void setTamano(int tamano) {
        this.tamano = tamano;
    }
    
    public boolean EsVacio() {
        return pFirst == null;
    }

}
