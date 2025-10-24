/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Scheduler;

import DataStruct.Queue;
import DataStruct.Nodo;
import Model.Process;

/**
 *
 * @author vivia
 */
public class SPN implements Scheduler {


//    @Override
//    public Process nextProcess(Queue readyQueue) {
//        if (!readyQueue.isEmpty()) {
//            sortByInstructions(readyQueue); // Ordenar antes de despachar
//            Process next = (Process) readyQueue.dispatch(); 
//            System.out.println("[Scheduler SPN] Proceso " + next.getPid() + " seleccionado para ejecución.");
//            return next;
//        }
//        return null;
//    }
    
    @Override
public Process nextProcess(Queue readyQueue) {
    if (readyQueue.isEmpty()) return null;

    Process shortest = null;
    Nodo actual = readyQueue.getHead();

    // Buscar el proceso con menor cantidad de instrucciones totales
    while (actual != null) {
        Process p = (Process) actual.getElement();
        if (shortest == null || p.getTotalInstructions() < shortest.getTotalInstructions()) {
            shortest = p;
        }
        actual = actual.getNext();
    }

    // Eliminar ese proceso de la cola (sin alterar la estructura interna)
    readyQueue.remove(shortest);

    System.out.println("[Scheduler SPN] Proceso " + shortest.getPid() +
                       " seleccionado para ejecución (" +
                       shortest.getTotalInstructions() + " instrucciones totales).");

    return shortest;
}

    @Override
    public boolean hasReadyProcess(Queue readyQueue) {
        return !readyQueue.isEmpty();
    }

    // Método privado para ordenar la cola por menor número de instrucciones
    private void sortByInstructions(Queue readyQueue) {
        if (readyQueue.isEmpty() || readyQueue.getHead().getNext() == null) {
            return; // Cola vacía o con un solo proceso
        }

        Nodo current = readyQueue.getHead().getNext();
        Nodo prevCurrent = readyQueue.getHead();

        while (current != null) {
            Nodo compare = readyQueue.getHead();
            Nodo prevCompare = null;

            while (compare != current) {
                Process pcbCurrent = (Process) current.getElement();
                Process pcbCompare = (Process) compare.getElement();

                if (pcbCurrent.getTotalInstructions() < pcbCompare.getTotalInstructions()) {
                    // Remover current de su posición
                    prevCurrent.setNext(current.getNext());

                    // Insertar current antes de compare
                    if (prevCompare == null) {
                        // Insertar al inicio
                        current.setNext(readyQueue.getHead());
                        readyQueue.setHead(current);
                    } else {
                        prevCompare.setNext(current);
                        current.setNext(compare);
                    }

                    // Ajustar current
                    current = prevCurrent.getNext();
                    break;
                }

                prevCompare = compare;
                compare = compare.getNext();
            }

            if (compare == current) {
                prevCurrent = current;
                current = current.getNext();
            }
        }
    }
}



