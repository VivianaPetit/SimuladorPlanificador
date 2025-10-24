/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Scheduler;

import Model.Process;
import DataStruct.Queue;  // 

/**
 * Implementación del planificador FCFS (First Come, First Served)
 * Los procesos se ejecutan en el orden en que llegan (sin interrupciones).
 */
public class FCFS implements Scheduler {

    @Override
    public Process nextProcess(Queue readyQueue) {
        if (!readyQueue.isEmpty()) {
            Process next = (Process) readyQueue.dispatch(); // extrae el primero en la cola
            System.out.println("[Scheduler FCFS] Proceso " + next.getPid() + " seleccionado para ejecución.");
            return next;
        }
        return null;
    }

    @Override
    public boolean hasReadyProcess(Queue readyQueue) {
        return !readyQueue.isEmpty();
    }
}

