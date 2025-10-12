


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author vivia
 */

import DataStruct.LinkedList;
import DataStruct.Nodo;
import DataStruct.Queue;
import Model.PCB;
import Scheduler.Scheduler; // (interfaz que luego implementará FCFS, RR, etc.)
import Scheduler.RR;   
import Scheduler.SRT;
import Scheduler.HRRN;


public class CPU {
    private final Scheduler scheduler;
    private int cycleDurationMs = 200; // duración de cada ciclo (simulada)
    private long currentTime = 0;
    
    // Métricas básicas
    private long totalCycles = 0;
    private long busyCycles = 0;
    
    private Queue readyQueue;
    private Queue processQueue;
    private Queue blockedQueue;
    Semaphore ioSemaphore = new Semaphore(1); // solo un dispositivo de E/S disponible

    

    public CPU(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.readyQueue = new Queue();
        this.processQueue = new Queue();
        this.blockedQueue = new Queue();
    }

    
    public void ejecutar() {
    System.out.println("[CPU] Iniciando simulación (modo expulsivo si aplica)...");
    int idleCycles = 0;
    PCB currentProcess = null;
    int rrQuantumCounter = 0;

    while (!readyQueue.isEmpty() || !processQueue.isEmpty() || currentProcess != null) {

        // ====== Limpiar procesos terminados de readyQueue ======
        Queue tempReady = new Queue();
        while (!readyQueue.isEmpty()) {
            PCB p = (PCB) readyQueue.dispatch();
            if (p.getStatus() != PCB.Status.TERMINATED) {
                tempReady.enqueue(p);
            }
        }
        readyQueue = tempReady;

        // ====== Revisar llegada de procesos ======
        if (!processQueue.isEmpty()) {
            Queue tempQueue = new Queue();
            while (!processQueue.isEmpty()) {
                PCB p = (PCB) processQueue.dispatch();
                if (p.getArrivalTime() <= currentTime && p.getStatus() == PCB.Status.NEW) {
                    addProcess(p);
                } else {
                    tempQueue.enqueue(p);
                }
            }
            processQueue = tempQueue;
        }

        // ====== Si no hay procesos listos ni en ejecución ======
        if (readyQueue.isEmpty() && currentProcess == null) {
            try {
                Thread.sleep(50);
                currentTime++;
                idleCycles++;
                continue;
            } catch (InterruptedException e) {
                break;
            }
        }
        idleCycles = 0;

        // ====== Selección del proceso ======
        if (currentProcess == null) {
            currentProcess = scheduler.nextProcess(readyQueue); 
            if (currentProcess != null) {
                currentProcess.setStatus(PCB.Status.RUNNING);
                rrQuantumCounter = 0;
                System.out.println("[CPU] Despachando proceso " + currentProcess.getPid());
                currentProcess.start(); // iniciar hilo si no ha iniciado
            }
        }

        if (currentProcess == null) continue;

        
        // ====== Manejo de excepción de E/S ======
        if (!currentProcess.isCpuBound() &&
            currentProcess.getCyclesToException() == 0) {
            
            System.out.println("[CPU] Proceso " + currentProcess.getPid() + " genera excepción de E/S.");

            currentProcess.setStatus(PCB.Status.BLOCKED);
            blockedQueue.enqueue(currentProcess);
            readyQueue.remove(currentProcess);
            PCB ioProcess = currentProcess; // guardamos referencia

            // Lanza hilo que simula la atención de E/S
            new Thread(() -> {
                ioSemaphore.acquire(); // Bloquea el acceso al dispositivo de E/S
                
                try {
                    System.out.println("[I/O Handler] Atendiendo E/S del proceso " + ioProcess.getPid());
                    Thread.sleep(ioProcess.getExceptionServiceCycles() * cycleDurationMs + 600);
                    ioProcess.setStatus(PCB.Status.READY);
                    ioProcess.setCyclesToException(-1); // No generará más excepciones
                    
                    System.out.println("[I/O Handler] E/S completada para proceso " + ioProcess.getPid());
                    synchronized (readyQueue) {
                        readyQueue.enqueue(ioProcess);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    ioSemaphore.release(); // Libera el dispositivo
                }
            }).start();

            // Libera la CPU inmediatamente
            currentProcess = null;
            rrQuantumCounter = 0;
            continue;
        }
        
        if (currentProcess.getCyclesToException() > 0) {
            currentProcess.setCyclesToException(currentProcess.getCyclesToException() - 1);
        }


        // ====== Ejecutar 1 instrucción ======
        try {
            currentProcess.getCanRun().release();
            currentProcess.getDone().acquire();
            currentTime++;
            rrQuantumCounter++;
            Thread.sleep(cycleDurationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ====== Comprobar si terminó ======
        if (currentProcess.getRemainingInstructions() <= 0) {
            currentProcess.setStatus(PCB.Status.TERMINATED);
            System.out.println("[CPU] Proceso " + currentProcess.getPid() + " finalizado.");
            currentProcess = null;
            rrQuantumCounter = 0;
            continue;
        }

        // ====== Preempción SRT ======
        if (scheduler instanceof SRT) {
            PCB shortest = ((SRT) scheduler).peekNextProcess(readyQueue);
            if (shortest != null && shortest.getRemainingInstructions() < currentProcess.getRemainingInstructions()) {
                System.out.println("[Scheduler SRT] Preemption: proceso " + currentProcess.getPid() + " reencolado.");
                addProcess(currentProcess); 
                currentProcess = null;
            }
        }

        // ====== Quantum RR ======
        if (scheduler instanceof RR) {
            int quantum = ((RR) scheduler).getQuantum();
            if (rrQuantumCounter >= quantum) {
                System.out.println("[Scheduler RR] Quantum terminado, reencolando proceso " + currentProcess.getPid());
                addProcess(currentProcess);
                currentProcess = null;
                rrQuantumCounter = 0;
            }
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
    
        public void ejecutarSecuencial() {
    System.out.println("[CPU] Iniciando simulación (modo no expulsivo)...");

    while (!readyQueue.isEmpty() || !processQueue.isEmpty()) {

        // ====== Revisar llegada de procesos ======
        if (!processQueue.isEmpty()) {
            Queue tempQueue = new Queue();
            while (!processQueue.isEmpty()) {
                PCB p = (PCB) processQueue.dispatch();
                if (p.getArrivalTime() <= currentTime && p.getStatus() == PCB.Status.NEW) {
                   
                    addProcess(p);
                } else {
                    tempQueue.enqueue(p);
                }
            }
            processQueue = tempQueue;
        }

        // ====== Si no hay procesos listos, esperar y avanzar tiempo ======
        if (readyQueue.isEmpty()) {
            try {
                Thread.sleep(50);
                currentTime++;
                continue;
            } catch (InterruptedException e) {
                break;
            }
        }

        // ====== Actualizar tiempo del scheduler ======
        if (scheduler instanceof HRRN) {
            ((HRRN) scheduler).updateTime(currentTime);
        }

        // ====== Selección del proceso ======
        PCB proceso = scheduler.nextProcess(readyQueue);
        if (proceso == null) continue;

        proceso.setStatus(PCB.Status.RUNNING);
        System.out.println("[CPU] Despachando proceso " + proceso.getPid());

        while (proceso.getRemainingInstructions() > 0 && proceso.getStatus() != PCB.Status.BLOCKED) {
            try {
                proceso.getCanRun().release();
                proceso.getDone().acquire();
                totalCycles++;
                busyCycles++;
                Thread.sleep(cycleDurationMs);
                currentTime++;
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
    
    public void addProcessQueue(PCB process) {
    process.setStatus(PCB.Status.NEW);
    
    // Si la cola está vacía, simplemente encolamos
    if (processQueue.isEmpty()) {
        processQueue.enqueue(process);
        System.out.println("[CPU Scheduler] Proceso " + process.getPid() + " agregado a la cola de procesos.");
        return;
    }

    // Creamos una cola temporal para reconstruir la cola en orden
    Queue tempQueue = new Queue();
    boolean inserted = false;

    while (!processQueue.isEmpty()) {
        PCB p = (PCB) processQueue.dispatch();
        // Insertamos el nuevo proceso antes de cualquier proceso con arrivalTime mayor
        if (!inserted && process.getArrivalTime() < p.getArrivalTime()) {
            tempQueue.enqueue(process);
            inserted = true;
        }
        tempQueue.enqueue(p);
    }

    // Si no se insertó, es el último
    if (!inserted) {
        tempQueue.enqueue(process);
    }

    // Reemplazamos la cola original con la ordenada
    processQueue = tempQueue;

    System.out.println("[CPU Scheduler] Proceso " + process.getPid() + " agregado a la cola de procesos.");
}
    
    public long getCurrentTime() {
    return currentTime;
}
        public LinkedList<PCB> obtenerProcesosTotales() {
            LinkedList<PCB> lista = new LinkedList<>();

            agregarDeCola(lista, readyQueue);
            agregarDeCola(lista, blockedQueue);
            agregarDeCola(lista, processQueue);

            return lista;
        }

        private void agregarDeCola(LinkedList<PCB> lista, Queue cola) {
            Nodo actual = cola.getHead();
            while (actual != null) {
                Object elemento = actual.getElement();
                if (elemento instanceof PCB pcb) {
                    lista.insertFinal(pcb);
                }
                actual = actual.getNext();
            }
        }

}
