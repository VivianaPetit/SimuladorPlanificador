package Scheduler;

import Model.PCB;
import DataStruct.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * Planificador HRRN (Highest Response Ratio Next)
 * No expulsivo: el proceso elegido se ejecuta hasta completarse.
 */
public class HRRN implements Scheduler {

    private long currentTime;

    public HRRN() {
        this.currentTime = 0;
    }

    @Override
    public PCB nextProcess(Queue readyQueue) {
        if (readyQueue.isEmpty()) {
            System.out.println("[HRRN] No hay procesos listos.");
            return null; // evita NPE
        }

        // Copiar todos los procesos de la cola en una lista temporal
        List<PCB> procesos = new ArrayList<>();
        while (!readyQueue.isEmpty()) {
            procesos.add((PCB) readyQueue.dispatch());
        }

        PCB elegido = null;
        double maxRatio = -1;

        // Calcular Response Ratio (RR) para cada proceso READY que ya llegó
        for (PCB p : procesos) {
            if (p.getStatus() != PCB.Status.READY) continue; // solo READY
            if (p.getArrivalTime() > currentTime) continue; // aún no ha llegado

            int waitingTime = (int) Math.max(0, currentTime - p.getArrivalTime());
            int serviceTime = Math.max(1, p.getTotalInstructions()); // evita división por 0
            double responseRatio = 1.0 + ((double) waitingTime / serviceTime);

            System.out.println("[HRRN] Proceso " + p.getPid() +
                    " | W=" + waitingTime +
                    " | S=" + serviceTime +
                    " | R=" + String.format("%.2f", responseRatio));

            if (responseRatio > maxRatio) {
                maxRatio = responseRatio;
                elegido = p;
            }
        }

        // Reencolar todos los procesos que no se eligieron, o que no estaban READY
        for (PCB p : procesos) {
            if (p != elegido && p.getStatus() == PCB.Status.READY) {
                readyQueue.enqueue(p);
            }
        }

        // Imprimir solo si hay un proceso elegido
        if (elegido != null) {
            System.out.println("[Scheduler HRRN] Proceso " + elegido.getPid() +
                    " seleccionado con R=" + String.format("%.2f", maxRatio));
        } else {
            System.out.println("[Scheduler HRRN] Ningún proceso READY encontrado.");
        }

        return elegido;
    }

    @Override
    public boolean hasReadyProcess(Queue readyQueue) {
        // Considera solo procesos que sean READY y que hayan llegado
        if (readyQueue.isEmpty()) return false;

        Queue tempQueue = new Queue();
        boolean found = false;

        while (!readyQueue.isEmpty()) {
            PCB p = (PCB) readyQueue.dispatch();
            if (p.getStatus() == PCB.Status.READY && p.getArrivalTime() <= currentTime) {
                found = true;
            }
            tempQueue.enqueue(p);
        }

        // Restaurar readyQueue
        while (!tempQueue.isEmpty()) {
            readyQueue.enqueue(tempQueue.dispatch());
        }

        return found;
    }

    // Método para actualizar el tiempo global del scheduler
    public void updateTime(long time) {
        this.currentTime = time;
    }
}
