/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Scheduler;

import DataStruct.Queue;
import DataStruct.Nodo;
import Model.PCB;

/**
 *
 * @author vivia
 */
public class SPN implements Scheduler {

    private Queue readyQueue;

    public SPN() {
        readyQueue = new Queue();
    }

    @Override
    public void addProcess(PCB process) {
        process.setStatus(PCB.Status.READY);
        readyQueue.enqueue(process);
        System.out.println("[Scheduler SPN] Proceso " + process.getPid() + " agregado a la cola de listos.");
    }

    @Override
    public PCB nextProcess() {
        if (!readyQueue.isEmpty()) {
            sortByInstructions(); // Ordenar antes de despachar
            PCB next = (PCB) readyQueue.dispatch(); 
            System.out.println("[Scheduler SPN] Proceso " + next.getPid() + " seleccionado para ejecución.");
            return next;
        }
        return null;
    }

    @Override
    public boolean hasReadyProcess() {
        return !readyQueue.isEmpty();
    }

    // Método privado para ordenar la cola por menor número de instrucciones
    private void sortByInstructions() {
        if (readyQueue.isEmpty() || readyQueue.getHead().getNext() == null) {
            return; // Cola vacía o con un solo proceso
        }

        Nodo current = readyQueue.getHead().getNext();
        Nodo prevCurrent = readyQueue.getHead();

        while (current != null) {
            Nodo compare = readyQueue.getHead();
            Nodo prevCompare = null;

            while (compare != current) {
                PCB pcbCurrent = (PCB) current.getElement();
                PCB pcbCompare = (PCB) compare.getElement();

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



