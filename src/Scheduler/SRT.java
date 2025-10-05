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

    private Queue readyQueue;

    public SRT() {
        readyQueue = new Queue();
    }

    @Override
    public void addProcess(PCB process) {
    process.setStatus(PCB.Status.READY);
    readyQueue.enqueue(process);
    sortByRemainingTime(); // 🧠 ordena siempre por tiempo restante
    System.out.println("[Scheduler SRT] Proceso " + process.getPid() + " agregado a la cola de listos.");
}

    @Override
    public PCB nextProcess() {
        if (!readyQueue.isEmpty()) {
            sortByRemainingTime(); // Siempre ordenar antes de despachar
            PCB next = (PCB) readyQueue.dispatch();
            System.out.println("[Scheduler SRT] Proceso " + next.getPid() +
                               " seleccionado para ejecución (" + next.getRemainingInstructions() + " restantes).");
            return next;
        }
        return null;
    }
    
    public PCB peekNextProcess() {
    return readyQueue.isEmpty() ? null : (PCB) readyQueue.getHead().getElement();
}

    @Override
    public boolean hasReadyProcess() {
        return !readyQueue.isEmpty();
    }

    /**
     * Ordena la cola según el menor tiempo restante (remainingInstructions).
     * Adaptación del método usado en SPN.
     */
    private void sortByRemainingTime() {
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
