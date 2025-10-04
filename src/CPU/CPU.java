/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CPU;

/**
 *
 * @author vivia
 */

import Model.PCB;
import Scheduler.Scheduler; // (interfaz que luego implementará FCFS, RR, etc.)

public class CPU implements Runnable {
    private final Scheduler scheduler;
    private boolean running = true;
    private int cycleDurationMs = 200; // configurable

    // Métricas básicas
    private long totalCycles = 0;
    private long busyCycles = 0;

    public CPU(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        System.out.println("[CPU] Iniciando simulación...");

        while (running) {
            PCB process = scheduler.nextProcess(); // el planificador decide

            if (process != null) {
                System.out.println("[CPU] Despachando proceso " + process.getPid());
                process.setStatus(PCB.Status.READY);

                try {
                    // Dispatcher: ejecuta 1 instrucción
                    process.getCanRun().release();
                    process.getDone().acquire();

                    busyCycles++;
                    totalCycles++;

                    // Verificar si terminó
                    if (process.getRemainingInstructions() == 0) {
                        process.setStatus(PCB.Status.TERMINATED);
                        System.out.println("[CPU] Proceso " + process.getPid() + " finalizado.");
                    } else {
                        process.setStatus(PCB.Status.READY);
                        scheduler.addProcess(process); // vuelve a la cola
                    }

                    Thread.sleep(cycleDurationMs / 2); // pausa pequeña entre procesos
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            } else {
                // No hay procesos listos
                totalCycles++;
                try {
                    Thread.sleep(cycleDurationMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void stopCPU() {
        running = false;
    }

    public double getCpuUtilization() {
        return totalCycles == 0 ? 0 : (double) busyCycles / totalCycles;
    }

    public void setCycleDurationMs(int cycleDurationMs) {
        this.cycleDurationMs = cycleDurationMs;
    }
}

