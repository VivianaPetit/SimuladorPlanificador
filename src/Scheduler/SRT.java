/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Scheduler;

import DataStruct.Queue;
import DataStruct.Nodo;
import Model.PCB;

/**
 * Implementación del planificador SRT (Shortest Remaining Time)
 * Versión expulsiva de SPN: siempre selecciona el proceso con el menor
 * tiempo restante de ejecución. Si llega un proceso más corto, se preempe.
 * 
 * @author Jose
 */
public class SRT implements Scheduler {


    @Override
public PCB nextProcess(Queue readyQueue) {
    if (!readyQueue.isEmpty()) {
        Nodo current = readyQueue.getHead();
        PCB minPCB = (PCB) current.getElement();

        // Buscar el proceso con menor tiempo restante
        while (current != null) {
            PCB pcb = (PCB) current.getElement();
            if (pcb.getRemainingInstructions() < minPCB.getRemainingInstructions()) {
                minPCB = pcb;
            }
            current = current.getNext();
        }

        System.out.println("[Scheduler SRT] Proceso " + minPCB.getPid() +
                           " seleccionado para ejecución (" + minPCB.getRemainingInstructions() + " restantes).");
        readyQueue.remove(minPCB);
        return minPCB; // NO hacer dispatch aquí, solo mirar
    }
    return null;
}

public PCB peekNextProcess(Queue readyQueue) {
    if (readyQueue.isEmpty()) return null;

    Nodo current = readyQueue.getHead();
    PCB shortest = null;

    while (current != null) {
        PCB pcb = (PCB) current.getElement();
        if (pcb.getRemainingInstructions() > 0) { // ignorar procesos terminados
            if (shortest == null || pcb.getRemainingInstructions() < shortest.getRemainingInstructions()) {
                shortest = pcb;
            }
        }
        current = current.getNext();
    }
    return shortest;
}

    @Override
    public boolean hasReadyProcess(Queue readyQueue) {
        return !readyQueue.isEmpty();
    }

    /**
     * Ordena la cola según el menor tiempo restante (remainingInstructions).
     * Adaptación del método usado en SPN.
     */
    private void sortByRemainingTime(Queue readyQueue) {
        if (readyQueue.isEmpty() || readyQueue.getHead().getNext() == null) {
            return; // Cola vacía o con un solo elemento
        }

        Nodo current = readyQueue.getHead().getNext();
        Nodo prevCurrent = readyQueue.getHead();

        while (current != null) {
            Nodo compare = readyQueue.getHead();
            Nodo prevCompare = null;

            while (compare != current) {
                PCB pcbCurrent = (PCB) current.getElement();
                PCB pcbCompare = (PCB) compare.getElement();

                if (pcbCurrent.getRemainingInstructions() < pcbCompare.getRemainingInstructions()) {
                    // Quitar 'current' de su posición actual
                    prevCurrent.setNext(current.getNext());

                    // Insertar 'current' antes de 'compare'
                    if (prevCompare == null) {
                        // Insertar al inicio
                        current.setNext(readyQueue.getHead());
                        readyQueue.setHead(current);
                    } else {
                        prevCompare.setNext(current);
                        current.setNext(compare);
                    }

                    // Reposicionar
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
