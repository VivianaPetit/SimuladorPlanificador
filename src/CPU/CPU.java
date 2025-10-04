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
import Scheduler.RR;    


public class CPU {
    private final Scheduler scheduler;
    private int cycleDurationMs = 200; // duración de cada ciclo (simulada)
    
    // Métricas básicas
    private long totalCycles = 0;
    private long busyCycles = 0;

    public CPU(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    // Método principal de ejecución (simulación lineal)
//    public void ejecutar() {
//        System.out.println("[CPU] Iniciando simulación...");
//
//        while (scheduler.hasReadyProcess()) {
//            PCB proceso = scheduler.nextProcess(); // el planificador decide quién va
//            System.out.println("[CPU] Despachando proceso " + proceso.getPid());
//            proceso.setStatus(PCB.Status.RUNNING);
//
//            // Mientras queden instrucciones por ejecutar
//            while (proceso.getRemainingInstructions() > 0) {
//                try {
//                    // Da permiso para ejecutar una instrucción
//                    proceso.getCanRun().release();
//                    // Espera a que el proceso confirme que terminó esa instrucción
//                    proceso.getDone().acquire();
//
//                    // Simula avance del reloj
//                    totalCycles++;
//                    busyCycles++;
//                    Thread.sleep(cycleDurationMs);
//
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//
//
//            // Proceso finaliza
//            proceso.setStatus(PCB.Status.TERMINATED);
//            System.out.println("[CPU] Proceso " + proceso.getPid() + " finalizado.");
//        }
//
//        System.out.println("[CPU] Todos los procesos terminados.");
//    }
    
    public void ejecutar() {
    System.out.println("[CPU] Iniciando simulación...");

    while (scheduler.hasReadyProcess()) {
        PCB proceso = scheduler.nextProcess();
        System.out.println("[CPU] Despachando proceso " + proceso.getPid());
        proceso.setStatus(PCB.Status.RUNNING);

        int quantum = (scheduler instanceof RR) ? ((RR) scheduler).getQuantum() : Integer.MAX_VALUE;

        int instruccionesEjecutadas = 0;

        while (proceso.getRemainingInstructions() > 0 && instruccionesEjecutadas < quantum) {
            try {
                proceso.getCanRun().release();
                proceso.getDone().acquire();

                totalCycles++;
                busyCycles++;
                instruccionesEjecutadas++;
                Thread.sleep(cycleDurationMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (scheduler instanceof RR) {
            ((RR) scheduler).requeueIfNeeded(proceso);
        } else {
            proceso.setStatus(PCB.Status.TERMINATED);
            System.out.println("[CPU] Proceso " + proceso.getPid() + " finalizado.");
        }
    }

    System.out.println("[CPU] Todos los procesos terminados.");
}

    public double getCpuUtilization() {
        return totalCycles == 0 ? 0 : (double) busyCycles / totalCycles;
    }

    public void setCycleDurationMs(int cycleDurationMs) {
        this.cycleDurationMs = cycleDurationMs;
    }
}


