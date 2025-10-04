/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladorplanificador;

import DataStruct.Queue;
import DataStruct.LinkedList;
import Model.PCB;
/**
 *
 * @author vivia
 */
public class SimuladorPlanificador {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        Queue queue = new Queue();
//        for (int i = 0; i < 10; i++) {
//            queue.enqueue(i);
//        }
//        queue.print();
//        queue.dispatch();
//        System.out.println(" ----------------");
//        queue.print();
//        System.out.println("Hello Wold");
//        
//        LinkedList linkedlist = new LinkedList();
//        queue.print();
          PCB pcb = new PCB(1, "Proceso1", 50, true, 10, 5, 100, 2, 0);
          System.out.println(pcb.getName());
        
    }
    
}
