


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
    private Scheduler scheduler;
    public static int cycleDurationMs = 500; // duración de cada ciclo (simulada) por default: 500
    private int currentTime = 0;
    private Feedback fbScheduler; 
    
    // Métricas básicas
    private int totalCycles = 0;
    private int busyCycles = 0;

    private Queue readyQueue;
    private Queue processQueue;
    private Queue blockedQueue;
    private Queue blockedQueueAux;
    private Queue runningQueue;
    private Queue finishedQueue;
    Semaphore ioSemaphore = new Semaphore(1); // solo un dispositivo de E/S disponible
    private Process currentProcess;  
    private Process procesoActual;  

    private static final int PROCESS_SWITCH_COST = 2;  // cambio de proceso
    private static final int IO_INTERRUPT_COST = 2;     // atención de interrupción E/S
    private static final int DISPATCH_COST = 1;         // cargar un nuevo proceso
    
    // Para el SO: 
    private int pc;
    private int mar;
    private boolean soEjecutando;
    private String status;
    private String tipo;
    
    private LinkedList<Double> historialUtilidad = new LinkedList<>();

    

    public CPU(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.readyQueue = new Queue();
        this.processQueue = new Queue();
        this.blockedQueue = new Queue();
        this.blockedQueueAux = new Queue(); // esta cola contiene al proceso que está siendo atendido por I/O 
        this.runningQueue = new Queue();
        this.finishedQueue = new Queue();
    }

        public void ejecutar() {
        
        int rrQuantumCounter = 0;
        int feedbackQuantumCounter = 0;
        boolean interruptedByIO = false; // para interrumpir la cpu si I/O termina
        Object arrivalLock = new Object(); // para notificar llegada de I/O  / nuevos listos

        // referencia a Feedback si aplica
        if (scheduler instanceof Feedback) {
            fbScheduler = (Feedback) scheduler;
        }

        // bucle principal: mientras existan procesos en cualquier cola o haya un proceso en ejecución
        while (!processQueue.isEmpty() || !readyQueue.isEmpty() || !blockedQueue.isEmpty() || !blockedQueueAux.isEmpty() || currentProcess != null
                || (fbScheduler != null && hasAnyReadyProcess(fbScheduler))) { 

            // 1) Revisar llegada de procesos desde processQueue (cada ciclo)
            if (!processQueue.isEmpty()) {
                Queue tempQueue = new Queue();
                while (!processQueue.isEmpty()) {
                    Process p = (Process) processQueue.dispatch();
                    if (p.getArrivalTime() <= currentTime && p.getStatus() == Process.Status.NEW) {
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
                    procesoActual = currentProcess;
                    feedbackQuantumCounter = 0;
                    if (currentProcess != null) {
                        currentProcess.setStatus(Process.Status.RUNNING);
                        runningQueue.enqueue(currentProcess);
                        System.out.println("[CPU] Despachando (Feedback) proceso " + currentProcess.getPid());
                    }
                } else {
                    // Normal scheduler (RR, FCFS, SRT, SPN, HRRN)
                    currentProcess = scheduler.nextProcess(readyQueue);
                    procesoActual = currentProcess;
                    rrQuantumCounter = 0;
                    if (currentProcess != null) {
                        currentProcess.setStatus(Process.Status.RUNNING);
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
                // no se incrementa busyCycles porque la CPU estuvo idle
                continue;
            }

            // 4) Antes de ejecutar la instrucción: comprobar e/s del proceso actual
            if (!currentProcess.isCpuBound() && currentProcess.getCyclesToException() == 0) {
                System.out.println("[Clock " + currentTime + "] Proceso " + currentProcess.getPid() + " genera excepción de E/S -> BLOQUEADO");
                currentProcess.setStatus(Process.Status.BLOCKED);
                blockedQueue.enqueue(currentProcess);
                runningQueue.remove(currentProcess);
                currentProcess = null;
                rrQuantumCounter = 0;
                feedbackQuantumCounter = 0;

                // lanzar hilo para simular atención I/O
                final Process ioProc = (Process) blockedQueue.dispatch(); 
                blockedQueueAux.enqueue(ioProc);
                new Thread(() -> {
                    ioSemaphore.acquire();
                    try {
                        System.out.println("[I/O Handler] Atendiendo E/S de proceso " + ioProc.getPid() + " (servicio " + ioProc.getExceptionServiceCycles() + " ciclos)");
                        // simular tiempo de servicio de E/S (en ms)
                        Thread.sleep(ioProc.getExceptionServiceCycles() * cycleDurationMs + 50);
                        ioProc.setStatus(Process.Status.READY);
                        ioProc.setCyclesToException(-1); // evitar nuevas excepciones si ese es el comportamiento deseado


                            // encolar de vuelta (si Feedback, al nivel 0; si no, a readyQueue)
                        if (fbScheduler != null) {
                            synchronized (fbScheduler) {
                                // Eliminar el proceso de cualquier cola de Feedback donde aún esté
                                for (int i = 0; i < fbScheduler.getQueues().getLenght(); i++) {
                                    Queue q = fbScheduler.getQueues().getElementGeneric(i);
                                    if (q != null) q.remove(ioProc);
                                }

                                //  Eliminarlo de bloqueados auxiliares
                                blockedQueueAux.remove(ioProc);

                                // Reinsertar limpio en nivel 0
                                fbScheduler.addNewProcess(ioProc);
                            }
                            System.out.println("[Clock " + currentTime + "] [I/O] E/S completada: proceso " +
                                               ioProc.getPid() + " -> Feedback nivel 0");
                        } else {
                            synchronized (readyQueue) {
                                readyQueue.enqueue(ioProc);
                                blockedQueueAux.remove(ioProc); // también eliminar en este caso
                            }
                            System.out.println("[Clock " + currentTime + "] [I/O] E/S completada: proceso " +
                                               ioProc.getPid() + " -> ready");
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
            try {
                // Simular el tiempo real de ejecución de la instrucción
                Thread.sleep(cycleDurationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // actualizar contadores y métricas
            totalCycles++;
            busyCycles++;
            currentTime++;
            actualizarRendimiento();

            // Actualizar campos del proceso (CPU hace pc++, mar++, remainingInstructions--)
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


            // reducir contador hacia próxima E/S
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
                currentProcess.setCompletionTime(currentTime);
                currentProcess.setStatus(Process.Status.TERMINATED);
                runningQueue.remove(currentProcess);
                readyQueue.remove(currentProcess);
                blockedQueue.remove(currentProcess);
                finishedQueue.enqueue(currentProcess);
                System.out.println("[Clock " + currentTime + "] [CPU] Proceso " + currentProcess.getPid() + " finalizado.");
                currentProcess = null;
                 procesoActual = null; 
                rrQuantumCounter = 0;
                feedbackQuantumCounter = 0;
                continue;
            }


            // 9) Reglas de preempción/quantum según scheduler

            // -- SRT (preemptiva por remaining time)
            if (scheduler instanceof SRT) {
                Process shortest = ((SRT) scheduler).peekNextProcess(readyQueue);
                if (shortest != null && shortest.getRemainingInstructions() < currentProcess.getRemainingInstructions()) {

                    System.out.println("[Scheduler SRT] Preempción: proceso " + currentProcess.getPid() +
                                       " reencolado por " + shortest.getPid());

                    // Quitar del runningQueue ANTES de reencolar
                    runningQueue.remove(currentProcess);

                    // Actualizar estado y reencolar en ready
                    currentProcess.setStatus(Process.Status.READY);
                    addProcess(currentProcess);

                    // Limpiar referencias
                    currentProcess = null;
                    procesoActual = null; // <- AGREGAR
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
                    currentProcess.setStatus(Process.Status.READY);
                    currentProcess = null;
                    rrQuantumCounter = 0;
                    continue;
                }
            }

            // -- Feedback: controlar quantum del nivel actual
            if (fbScheduler != null && currentProcess != null) {
                feedbackQuantumCounter++;
                int currentLevel = currentProcess.getCurrentLevel();
                int quantumActual = fbScheduler.getQuantums()[currentLevel];
                if (feedbackQuantumCounter >= quantumActual) {
                    System.out.println("[Scheduler Feedback] Quantum terminado para proceso " + currentProcess.getPid() +
                                       " en nivel " + currentLevel + ". Reencolando / degradando nivel.");
                    fbScheduler.requeueProcess(currentProcess, currentLevel);
                    currentProcess.setStatus(Process.Status.READY);
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

  
}
    
    private void ejecutarSO(int ciclos) {
        pc = 0;
        mar = 0;
        soEjecutando = true;
        
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

            System.out.println("[SO] Ejecutando SO... ciclo " + (i + 1) + "/" + ciclos);

        }
        soEjecutando = false;
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

    // ===== Métodos auxiliares =====
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
        return 0; // por defecto nivel 0 si no encuentra
    }

    
    public void addProcess(Process process) {
        process.setStatus(Process.Status.READY);
        readyQueue.enqueue(process);
        System.out.println("[CPU Scheduler] Proceso " + process.getPid() + " agregado a la cola de listos.");
    }
    
    public void addProcessQueue(Process process) {
        process.setStatus(Process.Status.NEW);
        processQueue.enqueue(process);
        System.out.println("[CPU Scheduler] Proceso " + process.getPid() + " agregado a la cola de procesos.");

//        // Si la cola está vacía, simplemente encolamos
//        if (processQueue.isEmpty()) {
//            processQueue.enqueue(process);
//            System.out.println("[CPU Scheduler] Proceso " + process.getPid() + " agregado a la cola de procesos.");
//            return;
//        }
//
//        // Creamos una cola temporal para reconstruir la cola en orden
//        Queue tempQueue = new Queue();
//        boolean inserted = false;
//
//        while (!processQueue.isEmpty()) {
//            Process p = (Process) processQueue.dispatch();
//            // Insertamos el nuevo proceso antes de cualquier proceso con arrivalTime mayor
//            if (!inserted && process.getArrivalTime() < p.getArrivalTime()) {
//                tempQueue.enqueue(process);
//                inserted = true;
//            }
//            tempQueue.enqueue(p);
//        }
//
//        // Si no se insertó, es el último
//        if (!inserted) {
//            tempQueue.enqueue(process);
//        }
//
//        // Reemplazamos la cola original con la ordenada
//        processQueue = tempQueue;
//
//        System.out.println("[CPU Scheduler] Proceso " + process.getPid() + " agregado a la cola de procesos.");
    }
    
    public long getCurrentTime() {
    return currentTime;
}
//    public LinkedList<PCB> obtenerProcesosTotales() {
//            LinkedList<PCB> lista = new LinkedList<>();
//            
//            agregarDeCola(lista, processQueue);
//            agregarDeCola(lista, runningQueue);
//            agregarDeCola(lista, readyQueue);
//            agregarDeCola(lista, blockedQueueAux);
//            agregarDeCola(lista, finishedQueue);
//            
//            return lista;
//    }

    private void agregarDeCola(LinkedList<Process> lista, Queue cola) {
            Nodo actual = cola.getHead();
            while (actual != null) {
                Object elemento = actual.getElement();
                if (elemento instanceof Process pcb) {
                    lista.insertFinal(pcb);
                }
                actual = actual.getNext();
            }
    }
    
   public LinkedList<Process> obtenerTodosLosProcesos() {
    LinkedList<Process> lista = new LinkedList<>();

    // Agregar procesos de las colas normales
    Queue[] colas = {processQueue, runningQueue, readyQueue, blockedQueueAux, finishedQueue};
    for (Queue q : colas) {
        Nodo actual = q.getHead(); // suponiendo Queue tiene getHead()
        while (actual != null) {
            Object elem = actual.getElement();
            if (elem instanceof Process pcb && !lista.existe(pcb)) {
                lista.insertFinal(pcb);
            }
            actual = actual.getNext();
        }
    }

    // Agregar procesos de Feedback si aplica
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
    System.out.println("[CPU] Cambiando scheduler a: " + newScheduler.getClass().getSimpleName());

    // 1) Guardar todos los procesos activos
    LinkedList<Process> todosProcesos = obtenerTodosLosProcesos();

    // 2) Limpiar referencias al scheduler anterior si era Feedback
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

    // 3) Asignar nuevo scheduler
    scheduler = newScheduler;
    if (scheduler instanceof Feedback fb) {
        fbScheduler = fb;
    }

    // 4) Reinsertar procesos en el nuevo scheduler
    for (int i = 0; i < todosProcesos.getLenght(); i++) {
        Process p = todosProcesos.getElementGeneric(i);

        if (p.getStatus() != Process.Status.TERMINATED) {
            // Solo reintegrar los procesos que NO estén ejecutándose actualmente
            if (p != currentProcess) {
                p.setStatus(Process.Status.READY);
                if (fbScheduler != null) {
                    fbScheduler.addNewProcess(p);
                } else {
                    addProcess(p);
                }
            }
        }
    }

    System.out.println("[CPU] Scheduler cambiado correctamente.");
}
   
   public Object getScheduler(){
       return this.scheduler;
   }
   
   public void setScheduler(Scheduler scheduler){
       this.scheduler = scheduler;
   }
   
   public boolean contieneProceso(Process proceso){
        return readyQueue.contains(proceso) || runningQueue.contains(proceso) || blockedQueue.contains(proceso) 
                || blockedQueueAux.contains(proceso) || finishedQueue.contains(proceso)
                || processQueue.contains(proceso) || (fbScheduler != null && hasAnyReadyProcess(fbScheduler));
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
   
   public double getUtilizacion() {
    if (totalCycles == 0) return 0;
    return ((double) busyCycles / totalCycles) * 100.0;
}
   
   private void actualizarRendimiento() {
    double utilidadActual = getUtilizacion();
    historialUtilidad.insertFinal(utilidadActual);
}
   
   public LinkedList<Double> getHistorialUtilidad() {
    return historialUtilidad;
}
     
  
}
