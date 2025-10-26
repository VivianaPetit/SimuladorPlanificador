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
import java.util.function.Consumer;

public class CPU {
    private Scheduler scheduler;
    public static int cycleDurationMs = 1000; // duración de cada ciclo (simulada) por default: 500
    public static int currentTime = 0;
    private Feedback fbScheduler;
    
    private Consumer<String> logListener;
    
    // Métricas básicas
    private int totalCycles = 0;
    private int busyCycles = 0;

    private Queue readyQueue;
    private Queue processQueue;
    private Queue blockedQueue;
    private Queue blockedQueueAux;
    private Queue runningQueue;
    private Queue finishedQueue;
    private Queue suspendedReadyQueue; 
    private Queue suspendedBlockedQueue; 
    Semaphore ioSemaphore = new Semaphore(1); // para exclusion mutua
    private Process procesoActual; // nuevo
    private Process currentProcess;  

    // Para el SO: 
    private int pc;
    private int mar;
    private boolean soEjecutando;
    private String status;
    private String tipo;
    private int memoryNeededSO;
    
    private final Object soLock = new Object();                // protege ejecución SO
    private volatile boolean soRequested = false;              // petición de ejecutar SO
    private volatile String soRequestedReason = null;          // motivo de la petición
    private final Object arrivalLock = new Object();           // ya lo tenías, centralizo su uso
    
    // Manejo de memoria 
    int totalMemory = 1024; // en MB
    int usedMemory = 0;

    public CPU(Scheduler scheduler) {
        
        this.scheduler = scheduler;
        this.readyQueue = new Queue();
        this.processQueue = new Queue();
        this.blockedQueue = new Queue();
        this.blockedQueueAux = new Queue(); // esta cola contiene al proceso que está siendo atendido por I/O 
        this.runningQueue = new Queue();
        this.finishedQueue = new Queue();
        this.suspendedReadyQueue = new Queue();
        this.suspendedBlockedQueue = new Queue(); 
        this.memoryNeededSO = 51;
        this.totalMemory = totalMemory - memoryNeededSO;
    }
        /**
         * Función que se encarga de la ejecución principal de la CPU.
         * Controla la planificación, interrupciones, E/S y ejecución de instrucciones.
        */
        public void ejecutar() {
            int rrQuantumCounter = 0;
            int feedbackQuantumCounter = 0;

            if (scheduler instanceof Feedback) {
                fbScheduler = (Feedback) scheduler;
            }

            logCPU("Iniciando simulacion...");

            while (true) {
                // Ejecutar SO si hay alguna petición pendiente
                executeRequestedSOIfAny();

                // 1) Llegada de procesos
                if (!processQueue.isEmpty()) {
                    Queue tempQueue = new Queue();
                    while (!processQueue.isEmpty()) {
                        Process p = (Process) processQueue.dispatch();
                        if (p.getArrivalTime() <= currentTime && p.getStatus() == Process.Status.NEW) {
                            if (fbScheduler != null) {
                                fbScheduler.addNewProcess(p);
                            } else {
                                addProcessToReadyQueue(p);
                            }
                            synchronized (arrivalLock) { arrivalLock.notifyAll(); }
                        } else {
                            tempQueue.enqueue(p);
                        }
                    }
                    processQueue = tempQueue;
                }

                // 2) Selección del siguiente proceso a ejecutar
                if (currentProcess == null) {
                    if (scheduler instanceof HRRN) {
                        ((HRRN) scheduler).updateTime(currentTime);
                    }

                    if (fbScheduler != null) {
                        currentProcess = fbScheduler.getNextProcess();
                        procesoActual = currentProcess;
                        feedbackQuantumCounter = 0;
                        if (currentProcess != null) {
                            currentProcess.setStatus(Process.Status.RUNNING);
                            runningQueue.enqueue(currentProcess);
                            logCPU("Despachando (Feedback) proceso " + currentProcess.getPid());

                            // Cambio de proceso: solicitar SO
                            requestSO("cambio de proceso");
                            executeRequestedSOIfAny();
                        }
                    } else {
                        currentProcess = scheduler.nextProcess(readyQueue);
                        procesoActual = currentProcess;
                        rrQuantumCounter = 0;
                        if (currentProcess != null) {
                            currentProcess.setStatus(Process.Status.RUNNING);
                            runningQueue.enqueue(currentProcess);
                            logCPU("Despachando proceso " + currentProcess.getPid());

                            // Cambio de proceso: solicitar SO
                            requestSO("cambio de proceso");
                            executeRequestedSOIfAny();
                        }
                    }
                }

                // 3) CPU ociosa
                if (currentProcess == null) {
                    try { Thread.sleep(cycleDurationMs); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    currentTime++;
                    totalCycles++;
                    continue;
                }

                // 4) Proceso genera una interrupción de E/S
                if (!currentProcess.isCpuBound() && currentProcess.getCyclesToException() == 0) {
                    logCPU("Proceso " + currentProcess.getPid() + " genera excepcion de E/S -> BLOQUEADO");

                    // Verificar si el proceso está en memoria o suspendido
                    if (currentProcess.isInMemory()) {
                        currentProcess.setStatus(Process.Status.BLOCKED);
                        blockedQueue.enqueue(currentProcess);
                    } else {
                        // Si no está en memoria, va directamente a suspendido-bloqueado
                        currentProcess.setStatus(Process.Status.SUSPENDED_BLOCKED);
                        suspendedBlockedQueue.enqueue(currentProcess);
                    }

                    runningQueue.remove(currentProcess);
                    currentProcess = null;
                    rrQuantumCounter = 0;
                    feedbackQuantumCounter = 0;

                    // Interrupción de solicitud de E/S: solicitar SO
                    requestSO("interrupción de solicitud de E/S");
                    executeRequestedSOIfAny();

                    // Solo procesar E/S si el proceso está en memoria (no suspendido)
                    if (!suspendedBlockedQueue.isEmpty() || !blockedQueue.isEmpty()) {
                        Process ioProc;
                        if (!blockedQueue.isEmpty()) {
                            ioProc = (Process) blockedQueue.dispatch();
                            blockedQueueAux.enqueue(ioProc);
                        } else {
                            // Tomar de suspendido-bloqueado si no hay en bloqueado normal
                            ioProc = (Process) suspendedBlockedQueue.dispatch();
                            // No va a blockedQueueAux porque está suspendido
                        }

                        if (ioProc != null && ioProc.isInMemory()) {
                            new Thread(() -> {
                                try {
                                    ioSemaphore.acquire();
                                    logIO("Atendiendo E/S de proceso " + ioProc.getPid() +
                                          " (servicio " + ioProc.getExceptionServiceCycles() + " ciclos)");

                                    Thread.sleep(ioProc.getExceptionServiceCycles() * cycleDurationMs + 50);

                                    // Actualizar estado según si está en memoria o suspendido
                                    if (ioProc.isInMemory()) {
                                        ioProc.setStatus(Process.Status.READY);

                                        if (fbScheduler != null) {
                                            synchronized (fbScheduler) {
                                                for (int i = 0; i < fbScheduler.getQueues().getLenght(); i++) {
                                                    Queue q = fbScheduler.getQueues().getElementGeneric(i);
                                                    if (q != null) q.remove(ioProc);
                                                }
                                                blockedQueueAux.remove(ioProc);
                                                fbScheduler.addNewProcess(ioProc);
                                            }
                                            logIO("E/S completada: proceso " + ioProc.getPid() + " -> Feedback nivel 0");
                                        } else {
                                            synchronized (readyQueue) {
                                                readyQueue.enqueue(ioProc);
                                                blockedQueueAux.remove(ioProc);
                                            }
                                            logIO("E/S completada: proceso " + ioProc.getPid() + " -> ready");
                                        }
                                    } else {
                                        // Proceso suspendido: pasa de SUSPENDED_BLOCKED a SUSPENDED_READY
                                        ioProc.setStatus(Process.Status.SUSPENDED_READY);
                                        suspendedReadyQueue.enqueue(ioProc);
                                        logIO("E/S completada: proceso " + ioProc.getPid() + " suspendido -> SUSPENDED_READY");
                                    }

                                    ioProc.setCyclesToException(-1);

                                    // Interrupción de finalización de E/S: solicitar SO (NO ejecutarlo aquí)
                                    requestSO("interrupcion de finalización E/S");

                                    synchronized (arrivalLock) { arrivalLock.notifyAll(); }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                } finally {
                                    ioSemaphore.release();
                                }
                            }).start();
                        }
                    }

                    continue;
                }

                // 5) Ejecutar instrucción normal
                try {
                    Thread.sleep(cycleDurationMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                totalCycles++;
                busyCycles++;
                currentTime++;

                try { currentProcess.incrementPC(); } catch (Exception ignored) {}
                try { currentProcess.incrementMAR(); } catch (Exception ignored) {}
                try {
                    currentProcess.decrementRemainingInstructions();
                    logProc("[" + currentProcess.getPid() + "] Ejecutando instruccion " +
                            (currentProcess.getTotalInstructions() - currentProcess.getRemainingInstructions()) +
                            " | PC=" + currentProcess.getPc() + " | MAR=" + currentProcess.getMar());
                } catch (Exception ignored) {}

                if (currentProcess.getCyclesToException() > 0) {
                    currentProcess.setCyclesToException(currentProcess.getCyclesToException() - 1);
                }

                synchronized (arrivalLock) {
                    try { arrivalLock.wait(5); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                // Ejecutar SO si quedó alguna petición pendiente
                executeRequestedSOIfAny();

                // 6) Verificar si terminó el proceso
                if (currentProcess.getRemainingInstructions() <= 0) {
                    currentProcess.setCompletionTime(currentTime);
                    currentProcess.setStatus(Process.Status.TERMINATED);
                    currentProcess.setInMemory(false);
                    runningQueue.remove(currentProcess);
                    readyQueue.remove(currentProcess);
                    blockedQueue.remove(currentProcess);
                    finishedQueue.enqueue(currentProcess);
                    logCPU("Proceso " + currentProcess.getPid() + " finalizado.");

                    // intentar pasar un proceso de suspendido listo a memoria
                    intentarReanudarProcesosSuspendidos();
                            
                    currentProcess = null;
                    procesoActual = null;
                    rrQuantumCounter = 0;
                    feedbackQuantumCounter = 0;
                    continue;
                }

                // 7) Control de quantum y preempción
                if (scheduler instanceof SRT) {
                    Process shortest = ((SRT) scheduler).peekNextProcess(readyQueue);
                    if (shortest != null && shortest.getRemainingInstructions() < currentProcess.getRemainingInstructions()) {
                        logSched("Preempcion: proceso " + currentProcess.getPid() +
                                 " reencolado por " + shortest.getPid());
                        runningQueue.remove(currentProcess);
                        currentProcess.setStatus(Process.Status.READY);
                        addProcessToReadyQueue(currentProcess);

                        requestSO("cambio de proceso");
                        executeRequestedSOIfAny();

                        currentProcess = null;
                        procesoActual = null;
                        rrQuantumCounter = 0;
                        continue;
                    }
                }

                if (scheduler instanceof RR) {
                    rrQuantumCounter++;
                    int quantum = ((RR) scheduler).getQuantum();
                    if (rrQuantumCounter >= quantum) {
                        logSched("Quantum terminado, reencolando proceso " + currentProcess.getPid());
                        addProcessToReadyQueue(currentProcess);
                        runningQueue.remove(currentProcess);
                        currentProcess.setStatus(Process.Status.READY);

                        requestSO("cambio de proceso");
                        executeRequestedSOIfAny();

                        currentProcess = null;
                        rrQuantumCounter = 0;
                        continue;
                    }
                }

                if (fbScheduler != null && currentProcess != null) {
                    feedbackQuantumCounter++;
                    int currentLevel = currentProcess.getCurrentLevel();
                    int quantumActual = fbScheduler.getQuantums()[currentLevel];
                    if (feedbackQuantumCounter >= quantumActual) {
                        logSched("Quantum terminado para proceso " +
                                 currentProcess.getPid() + " en nivel " + currentLevel);
                        fbScheduler.requeueProcess(currentProcess, currentLevel);
                        currentProcess.setStatus(Process.Status.READY);

                        requestSO("cambio de proceso");
                        executeRequestedSOIfAny();

                        currentProcess = null;
                        feedbackQuantumCounter = 0;
                        continue;
                    }
                }
            }
        }

        
        // Método para pedir ejecución del SO (llamado desde IO handler u otras partes)
        private void requestSO(String reason) {
            synchronized (soLock) {
                // si ya hay una petición, preferimos conservar la primera
                soRequested = true;
                soRequestedReason = reason;
            }
            // notificar para que el hilo CPU pueda reaccionar lo antes posible
            synchronized (arrivalLock) { arrivalLock.notifyAll(); }
        }

        // Ejecuta la petición de SO (síncrono, debe llamarlo el hilo CPU)
        private void executeRequestedSOIfAny() {
            synchronized (soLock) {
                if (!soRequested) return;
                // capturar el motivo antes de resetear
                String reason = soRequestedReason;
                soRequested = false;
                soRequestedReason = null;
                // ejecutar SO de forma exclusiva
                ejecutarSO(reason);
            }
        }

        private void calcularMemoriaOcupada() {
            int memoriaOcupada = 0;
            LinkedList procesos = obtenerTodosLosProcesos();

            for (int i = 0; i < procesos.getLenght(); i++) {
                Process p = procesos.getElementIn(i);
                if (p.isInMemory()) {
                    memoriaOcupada += p.getMemoryNeeded();
                }
            }

            usedMemory = memoriaOcupada;
            //logSO("Memoria ocupada: " + usedMemory + " / " + totalMemory + " MB");
        }

        private void suspendProcess(Process p) {
            if (p == null) return;

            readyQueue.remove(p);
            blockedQueueAux.remove(p);

            usedMemory -= p.getMemoryNeeded();
            p.setInMemory(false);

            if (p.getStatus() == Process.Status.BLOCKED) {
                p.setStatus(Process.Status.SUSPENDED_BLOCKED);
                suspendedBlockedQueue.enqueue(p);
                logSO("Proceso " + p.getPid() + " suspendido (bloqueado). Memoria liberada.");
                requestSO("swapping de proceso bloqueado a suspendido-bloqueado");
            } else {
                p.setStatus(Process.Status.SUSPENDED_READY);
                suspendedReadyQueue.enqueue(p);
                logSO("Proceso " + p.getPid() + " suspendido (listo). Memoria liberada.");
                requestSO("swapping de proceso listo a suspendido-listo");
            }

            logSO("Memoria usada ahora: " + usedMemory + "/" + totalMemory);
        }
        
                public void addProcessToReadyQueue(Process process) {
            calcularMemoriaOcupada();

            if (usedMemory + process.getMemoryNeeded() <= totalMemory) {
                process.setStatus(Process.Status.READY);
                process.setInMemory(true);
                readyQueue.enqueue(process);
                usedMemory += process.getMemoryNeeded();
                logSched("Proceso " + process.getPid() + " agregado a la cola de listos. Memoria usada: " 
                        + usedMemory + "/" + totalMemory);
                return;
            }

            logSO("Memoria insuficiente para proceso " + process.getPid() + ". Intentando liberar espacio...");

            Process toSuspend = findProcessToSuspend();
            if (toSuspend != null) {
                suspendProcess(toSuspend);
                calcularMemoriaOcupada();
            }

            if (usedMemory + process.getMemoryNeeded() <= totalMemory) {
                process.setStatus(Process.Status.READY);
                process.setInMemory(true);
                readyQueue.enqueue(process);
                usedMemory += process.getMemoryNeeded();
                logSched("Proceso " + process.getPid() + " agregado a la cola de listos tras liberar memoria.");
            } else {
                process.setStatus(Process.Status.SUSPENDED_READY);
                process.setInMemory(false);
                suspendedReadyQueue.enqueue(process);
                logSO("Proceso " + process.getPid() + " suspendido (cola suspendidos-listos).");
            }
        }

        private Process findProcessToSuspend() {
            Process candidate = null;

            Nodo current = blockedQueueAux.getHead();
            while (current != null) {
                Process p = (Process) current.getElement();
                if (p.isInMemory()) {
                    if (candidate == null) {
                        candidate = p;
                    } else if (p.getExceptionServiceCycles() > candidate.getExceptionServiceCycles()) {
                        candidate = p;
                    }
                }
                current = current.getNext();
            }

            if (candidate == null) {
                current = readyQueue.getHead();
                while (current != null) {
                    Process p = (Process) current.getElement();
                    if (p.isInMemory()) {
                        if (candidate == null) {
                            candidate = p;
                        } else {
                            if (p.getPriority() > candidate.getPriority()) {
                                candidate = p;
                            } else if (p.getPriority() == candidate.getPriority() &&
                                       p.getMemoryNeeded() > candidate.getMemoryNeeded()) {
                                candidate = p;
                            } else if (p.getPriority() == candidate.getPriority() &&
                                       p.getMemoryNeeded() == candidate.getMemoryNeeded() &&
                                       p.getTimeInMemory(currentTime) > candidate.getTimeInMemory(currentTime)) {
                                candidate = p;
                            }
                        }
                    }
                    current = current.getNext();
                }
            }

            return candidate;
        }

        private void gestionarReanudacionProcesos() {
            if (suspendedReadyQueue.isEmpty()) {
                return;
            }

            if (usedMemory >= totalMemory) {
                return;
            }

            boolean seReanudoAlguno = false;
            int procesosReanudados = 0;

            // Obtener lista ordenada por prioridad (procesos con mayor prioridad primero)
            LinkedList<Process> procesosOrdenados = obtenerProcesosSuspendidosPorPrioridad();
            Queue tempNoReanudados = new Queue();

            // Intentar reanudar procesos por orden de prioridad
            for (int i = 0; i < procesosOrdenados.getLenght() && usedMemory < totalMemory; i++) {
                Process p = procesosOrdenados.getElementGeneric(i);

                if (usedMemory + p.getMemoryNeeded() <= totalMemory) {
                    // Reanudar proceso
                    usedMemory += p.getMemoryNeeded();
                    p.setInMemory(true);
                    p.setStatus(Process.Status.READY);
                    
                    if (fbScheduler != null) {
                        fbScheduler.addNewProcess(p);
                    } else {
                        addProcessToReadyQueue(p);
                    }

                    procesosReanudados++;
                    seReanudoAlguno = true;
                    logSO("Proceso " + p.getPid() + " (Prioridad: " + p.getPriority() + 
                          ") reanudado automáticamente.");

                    // Remover de suspendedReadyQueue
                    suspendedReadyQueue.remove(p);
                } else {
                    // Mantener en cola temporal para no perderlo
                    tempNoReanudados.enqueue(p);
                }
            }

            // Los procesos que no cupieron se mantienen suspendidos
            // (ya están en suspendedReadyQueue)

            if (seReanudoAlguno) {
                logSO(procesosReanudados + " proceso(s) reanudado(s). Memoria: " + 
                      usedMemory + "/" + totalMemory + " MB");
                calcularMemoriaOcupada();
            }
        }

        private LinkedList<Process> obtenerProcesosSuspendidosPorPrioridad() {
            LinkedList<Process> lista = new LinkedList<>();

            // Copiar todos los procesos suspendidos a una lista
            Queue temp = new Queue();
            while (!suspendedReadyQueue.isEmpty()) {
                Process p = (Process) suspendedReadyQueue.dispatch();
                lista.insertFinal(p);
                temp.enqueue(p);
            }

            // Restaurar la cola original
            while (!temp.isEmpty()) {
                suspendedReadyQueue.enqueue(temp.dispatch());
            }

            // Ordenar por prioridad descendente (mayor prioridad primero)
            for (int i = 0; i < lista.getLenght() - 1; i++) {
                for (int j = i + 1; j < lista.getLenght(); j++) {
                    Process p1 = lista.getElementGeneric(i);
                    Process p2 = lista.getElementGeneric(j);

                    if (p2.getPriority() > p1.getPriority()) {
                        // Intercambiar posiciones
                        lista.setElementIn(i, p2);
                        lista.setElementIn(j, p1);
                    }
                }
            }

            return lista;
        }
        
        private void intentarReanudarProcesosSuspendidos() {
            // Solo intentar reanudar si hay memoria disponible
            if (usedMemory < totalMemory && !suspendedReadyQueue.isEmpty()) {
                gestionarReanudacionProcesos();
            }
        }

        private void ejecutarSO(String motivo) {
            int ciclos = 2;
            soEjecutando = true;

            pc = 0;
            mar = 0;

            for (int i = 0; i < ciclos; i++) {
                try {
                    Thread.sleep(cycleDurationMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                pc++;
                mar++;
                totalCycles++;
                busyCycles++;
                currentTime++;

                logSO("Ejecutando ciclo " + (i + 1) + "/" + ciclos + " para " + motivo);
            }

            soEjecutando = false;
        }

        private boolean hasAnyReadyProcess(Feedback fb) {
            for (int i = 0; i < fb.getQueues().getLenght(); i++) {
                Queue q = fb.getQueues().getElementGeneric(i);
                if (q != null && !q.isEmpty()) return true;
            }
            return false;
        }

        private int findProcessLevel(Feedback fb, Process p) {
            for (int i = 0; i < fb.getQueues().getLenght(); i++) {
                Queue q = fb.getQueues().getElementGeneric(i);
                if (q.contains(p)) return i;
            }
            return 0;
        }

        public void addProcessQueue(Process process) {
            process.setStatus(Process.Status.NEW);
            processQueue.enqueue(process);
            logSched("Proceso " + process.getPid() + " agregado a la cola de procesos.");
        }

        public long getCurrentTime() {
            return currentTime;
        }

        public LinkedList<Process> obtenerTodosLosProcesos() {
            LinkedList<Process> lista = new LinkedList<>();

            Queue[] colas = {processQueue, runningQueue, readyQueue, blockedQueueAux, finishedQueue, suspendedReadyQueue, suspendedBlockedQueue};
            for (Queue q : colas) {
                Nodo actual = q.getHead();
                while (actual != null) {
                    Object elem = actual.getElement();
                    if (elem instanceof Process pcb && !lista.existe(pcb)) {
                        lista.insertFinal(pcb);
                    }
                    actual = actual.getNext();
                }
            }

            if (scheduler instanceof Feedback fb) {
                LinkedList<Queue> fbQueues = fb.getQueues();
                for (int i = 0; i < fbQueues.getLenght(); i++) {
                    Queue q = fbQueues.getElementGeneric(i);
                    if (q == null) continue;

                    Nodo actual = q.getHead();
                    while (actual != null) {
                        Object elem = actual.getElement();
                        if (elem instanceof Process pcb && !lista.existe(pcb)) {
                            lista.insertFinal(pcb);
                        }
                        actual = actual.getNext();
                    }
                }
            }

            return lista;
        }

        public void cambiarScheduler(Scheduler newScheduler) {
            logCPU("Cambiando scheduler a: " + newScheduler.getClass().getSimpleName());

            LinkedList<Process> todosProcesos = obtenerTodosLosProcesos();

            if (scheduler instanceof Feedback oldFb) {
                LinkedList<Queue> fbQueues = oldFb.getQueues();
                for (int i = 0; i < fbQueues.getLenght(); i++) {
                    Queue q = fbQueues.getElementGeneric(i);
                    Nodo nodo = q.getHead();
                    while (nodo != null) {
                        Object elem = nodo.getElement();
                        q.remove(elem);
                        nodo = nodo.getNext();
                    }
                }
                fbScheduler = null;
            }

            scheduler = newScheduler;
            if (scheduler instanceof Feedback fb) {
                fbScheduler = fb;
            }

            for (int i = 0; i < todosProcesos.getLenght(); i++) {
                Process p = todosProcesos.getElementGeneric(i);

                if (p.getStatus() != Process.Status.TERMINATED && p != currentProcess) {
                    p.setStatus(Process.Status.READY);
                    if (fbScheduler != null) {
                        fbScheduler.addNewProcess(p);
                    } else {
                        addProcessToReadyQueue(p);
                    }
                }
            }

            logCPU("Scheduler cambiado correctamente.");
        }

   
   public boolean contieneProceso(Process proceso) {
        return readyQueue.contains(proceso) || runningQueue.contains(proceso) || blockedQueue.contains(proceso) 
                || blockedQueueAux.contains(proceso) || finishedQueue.contains(proceso)
                || processQueue.contains(proceso) || (fbScheduler != null && hasAnyReadyProcess(fbScheduler)) 
                || suspendedReadyQueue.contains(proceso) || suspendedBlockedQueue.contains(proceso);
   }
   
    // Logs mejorados
    private synchronized void log(String category, String message) {
        String logMsg = String.format("[Clock %3d] [%s] %s%n", currentTime, category, message);
        System.out.print(logMsg);  
        notifyLogListener(logMsg); 
    }

    private void logSO(String message) { log("SO", message); }
    private void logIO(String message) { log("DMA", message); }
    private void logCPU(String message) { log("CPU", message); }
    private void logSched(String message) { log("Scheduler", message); }
    private void logProc(String message) { log("Proceso", message); }
    
    public void setLogListener(Consumer<String> listener) {
        this.logListener = listener;
    }

    private void notifyLogListener(String message) {
        if (logListener != null) {
            logListener.accept(message);
        }
    }

   
   public Object getScheduler(){
       return this.scheduler;
   }
   
   public void setScheduler(Scheduler scheduler){
       this.scheduler = scheduler;
   }
   
   public int getPc() { 
       return pc; 
   }
   public int getMar() { 
       return mar; 
   }
   public boolean isSoEjecutando() { 
       return soEjecutando; 
   }
   public String getStatus() { 
       return status; 
   }
   public String getTipo() { 
       return tipo; 
   }
   
   public Process getCurrentProcess() {
       return currentProcess;
   }
   
   public double getCpuUtilization() {
        return totalCycles == 0 ? 0 : (double) busyCycles / totalCycles;
    }
    
    public int getCycleDurationMs() {
        return this.cycleDurationMs;
    }
    
    public int getTotalCycles(){
        return totalCycles;
    }
    
    public int getBusyCycles(){
        return busyCycles;
    }

    public void setCycleDurationMs(int cycleDurationMs) {
        this.cycleDurationMs = cycleDurationMs;
    }

   
  
}
