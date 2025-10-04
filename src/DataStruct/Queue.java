/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataStruct;

/**
 *
 * @author vivia
 */
public class Queue {
    private Nodo first;  
    private Nodo last;  

    public Queue() {
        this.first = null;
        this.last = null;
    }

    // Check if the queue is empty
    public boolean isEmpty() {
        return first == null;
    }

    // Enqueue (add an element at the end)
    public void enqueue(Object info) {
        Nodo newNode = new Nodo(info);
        if (isEmpty()) {
            first = newNode;
            last = newNode;
        } else {
            last.setNext(newNode);
            last = newNode;
        }
    }

    // Dequeue (remove the first element)
    public Object dequeue() {
        if (isEmpty()) {
            throw new RuntimeException("Queue is empty");
        }
        Object data = first.getInfo();
        first = first.getNext();
        if (first == null) { // If the queue became empty
            last = null;
        }
        return data;
    }

    // Peek (see the first element without removing it)
    public Object peek() {
        if (isEmpty()) {
            throw new RuntimeException("Queue is empty");
        }
        return first.getInfo();
    }
}

