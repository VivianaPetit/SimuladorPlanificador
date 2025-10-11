/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author vivia
 */

import DataStruct.Queue;
import Model.PCB;
import Scheduler.Scheduler; // (interfaz que luego implementará FCFS, RR, etc.)
import Scheduler.RR;   
import Scheduler.SRT;


public class CPU {
    private final Scheduler scheduler;
    private int cycleDurationMs = 200; // duración de cada ciclo (simulada)
    
    // Métricas básicas
    private long totalCycles = 0;
    private long busyCycles = 0;
    
    private Queue readyQueue;

    public CPU(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.readyQueue = new Queue();
    }

    
    public void ejecutar() {
    System.out.println("[CPU] Iniciando simulación...");

    while (true) {
        if (!scheduler.hasReadyProcess(readyQueue)) {
            try {
                Thread.sleep(50); // Espera a que lleguen procesos
                continue;
            } catch (InterruptedException e) {
                break;
            }
        }

        PCB proceso = scheduler.nextProcess(readyQueue);
        if (proceso == null) continue;

        System.out.println("[CPU] Despachando proceso " + proceso.getPid());
        proceso.setStatus(PCB.Status.RUNNING);

        int quantum = (scheduler instanceof RR) ? ((RR) scheduler).getQuantum() : Integer.MAX_VALUE;
        int instruccionesEjecutadas = 0;

        while (proceso.getRemainingInstructions() > 0 && instruccionesEjecutadas < quantum) {
            try {
                proceso.getCanRun().release();
                proceso.getDone().acquire();

                instruccionesEjecutadas++;
                Thread.sleep(cycleDurationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (scheduler instanceof RR) {
            ((RR) scheduler).requeueIfNeeded(proceso, readyQueue);
        } else {
            proceso.setStatus(PCB.Status.TERMINATED);
            System.out.println("[CPU] Proceso " + proceso.getPid() + " finalizado.");
        }
    }
}

    public double getCpuUtilization() {
        return totalCycles == 0 ? 0 : (double) busyCycles / totalCycles;
    }

    public void setCycleDurationMs(int cycleDurationMs) {
        this.cycleDurationMs = cycleDurationMs;
    }
    
    public void ejecutarSecuencial() {
    System.out.println("[CPU] Iniciando simulación (modo no expulsivo)...");

    while (scheduler.hasReadyProcess(readyQueue)) {
        PCB proceso = scheduler.nextProcess(readyQueue);
        if (proceso == null) continue;

        proceso.setStatus(PCB.Status.RUNNING);
        System.out.println("[CPU] Despachando proceso " + proceso.getPid());

        while (proceso.getRemainingInstructions() > 0) {
            try {
                proceso.getCanRun().release();
                proceso.getDone().acquire();
                totalCycles++;
                busyCycles++;
                Thread.sleep(cycleDurationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        proceso.setStatus(PCB.Status.TERMINATED);
        System.out.println("[CPU] Proceso " + proceso.getPid() + " finalizado.");
    }
    
    System.out.println("[CPU] Todos los procesos terminados.");
}
    
    public void addProcess(PCB process) {
        process.setStatus(PCB.Status.READY);
        readyQueue.enqueue(process);
        System.out.println("[CPU Scheduler] Proceso " + process.getPid() + " agregado a la cola de listos.");
    }

}

