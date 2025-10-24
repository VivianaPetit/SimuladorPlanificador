
package Scheduler;

import Model.Process;
import DataStruct.Queue;

public class RR implements Scheduler {

    private final int quantum; // Quantum en número de instrucciones

    public RR(int quantum) {
        this.quantum = quantum;
    }

    public void addProcess(Process process, Queue readyQueue) {
        process.setStatus(Process.Status.READY);
        readyQueue.enqueue(process);
        System.out.println("[Scheduler RR] Proceso " + process.getPid() + " agregado a la cola.");
    }

    @Override
    public Process nextProcess(Queue readyQueue) {
        if (!readyQueue.isEmpty()) {
            
            return (Process) readyQueue.dispatch();
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

    public void requeueIfNeeded(Process process, Queue readyQueue) {
        if (process.getRemainingInstructions() > 0) {
            addProcess(process, readyQueue);
            System.out.println("[Scheduler RR] Proceso " + process.getPid() +
                    " reencolado con " + process.getRemainingInstructions() + " instrucciones restantes.");
        } else {
            process.setStatus(Process.Status.TERMINATED);
            System.out.println("[Scheduler RR] Proceso " + process.getPid() + " terminó.");
        }
    }
}
