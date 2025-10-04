package Scheduler;

import Model.PCB;
import DataStruct.Queue;

public class RR implements Scheduler {

    private Queue readyQueue;
    private final int quantum; // Quantum en número de instrucciones

    public RR(int quantum) {
        this.readyQueue = new Queue();
        this.quantum = quantum;
    }

    @Override
    public void addProcess(PCB process) {
        process.setStatus(PCB.Status.READY);
        readyQueue.enqueue(process);
        System.out.println("[Scheduler RR] Proceso " + process.getPid() + " agregado a la cola.");
    }

    @Override
    public PCB nextProcess() {
        if (!readyQueue.isEmpty()) {
            return (PCB) readyQueue.dispatch();
        }
        return null;
    }

    @Override
    public boolean hasReadyProcess() {
        return !readyQueue.isEmpty();
    }

    public int getQuantum() {
        return quantum;
    }

    public void requeueIfNeeded(PCB process) {
        if (process.getRemainingInstructions() > 0) {
            addProcess(process);
            System.out.println("[Scheduler RR] Proceso " + process.getPid() +
                    " reencolado con " + process.getRemainingInstructions() + " instrucciones restantes.");
        } else {
            process.setStatus(PCB.Status.TERMINATED);
            System.out.println("[Scheduler RR] Proceso " + process.getPid() + " terminó.");
        }
    }
}
