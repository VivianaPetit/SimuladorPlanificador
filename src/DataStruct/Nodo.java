/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataStruct;

/**
 *
 * @author vivia
 */
public class Nodo { // Atributos siempre privados..
    private Object info;
    private Nodo next;
    
    public Nodo (Object info) {
        this.info = info;
        this.next = null;
    }

    public Object getInfo() {
        return info;
    }

    public Nodo getNext() {
        return next;
    }

    public void setInfo(Object info) {
        this.info = info;
    }

    public void setNext(Nodo next) {
        this.next = next;
    }
}
