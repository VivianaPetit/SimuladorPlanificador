/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataStruct;


public class Queue {
    private Nodo head, tail;
    private int size;
    
    public Queue() {
        this.head = this.tail = null;
        size = 0;
    }

    public Nodo getHead() {
        return head;
    }

    public void setHead(Nodo head) {
        this.head = head;
    }

    public Nodo getTail() {
        return tail;
    }

    public void setTail(Nodo tail) {
        this.tail = tail;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    
    public void enqueue(Object element) {
        Nodo nodo = new Nodo(element);
        if (isEmpty()) {
            setHead(nodo);
            setTail(nodo);
        } else {
            getTail().setNext(nodo);
            setTail(nodo);
        }
        size++;
    }

    public void dequeue() {
        if (isEmpty()) {
            System.out.println("La lista esta vacia");
        } else {
            Nodo pointer = getHead();
            setHead(pointer.getNext());
            pointer.setNext(null);
            size--;
        }
    }
    
    public Object dispatch() {
    if (isEmpty()) {
        return null; // evita NullPointerException
    }
    Nodo nodo = head;
    head = head.getNext();
    if (head == null) {
        tail = null;
    }
    return nodo.getElement();
}

    public boolean isEmpty() {
        return getHead() == null && getTail() == null;
    }
    
    public void print() {
        Nodo pointer = getHead();
        while (pointer != null) {
            System.out.println("[ "+pointer.getElement() + " ]");
            pointer = pointer.getNext();
        }
    }
    
}
