/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author Jose
 */

/**
 * Representa un proceso dentro del simulador del planificador de CPU.
 * Cada PCB tiene su propio hilo y se sincroniza con el CPU mediante semáforos.
 */
public class PCB implements Runnable {

    // ======== Atributos básicos del proceso ========
    private int pid;
    private String name;
    private int totalInstructions;
    private int remainingInstructions;
    private int pc;
    private int mar;
    private int memoryNeeded;
    private boolean cpuBound;
    private int cyclesToException;
    private int exceptionServiceCycles;
    private int priority;

    public enum Status {
        NEW, READY, RUNNING, BLOCKED, SUSPENDED, TERMINATED
    }
    
    private int arrivalTime;
    private int startTime;
    private int finishTime;
    private Status status;

    //  Sincronización 
    private final Semaphore canRun = new Semaphore(0); // CPU da permiso
    private final Semaphore done = new Semaphore(0);   // Proceso avisa al CPU
    private Thread thread; // hilo interno que representa al proceso

   
    public PCB(int pid, String name, int totalInstructions, boolean cpuBound,
               int cyclesToException, int exceptionServiceCycles,
               int memoryNeeded, int priority, int arrivalTime) {

        this.pid = pid;
        this.name = name;
        this.totalInstructions = totalInstructions;
        this.remainingInstructions = totalInstructions;
        this.pc = 0;
        this.mar = 0;
        this.cpuBound = cpuBound;
        this.cyclesToException = cyclesToException;
        this.exceptionServiceCycles = exceptionServiceCycles;
        this.memoryNeeded = memoryNeeded;
        this.priority = priority;
        this.arrivalTime = arrivalTime;

        this.startTime = -1;   // aún no ha iniciado
        this.finishTime = -1;  // aún no ha terminado
        this.status = Status.NEW;
    }

    //  Ejecución del proceso 
    @Override
    public void run() {
        try {
            while (remainingInstructions > 0) {
                // Espera permiso del CPU
                canRun.acquire();

                // Cambia estado a RUNNING
                setStatus(Status.RUNNING);

                // Simula ejecución de una instrucción
                pc++;
                mar++;
                remainingInstructions--;

                System.out.println("[Proceso " + pid + "] Ejecutando instrucción " +
                        (totalInstructions - remainingInstructions) +
                        " | PC=" + pc + " | MAR=" + mar);

                // Simula duración del ciclo (tiempo de CPU)
                Thread.sleep(600);

                // Notifica al CPU que terminó esta instrucción
                done.release();
            }

            // Cuando termina todas las instrucciones
            setStatus(Status.TERMINATED);
            System.out.println("[Proceso " + pid + "] Finalizado.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    //  Control del hilo 
    public void start() {
        if (thread == null) {
            thread = new Thread(this, "Proceso-" + pid);
            thread.start();
        }
    }


    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public int getTotalInstructions() {
        return totalInstructions;
    }

    public int getMemoryNeeded() {
        return memoryNeeded;
    }

    public boolean isCpuBound() {
        return cpuBound;
    }

    public int getExceptionServiceCycles() {
        return exceptionServiceCycles;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setRemainingInstructions(int remainingInstructions) {
        this.remainingInstructions = remainingInstructions;
    }

    public int getRemainingInstructions() {
        return remainingInstructions;
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public int getMar() {
        return mar;
    }

    public void setMar(int mar) {
        this.mar = mar;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setCyclesToException(int cyclesToException) {
        this.cyclesToException = cyclesToException;
    }

    public int getCyclesToException() {
        return cyclesToException;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public Semaphore getCanRun() {
        return canRun;
    }

    public Semaphore getDone() {
        return done;
    }
}
