/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Scheduler;

import Model.PCB;

/**
 *
 * @author vivia
 */

public interface Scheduler {
    // Devuelve el siguiente proceso a ejecutar según la política
    PCB nextProcess();

    // Agrega un proceso a la cola de Ready
    void addProcess(PCB p);
    
    // Indica si hay procesos en la cola de Listo
    boolean hasReadyProcess();
}

