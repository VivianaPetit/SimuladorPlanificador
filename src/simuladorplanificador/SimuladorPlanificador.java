
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
import GUI.Carga;
import GUI.SimuladorCPU;
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
        new SimuladorCPU().setVisible(true);
        int[] quantums = {3, 6, 9}; // niveles de prioridad
        Scheduler scheduler = new FCFS();


        // 2️⃣ Crear la CPU y pasarle el scheduler
        CPU cpu = new CPU(scheduler);
        
        
        Thread cpuThread = new Thread(() -> {
            cpu.ejecutar();
        });

        cpuThread.start();       
    }

    
}
