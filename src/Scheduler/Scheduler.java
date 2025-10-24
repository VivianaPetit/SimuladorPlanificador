/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Scheduler;

import DataStruct.Queue;
import Model.Process;

/**
 *
 * @author vivia
 */

public interface Scheduler {
    // Devuelve el siguiente proceso a ejecutar según la política
    Process nextProcess(Queue readyQueue);

    // Indica si hay procesos en la cola de Listo
    boolean hasReadyProcess(Queue readyQueue);
}

