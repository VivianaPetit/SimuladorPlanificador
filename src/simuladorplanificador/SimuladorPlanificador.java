
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladorplanificador;

import Model.PCB;
import Model.CPU;
import Scheduler.Scheduler;   // 
import Scheduler.FCFS;        // 
import Scheduler.RR;        // 
import Scheduler.SPN;
import Scheduler.SRT;
import Scheduler.HRRN;
import Scheduler.Feedback;
/**
 *
 * @author vivia
 */
public class SimuladorPlanificador {

    /**
     * @param args the command line arguments
     */
   public static void main(String[] args) {
        // 1️⃣ Crear un scheduler (ejemplo: FCFS)
        int[] quantums = {3, 6, 9}; // niveles de prioridad
        Scheduler scheduler = new FCFS();

        // 2️⃣ Crear la CPU y pasarle el scheduler
        CPU cpu = new CPU(scheduler);

        // 3️⃣ Crear algunos procesos (PCB)
       // PCB p1 = new PCB(1, "P1", 10, false, 2, 6, 100, 1, 1);
        //PCB p2 = new PCB(2, "P2", 5, true, 0, 0, 100, 1, 20);
       // PCB p3 = new PCB(3, "P3", 6, true, 0, 0, 100, 1, 1);

        // 4️⃣ Iniciar los hilos de los procesos
        //p1.start();
        //p2.start();
        //p3.start();

        // 5️⃣ Agregarlos al scheduler
        // 🧵 Ejecutar el CPU en un hilo separado
        
        
    Thread cpuThread = new Thread(() -> {
    if (scheduler instanceof RR || scheduler instanceof SRT) {
        cpu.ejecutar(); // Expulsivo
    }else if (scheduler instanceof Feedback) {
    cpu.ejecutarFeedback(); // Expulsivo
}
    
    else {
        cpu.ejecutarSecuencial(); // No expulsivo
    }
});
    
    cpu.addProcessQueue(p1);
    cpu.addProcessQueue(p2);
    cpu.addProcessQueue(p3);
    
    cpuThread.start();
    
    



    // 🕒 Simular llegadas en distintos tiempos
//    try {
//        scheduler.addProcess(p1); // llega en t=0
//        Thread.sleep(1);        // espera medio segundo
//
//        scheduler.addProcess(p2); // llega en t=0.5s
//        Thread.sleep(1);        // espera otro medio segundo
//
//        scheduler.addProcess(p3); // llega en t=1.0s
//    } catch (InterruptedException e) {
//        e.printStackTrace();
//    }

  
        
        
    }

    
}
