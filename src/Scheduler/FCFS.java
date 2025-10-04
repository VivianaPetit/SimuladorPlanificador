/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Scheduler;

import Model.PCB;
import DataStruct.Queue;  // 

/**
 * Implementación del planificador FCFS (First Come, First Served)
 * Los procesos se ejecutan en el orden en que llegan (sin interrupciones).
 */
public class FCFS implements Scheduler {

    private Queue readyQueue; // Usamos cola personalizada

    public FCFS() {
        readyQueue = new Queue();
    }

    @Override
    public void addProcess(PCB process) {
        process.setStatus(PCB.Status.READY);
        readyQueue.enqueue(process);
        System.out.println("[Scheduler FCFS] Proceso " + process.getPid() + " agregado a la cola de listos.");
    }

    @Override
    public PCB nextProcess() {
        if (!readyQueue.isEmpty()) {
            PCB next = (PCB) readyQueue.dispatch(); // extrae el primero en la cola
            System.out.println("[Scheduler FCFS] Proceso " + next.getPid() + " seleccionado para ejecución.");
            return next;
        }
        return null;
    }

    @Override
    public boolean hasReadyProcess() {
        return !readyQueue.isEmpty();
    }
}

