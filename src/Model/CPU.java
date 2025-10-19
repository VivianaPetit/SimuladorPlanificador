


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
import Scheduler.FCFS;
import Scheduler.Scheduler; // (interfaz que luego implementará FCFS, RR, etc.)
import Scheduler.RR;   
import Scheduler.SRT;
import Scheduler.HRRN;
import Scheduler.Feedback;
import Scheduler.SPN;


public class CPU {
    private final Scheduler scheduler;
    private int cycleDurationMs = 1000; // duración de cada ciclo (simulada)
    private int currentTime = 0;
    private Feedback fbScheduler; // referencia al scheduler Feedback
    
    
    // Métricas básicas
    private int totalCycles = 0;
    private int busyCycles = 0;
    
    private Queue readyQueue;
    private Queue processQueue;
    private Queue blockedQueue;
    private Queue runningQueue;
    Semaphore ioSemaphore = new Semaphore(1); // solo un dispositivo de E/S disponible

    

    public CPU(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.readyQueue = new Queue();
        this.processQueue = new Queue();
        this.blockedQueue = new Queue();
        this.runningQueue = new Queue();
    }

    public void ejecutar() {
    System.out.println("[CPU] Iniciando simulación...");
    PCB currentProcess = null;
    int rrQuantumCounter = 0;
    int feedbackQuantumCounter = 0;
    boolean interruptedByIO = false; // para mostrar mensajes inmediatos si I/O termina
    Object arrivalLock = new Object(); // para notificar llegada E/S / nuevos listos

    // referencia a Feedback si aplica
    if (scheduler instanceof Feedback) {
        fbScheduler = (Feedback) scheduler;
    }

    // bucle principal: mientras existan procesos en cualquier cola o haya un proceso en ejecución
    while (!processQueue.isEmpty() || !readyQueue.isEmpty() || !blockedQueue.isEmpty() || currentProcess != null
            || (fbScheduler != null && hasAnyReadyProcess(fbScheduler))) {

        // 1) Revisar llegada de procesos desde processQueue (cada ciclo)
        if (!processQueue.isEmpty()) {
            Queue tempQueue = new Queue();
            while (!processQueue.isEmpty()) {
                PCB p = (PCB) processQueue.dispatch();
                if (p.getStatus() == PCB.Status.NEW) {
                    // agregamos al ready o al scheduler Feedback en su nivel 0
                    if (fbScheduler != null) {
                        fbScheduler.addNewProcess(p);
                        //System.out.println("[Clock " + currentTime + "] Llegada: proceso " + p.getPid() + " -> Feedback nivel 0");
                    } else {
                        addProcess(p);
                        //System.out.println("[Clock " + currentTime + "] Llegada: proceso " + p.getPid() + " -> ready");
                    }
                    // notificar posible preempción / actualización visual
                    synchronized (arrivalLock) { arrivalLock.notifyAll(); }
                } else {
                    tempQueue.enqueue(p);
                }
            }
            processQueue = tempQueue;
        }

        // 2) Si no hay proceso a ejecutar, intentar seleccionar uno
        if (currentProcess == null) {
            // actualizar HRRN si corresponde
            if (scheduler instanceof HRRN) {
                ((HRRN) scheduler).updateTime(currentTime);
            }

            if (fbScheduler != null) {
                // Feedback selection
                currentProcess = fbScheduler.getNextProcess();
                feedbackQuantumCounter = 0;
                if (currentProcess != null) {
                    currentProcess.setStatus(PCB.Status.RUNNING);
                    runningQueue.enqueue(currentProcess);
                    System.out.println("[CPU] Despachando (Feedback) proceso " + currentProcess.getPid());
                }
            } else {
                // Normal scheduler (RR, FCFS, SRT, SPN, HRRN)
                currentProcess = scheduler.nextProcess(readyQueue);
                rrQuantumCounter = 0;
                if (currentProcess != null) {
                    currentProcess.setStatus(PCB.Status.RUNNING);
                    runningQueue.enqueue(currentProcess);
                    System.out.println("[CPU] Despachando proceso " + currentProcess.getPid());
                }
            }
        }

        // 3) Si no hay nada que ejecutar: avanzar tiempo (idle)
        if (currentProcess == null) {
            try {
                Thread.sleep(cycleDurationMs); // pausa corta para no busy-wait
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            currentTime++;
            totalCycles++;
            // no incrementamos busyCycles porque la CPU estuvo idle
            continue;
        }

        // 4) Antes de ejecutar la instrucción: comprobar E/S inminente del proceso actual
        if (!currentProcess.isCpuBound() && currentProcess.getCyclesToException() == currentTime) {
            System.out.println("[Clock " + currentTime + "] Proceso " + currentProcess.getPid() + " genera excepción de E/S -> BLOQUEADO");
            currentProcess.setStatus(PCB.Status.BLOCKED);
            blockedQueue.enqueue(currentProcess);
            currentProcess = null;
            rrQuantumCounter = 0;
            feedbackQuantumCounter = 0;

            // lanzar hilo para simular atención I/O
            final PCB ioProc = (PCB) blockedQueue.dispatch(); // ya lo añadimos arriba; lo sacamos para usarlo aquí
            new Thread(() -> {
                ioSemaphore.acquire();
                try {
                    System.out.println("[I/O Handler] Atendiendo E/S de proceso " + ioProc.getPid() + " (servicio " + ioProc.getExceptionServiceCycles() + " ciclos)");
                    // simular tiempo de servicio de E/S (en ms)
                    Thread.sleep(ioProc.getExceptionServiceCycles() * cycleDurationMs + 50);
                    ioProc.setStatus(PCB.Status.READY);
                    ioProc.setCyclesToException(-1); // evitar nuevas excepciones si ese es el comportamiento deseado

                    // encolar de vuelta (si Feedback, al nivel 0; si no, a readyQueue)
                    if (fbScheduler != null) {
                        synchronized (fbScheduler) {
                            fbScheduler.addNewProcess(ioProc);
                        }
                        System.out.println("[Clock " + currentTime + "] [I/O] E/S completada: proceso " + ioProc.getPid() + " -> Feedback nivel 0");
                    } else {
                        synchronized (readyQueue) {
                            readyQueue.enqueue(ioProc);
                        }
                        System.out.println("[Clock " + currentTime + "] [I/O] E/S completada: proceso " + ioProc.getPid() + " -> ready");
                    }

                    // notificar al loop principal que algo nuevo está listo (posible preempción)
                    synchronized (arrivalLock) { arrivalLock.notifyAll(); }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    ioSemaphore.release();
                }
            }).start();

            // pasar al siguiente ciclo (ya incrementaremos tiempo luego)
            continue;
        }

        // 5) Reducir contador hacia próxima E/S si aplica (se hace *antes* o *después* de ejecutar la instrucción según tu modelo;
        //    aquí lo reducimos sólo *si* se definió >0 y asumimos que la instrucción que vamos a ejecutar consume 1 de ese contador).
        if (currentProcess.getCyclesToException() > 0) {
            // no aplicamos el decremento aún; lo haremos después de ejecutar la instrucción
        }

        // 6) Ejecutar EXACTAMENTE UNA instrucción: pc++; mar++; remainingInstructions--;
        //    --- IMPORTANTE: asumimos que PCB tiene los métodos incrementPC(), incrementMAR(), decrementRemainingInstructions()
        //    Si no existen, reemplázalos por tus getters/setters (ej: pcb.setPc(pcb.getPc()+1) ...).
        try {
            // Simular el tiempo real de ejecución de la instrucción
            Thread.sleep(cycleDurationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        // Actualizar campos del proceso (CPU hace pc++, mar++, remainingInstructions--)
        // --- Si no tienes estos métodos en PCB, sustituir por los getters/setters adecuados.
        try {
            currentProcess.incrementPC();  // pc++
        } catch (NoSuchMethodError | RuntimeException ex) {
            // fallback si no existe incrementPC
            try {
                currentProcess.setPc(currentProcess.getPc() + 1);
            } catch (Exception ignored) {}
        }
        try {
            currentProcess.incrementMAR();     
            // mar++
        } catch (NoSuchMethodError | RuntimeException ex) {
            try {
                currentProcess.setMar(currentProcess.getMar() + 1);
            } catch (Exception ignored) {}
        }
        try {
            currentProcess.decrementRemainingInstructions();
            System.out.println("[Proceso " + currentProcess.getPid() + "] Ejecutando instrucción " +
                        (currentProcess.getTotalInstructions() - currentProcess.getRemainingInstructions()) +
                        " | PC=" + currentProcess.getPc() + " | MAR=" + currentProcess.getMar());// remainingInstructions--
        } catch (NoSuchMethodError | RuntimeException ex) {
            try {
                currentProcess.setRemainingInstructions(currentProcess.getRemainingInstructions() - 1);
            } catch (Exception ignored) {}
        }
        

        // reducir contador hacia próxima E/S (si lo tenías)
        if (currentProcess.getCyclesToException() > 0) {
            currentProcess.setCyclesToException(currentProcess.getCyclesToException() - 1);
        }

        // 7) Mostrar evento si durante la ejecución se encoló un proceso listo (se notifica mediante arrivalLock desde I/O o llegada)
        synchronized (arrivalLock) {
            // intentamos esperar muy corto para permitir que I/O notifique si acabó *exactamente* ahora
            try {
                arrivalLock.wait(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // (si en otro hilo se encoló, ese hilo imprimió su propio mensaje ya)
        
        
        // 8) Comprobar si el proceso terminó
        if (currentProcess.getRemainingInstructions() <= 0) {
            currentProcess.setStatus(PCB.Status.TERMINATED);
            runningQueue.remove(currentProcess);
            readyQueue.remove(currentProcess);
            blockedQueue.remove(currentProcess);
            System.out.println("[Clock " + currentTime + "] [CPU] Proceso " + currentProcess.getPid() + " finalizado.");
            currentProcess = null;
            rrQuantumCounter = 0;
            feedbackQuantumCounter = 0;
            continue;
        }
        
        // actualizar contadores y métricas
        totalCycles++;
        busyCycles++;
        currentTime++;
        

        // 9) Reglas de preempción/quantum según scheduler

        // -- SRT (preemptiva por remaining time)
        if (scheduler instanceof SRT) {
            PCB shortest = ((SRT) scheduler).peekNextProcess(readyQueue);
            if (shortest != null && shortest.getRemainingInstructions() < currentProcess.getRemainingInstructions()) {
                System.out.println("[Scheduler SRT] Preempción: proceso " + currentProcess.getPid() + " reencolado por " + shortest.getPid());
                addProcess(currentProcess); // reencolamos
                currentProcess.setStatus(PCB.Status.READY);
                currentProcess = null;
                rrQuantumCounter = 0;
                continue;
            }
        }

        // -- SPN: normalmente no preemptiva; nada que hacer (si quieres preemptiva conviértela en SRT)
        // -- HRRN: se actualiza antes de selección (no preemptiva por lo general)

        // -- RR: controlar quantum
        if (scheduler instanceof RR) {
            rrQuantumCounter++;
            int quantum = ((RR) scheduler).getQuantum();
            if (rrQuantumCounter >= quantum) {
                System.out.println("[Scheduler RR] Quantum terminado, reencolando proceso " + currentProcess.getPid());
                addProcess(currentProcess);
                runningQueue.remove(currentProcess);
                currentProcess.setStatus(PCB.Status.READY);
                currentProcess = null;
                rrQuantumCounter = 0;
                continue;
            }
        }

        // -- Feedback: controlar quantum del nivel actual
        if (fbScheduler != null && currentProcess != null) {
            feedbackQuantumCounter++;
            int currentLevel = findProcessLevel(fbScheduler, currentProcess);
            int quantumActual = fbScheduler.getQuantums()[Math.max(0, Math.min(currentLevel, fbScheduler.getQuantums().length - 1))];
            if (feedbackQuantumCounter >= quantumActual) {
                System.out.println("[Scheduler Feedback] Quantum terminado para proceso " + currentProcess.getPid() +
                                   " en nivel " + currentLevel + ". Reencolando / degradando nivel.");
                fbScheduler.requeueProcess(currentProcess, currentLevel);
                currentProcess.setStatus(PCB.Status.READY);
                currentProcess = null;
                feedbackQuantumCounter = 0;
                continue;
            }
        }

        // -- SRT/other preemptive policies: también chequeamos si un nuevo proceso listo (por llegada o I/O) debería preemptar.
        //    Para RR la preempción la maneja el quantum. Para FCFS/SPN/HRRN (no preemptivas) NO preemptamos aquí.
        if (!(scheduler instanceof RR) && !(scheduler instanceof FCFS) && !(scheduler instanceof SPN) && !(scheduler instanceof HRRN)
                && !(fbScheduler != null && /* asume feedback usa su propio requeue logic */ false)) {
            // la condición anterior intenta detectar políticas preemptivas (ej SRT). Ya tratamos SRT explícitamente.
        }

        // 10) Si llegamos aquí, continuar con el mismo proceso al siguiente ciclo
        

    } // fin while principal

    System.out.println("[CPU] Simulación finalizada. Todos los procesos terminados o en estado bloqueado sin avanzar.");
    System.out.print("\n Tr \n");
    System.out.print(getCpuUtilization());
    System.out.print("\n Ciclos ociosos de la CPU \n");
    System.out.print(totalCycles - busyCycles);
    System.out.print("\n Ciclos totales \n");
    System.out.print(totalCycles);
}
    
    public double getCpuUtilization() {
        return totalCycles == 0 ? 0 : (double) busyCycles / totalCycles;
    }

    public void setCycleDurationMs(int cycleDurationMs) {
        this.cycleDurationMs = cycleDurationMs;
    }

    // ===== Métodos auxiliares =====
    private boolean hasAnyReadyProcess(Feedback fb) {
        for (int i = 0; i < fb.getQueues().getLenght(); i++) {
            Queue q = fb.getQueues().getElementGeneric(i);
            if (q != null && !q.isEmpty()) return true;
        }
        return false;
    }

    private int findProcessLevel(Feedback fb, PCB p) {
        for (int i = 0; i < fb.getQueues().getLenght(); i++) {
            Queue q = fb.getQueues().getElementGeneric(i);
            if (q.contains(p)) return i;
        }
        return 0; // por defecto nivel 0 si no encuentra
    }

    
    public void addProcess(PCB process) {
        process.setStatus(PCB.Status.READY);
        readyQueue.enqueue(process);
        System.out.println("[CPU Scheduler] Proceso " + process.getPid() + " agregado a la cola de listos.");
    }
    
    public void addProcessQueue(PCB process) {
        process.setStatus(PCB.Status.NEW);
        processQueue.enqueue(process);
}
    
    public long getCurrentTime() {
    return currentTime;
}
        public LinkedList<PCB> obtenerProcesosTotales() {
            LinkedList<PCB> lista = new LinkedList<>();

            agregarDeCola(lista, readyQueue);
            agregarDeCola(lista, runningQueue);
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
