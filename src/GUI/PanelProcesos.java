/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

/**
 *
 * @author vivia
 */

import Model.PCB;
import javax.swing.*;
import java.awt.*;
import DataStruct.LinkedList;

public class PanelProcesos extends JPanel {
    Fuentes tipoFuente;
    private LinkedList procesos;

    public PanelProcesos(LinkedList procesos) {
        tipoFuente = new Fuentes();
        this.procesos = procesos;
        setBackground(Color.WHITE);
    }

    public void setProcesos(LinkedList procesos) {
        this.procesos = procesos;
    }
    
    public void actualizarProcesos(LinkedList<PCB> nuevosProcesos) {
    this.procesos = nuevosProcesos;
    repaint(); // Redibuja el panel
}


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (procesos == null || procesos.esVacio()) return;

        int x = 40; // posición inicial X
        int width = 25; // ancho de cada proceso
        int maxHeight = 200; // altura máxima (ajustable)

        for (int i = 0; i < procesos.getLenght(); i++) {
            // Color según estado
            PCB p = procesos.getElementIn(i);
            Color color;
            switch (p.getStatus()) {
                case READY: color = Color.BLUE; break;
                case RUNNING: color = Color.GREEN; break;
                case BLOCKED: color = Color.RED; break;
                case TERMINATED: color = Color.GRAY; break;
                case NEW:
                default: color = Color.WHITE; break;
            }
            

            // Altura proporcional al número de instrucciones
            int height = Math.max(30, Math.min(maxHeight, p.getTotalInstructions() * 5));
            int y = 40; 
            
            // Dibujar rectángulo
            g.setColor(color);
            g.fillRect(x, y, width, height);

            // Borde negro
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, height);

            // Etiqueta de nombre e instrucciones restantes
            g.setColor(Color.BLACK);
            g.setFont(tipoFuente.fuente(tipoFuente.nombre, 0, 12));
            g.drawString("P" + p.getPid(), x + 10, y - 5);
            g.drawString(p.getStatus().toString(), x - 5, y + height + 15);
            g.drawString(p.getRemainingInstructions() + "/" + p.getTotalInstructions(),
                         x, y + height + 30);

            x += width + 30; // espacio entre procesos
        }
    }
}

