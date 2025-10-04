/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladorplanificador;

import Model.PCB;
import CPU.CPU;
import Scheduler.Scheduler;   // 
import Scheduler.FCFS;        // 
import Scheduler.RR;        // 
import Scheduler.SPN;
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
        Scheduler scheduler = new SPN();

        // 2️⃣ Crear la CPU y pasarle el scheduler
        CPU cpu = new CPU(scheduler);

        // 3️⃣ Crear algunos procesos (PCB)
        PCB p1 = new PCB(1, "P1", 10, true, 0, 0, 100, 1, 0);
        PCB p2 = new PCB(2, "P2", 4, true, 0, 0, 100, 1, 0);
        PCB p3 = new PCB(3, "P3", 3, true, 0, 0, 100, 1, 0);

        // 4️⃣ Iniciar los hilos de los procesos
        p1.start();
        p2.start();
        p3.start();

        // 5️⃣ Agregarlos al scheduler
        scheduler.addProcess(p1);
        scheduler.addProcess(p2);
        scheduler.addProcess(p3);

        // 6️⃣ Iniciar el CPU 
        cpu.ejecutar();
        
        System.out.println("Uso del CPU: " + (cpu.getCpuUtilization() * 100) + "%");
        
        
        
    }

    
}
