package Scheduler;

import Model.PCB;
import DataStruct.Queue;

public class RR implements Scheduler {

    private final int quantum; // Quantum en número de instrucciones

    public RR(int quantum) {
        this.quantum = quantum;
    }

    public void addProcess(PCB process, Queue readyQueue) {
        process.setStatus(PCB.Status.READY);
        readyQueue.enqueue(process);
        System.out.println("[Scheduler RR] Proceso " + process.getPid() + " agregado a la cola.");
    }

    @Override
    public PCB nextProcess(Queue readyQueue) {
        if (!readyQueue.isEmpty()) {
            
            return (PCB) readyQueue.dispatch();
        }
        return null;
    }

    @Override
    public boolean hasReadyProcess(Queue readyQueue) {
        return !readyQueue.isEmpty();
    }

    public int getQuantum() {
        return quantum;
    }

    public void requeueIfNeeded(PCB process, Queue readyQueue) {
        if (process.getRemainingInstructions() > 0) {
            addProcess(process, readyQueue);
            System.out.println("[Scheduler RR] Proceso " + process.getPid() +
                    " reencolado con " + process.getRemainingInstructions() + " instrucciones restantes.");
        } else {
            process.setStatus(PCB.Status.TERMINATED);
            System.out.println("[Scheduler RR] Proceso " + process.getPid() + " terminó.");
        }
    }
}
