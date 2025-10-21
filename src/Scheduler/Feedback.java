package Scheduler;

import DataStruct.LinkedList;
import DataStruct.Queue;
import Model.PCB;

/**
 * Planificador Feedback Multinivel (MLFQ).
 * Cada nivel tiene su propio quantum. Los procesos se degradan al siguiente nivel
 * si no terminan dentro del quantum asignado.
 */
public class Feedback implements Scheduler {

    private final int[] quantums;           // Quantum por nivel
    private final int levels;                // Número de niveles
    private final LinkedList<Queue> queues; // Lista de colas de prioridad

    /**
     * Constructor
     * @param quantums arreglo con quantum por cada nivel (nivel 0 = mayor prioridad)
     */
    public Feedback(int[] quantums) {
        this.quantums = quantums;
        this.levels = quantums.length;
        this.queues = new LinkedList<>();

        // Crear las colas de cada nivel
        for (int i = 0; i < levels; i++) {
            queues.insertFinal(new Queue());
        }

        System.out.println("[Feedback] Inicializadas " + levels + " colas de prioridad.");
    }

    /**
     * Devuelve las colas internas
     */
    public LinkedList<Queue> getQueues() {
        return queues;
    }

    /**
     * Devuelve los quantums de cada nivel
     */
    public int[] getQuantums() {
        return quantums;
    }

    // =================== Métodos Scheduler ===================

    @Override
    public PCB nextProcess(Queue readyQueue) {
        // No se usa este método; la CPU llamará getNextProcess() directamente
        return null;
    }

    @Override
    public boolean hasReadyProcess(Queue readyQueue) {
        // No se usa; la CPU puede usar hasAnyReadyProcess(this)
        return false;
    }

    // =================== Funcionalidad Feedback ===================

    /**
     * Obtiene el siguiente proceso listo del nivel más alto disponible.
     * @return PCB listo o null si no hay procesos
     */
    public PCB getNextProcess() {
    for (int i = 0; i < levels; i++) {
        Queue q = queues.getElementGeneric(i);
        if (q != null && !q.isEmpty()) {
            PCB p = (PCB) q.dispatch();
            p.setCurrentLevel(i); // 👈 guardar nivel actual
            System.out.println("[Feedback] Proceso " + p.getPid() + " obtenido del nivel " + i);
            return p;
        }
    }
    return null;
}

    /**
     * Reencola un proceso en el siguiente nivel según su quantum.
     * Si terminó, se marca como TERMINATED.
     * @param p proceso a reencolar
     * @param currentLevel nivel actual
     */
//    public void requeueProcess(PCB p, int currentLevel) {
//        if (p.getRemainingInstructions() > 0) {
//            
//            int nextLevel = Math.min(currentLevel + 1, levels - 1); // degradación
//            p.setCurrentLevel(nextLevel);
//            Queue nextQueue = queues.getElementGeneric(nextLevel);
//            nextQueue.enqueue(p);
//            System.out.println("[Feedback] Reencolando proceso " + p.getPid() +
//                               " en nivel " + nextLevel +
//                               " | instrucciones restantes: " + p.getRemainingInstructions());
//        } else {
//            p.setStatus(PCB.Status.TERMINATED);
//            System.out.println("[Feedback] Proceso " + p.getPid() + " terminado.");
//        }
//    }
    public void requeueProcess(PCB p, int currentLevel) {
    if (p.getRemainingInstructions() > 0) {

        // 🔹 Asegurarse de que las colas están bien creadas
        if (queues == null || queues.getLenght() == 0) {
            System.err.println("[Feedback] ERROR: Las colas no están inicializadas correctamente.");
            return;
        }

        // 🔹 Calcular el nuevo nivel (degradación controlada)
        int nextLevel = Math.min(currentLevel + 1, levels - 1);
        p.setCurrentLevel(nextLevel);
        p.setStatus(PCB.Status.READY);

        Queue nextQueue = queues.getElementGeneric(nextLevel);
        if (nextQueue == null) {
            System.err.println("[Feedback] ERROR: Cola del nivel " + nextLevel + " es null.");
            return;
        }

        // 🔹 Evitar duplicados solo si ya existe en la cola destino
        if (!nextQueue.contains(p)) {
            nextQueue.enqueue(p);
            System.out.println("[Feedback] Reencolando proceso " + p.getPid() +
                               " en nivel " + nextLevel +
                               " | instrucciones restantes: " + p.getRemainingInstructions());
        } else {
            System.out.println("[Feedback] Proceso " + p.getPid() +
                               " ya está en la cola del nivel " + nextLevel + ", no se duplica.");
        }

    } else {
        p.setStatus(PCB.Status.TERMINATED);
        System.out.println("[Feedback] Proceso " + p.getPid() + " terminado.");
    }
}



    /**
     * Agrega un proceso nuevo al nivel más alto (nivel 0)
     * @param p proceso a agregar
     */
//    public void addNewProcess(PCB p) {
//        p.setStatus(PCB.Status.READY);
//        Queue q = queues.getElementGeneric(0);
//        q.enqueue(p);
//        System.out.println("[Feedback] Proceso " + p.getPid() + " agregado al nivel 0.");
//    }
    
    public void addNewProcess(PCB p) {
    // 🔹 Evitar duplicados en niveles inferiores
    for (int i = 0; i < levels; i++) {
        Queue q = queues.getElementGeneric(i);
        if (q != null) q.remove(p);
    }

    p.setStatus(PCB.Status.READY);
    p.setCurrentLevel(0);
    Queue q = queues.getElementGeneric(0);
    q.enqueue(p);

    System.out.println("[Feedback] Proceso " + p.getPid() + " agregado al nivel 0.");
}
}
