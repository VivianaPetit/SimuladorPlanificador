package Scheduler;

import Model.Process;
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
    public Process nextProcess(Queue readyQueue) {
        if (readyQueue.isEmpty()) {
            System.out.println("[HRRN] No hay procesos listos.");
            return null; // evita NPE
        }

        // Copiar todos los procesos de la cola en una lista temporal
        List<Process> procesos = new ArrayList<>();
        while (!readyQueue.isEmpty()) {
            procesos.add((Process) readyQueue.dispatch());
        }

        Process elegido = null;
        double maxRatio = -1;

        // Calcular Response Ratio (RR) para cada proceso READY que ya llegó
        for (Process p : procesos) {
            if (p.getStatus() != Process.Status.READY) continue; // solo READY
            if (p.getArrivalTime() > currentTime) continue; // aún no ha llegado

            int waitingTime = (int) Math.max(0, currentTime - p.getArrivalTime());
            int serviceTime = Math.max(1, p.getTotalInstructions()); // evita división por 0
            double responseRatio = 1.0 + ((double) waitingTime / serviceTime);

            System.out.println("[HRRN] Proceso " + p.getPid() +
                    " | W=" + waitingTime +
                    " | S=" + serviceTime +
                    " | R=" + String.format("%.2f", responseRatio));

            if (responseRatio > maxRatio) {
    // 1. Gana si la relación de respuesta es estrictamente mayor
    maxRatio = responseRatio;
    elegido = p;
} else if (responseRatio == maxRatio) {
    // 2. Desempate: Gana si la relación es igual Y llegó antes (FCFS)
    if (elegido == null || p.getArrivalTime() < elegido.getArrivalTime()) {
        maxRatio = responseRatio;
        elegido = p;
    }
}
        }

        // Reencolar todos los procesos que no se eligieron, o que no estaban READY
        for (Process p : procesos) {
            if (p != elegido && p.getStatus() == Process.Status.READY) {
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
            Process p = (Process) readyQueue.dispatch();
            if (p.getStatus() == Process.Status.READY && p.getArrivalTime() <= currentTime) {
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
