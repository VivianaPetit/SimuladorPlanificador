/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author vivia
 */
public class Semaphore {
    private int permits;

    public Semaphore(int initialPermits) {
        this.permits = initialPermits;
    }

    // Adquirir un permiso (bloqueante)
    public synchronized void acquire() {
        while (permits == 0) {
            try {
                wait(); // espera hasta que haya un permiso disponible
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        permits--; // usa un permiso.
    }

    // Liberar un permiso
    public synchronized void release() {
        permits++;        // agrega un permiso
        notify();         // despierta a un hilo en espera
    }
}
