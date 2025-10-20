/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import DataStruct.LinkedList;
import Model.CPU;
import Model.PCB;
import Scheduler.FCFS;
import Scheduler.Feedback;
import Scheduler.HRRN;
import Scheduler.RR;
import Scheduler.SPN;
import Scheduler.SRT;
import Scheduler.Scheduler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.*;
/**
 *
 * @author vivia
 */
public class SimuladorCPU extends javax.swing.JFrame {
    private PanelProcesos panelProcesos;
    private CPU cpu;
    private Scheduler scheduler;
    private LinkedList<PCB> procesos;
    
    

    public SimuladorCPU(LinkedList<PCB> procesosCargados) {
        this.procesos = procesosCargados; // guardamos la lista que viene de Carga
        initComponents();
        
        colaListo.setLayout(new BoxLayout(colaListo, BoxLayout.X_AXIS));
        colaBloqueado.setLayout(new BoxLayout(colaBloqueado, BoxLayout.X_AXIS));
        colaTerminado.setLayout(new BoxLayout(colaTerminado, BoxLayout.X_AXIS));
        CPU.setLayout(new BoxLayout(CPU, BoxLayout.X_AXIS));
        
        setResizable(false);
        setLocationRelativeTo(null);
        setTitle("Simulador de Planificación de Procesos");

        setSize(1055, 700);
        setLayout(new BorderLayout(10,10));
        


        

        this.setSize(1055, 700);
        setLayout(new BorderLayout());

        int[] quantums = {3, 6, 9}; // niveles de prioridad


        // Configurar el planificador a usar

        scheduler = new RR(5); 


        // Crear CPU con el scheduler
        cpu = new CPU(scheduler);

        setVisible(true);
        
        CPU.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 50));
        CPU.setBackground(new Color(245, 245, 245));

        
        iniciarSimulacion();
    }

    // Este método arranca toda la simulacion
    private void iniciarSimulacion() {
        // Crear procesos
        
 
//        procesos.insertFinal(new PCB(1, "A", 9, true, 2, 3, 10, 1, 0));
//        procesos.insertFinal(new PCB(2, "B", 5, true, 2, 2, 10, 2, 3));
//        procesos.insertFinal(new PCB(3, "C", 10, false, 5, 3, 10, 1, 0));
//        procesos.insertFinal(new PCB(4, "D", 12, false, 2, 6, 10, 2, 3));
//        procesos.insertFinal(new PCB(5, "E", 3, true, 2, 3, 10, 1, 0));
//        procesos.insertFinal(new PCB(6, "F", 2, true, 2, 2, 10, 2, 3));

        for (int i = 0; i < procesos.getLenght(); i++) {
            PCB p = procesos.getElementIn(i);
            cpu.addProcessQueue(p);
            p.start();
        }
        
        // Hilo CPU (simulación)
        Thread cpuThread = new Thread(() -> {
            cpu.ejecutar();
        });
        cpuThread.start();

        new Thread(() -> {
            while (true) {
                LinkedList<PCB> listaProcesos = cpu.obtenerProcesosTotales();
                SwingUtilities.invokeLater(() -> actualizarPaneles(listaProcesos));

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();

    }

    
        private JPanel crearTarjetaProceso(PCB p) {
    JPanel tarjeta = new JPanel();
    tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
    tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
    ));
    tarjeta.setBackground(Color.WHITE);
    tarjeta.setMaximumSize(new Dimension(150, 100));
    tarjeta.setPreferredSize(new Dimension(150, 100));

    JLabel id = new JLabel("ID: " + p.getPid());
    JLabel nombre = new JLabel("Nombre: " + p.getName());
    JLabel estado = new JLabel(p.getStatus().toString());
    JLabel PC = new JLabel("PC: " + p.getPc());
    JLabel MAR = new JLabel("MAR: " + p.getMar());

    // Centrar texto
    id.setAlignmentX(Component.CENTER_ALIGNMENT);
    nombre.setAlignmentX(Component.CENTER_ALIGNMENT);
    estado.setAlignmentX(Component.CENTER_ALIGNMENT);
    PC.setAlignmentX(Component.CENTER_ALIGNMENT);
    MAR.setAlignmentX(Component.CENTER_ALIGNMENT);

    // Fuente y estilo
    id.setFont(new Font("Segoe UI", Font.BOLD, 12));
    nombre.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    estado.setFont(new Font("Segoe UI", Font.BOLD, 12));
    estado.setForeground(new Color(60, 60, 60));

    tarjeta.add(id);
    tarjeta.add(nombre);
    tarjeta.add(estado);
    tarjeta.add(PC);
    tarjeta.add(MAR);

    // Colorear según estado
    switch (p.getStatus()) {
        case RUNNING -> tarjeta.setBackground(new Color(210, 255, 210));
        case READY -> tarjeta.setBackground(new Color(210, 230, 255));
        case BLOCKED -> tarjeta.setBackground(new Color(255, 230, 190));
        case TERMINATED -> tarjeta.setBackground(new Color(220, 220, 220));
    }

    return tarjeta;
}

    
    private void actualizarPaneles(LinkedList<PCB> procesos) {
        // Limpiar paneles
        colaListo.removeAll();
        colaBloqueado.removeAll();
        colaTerminado.removeAll();
        CPU.removeAll();

        // Agregar procesos a su panel correspondiente
        for (int i = 0; i < procesos.getLenght(); i++) {
            PCB p = procesos.getElementIn(i);
            JPanel tarjeta = crearTarjetaProceso(p);

            switch (p.getStatus()) {
                case READY -> colaListo.add(tarjeta);
                case BLOCKED -> colaBloqueado.add(tarjeta);
                case TERMINATED -> colaTerminado.add(tarjeta);
                case RUNNING -> CPU.add(tarjeta);
            }
        }

        // Actualizar interfaz
        colaListo.revalidate();
        colaListo.repaint();
        colaBloqueado.revalidate();
        colaBloqueado.repaint();
        colaTerminado.revalidate();
        colaTerminado.repaint();
        CPU.revalidate();
        CPU.repaint();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelRound2 = new GUI.PanelRound();
        jScrollPane3 = new javax.swing.JScrollPane();
        colaTerminado = new javax.swing.JPanel();
        CPU = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        colaBloqueado = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        colaListo = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Simulador de Planificador de Procesos");
        setBackground(new java.awt.Color(51, 153, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelRound2.setBackground(new java.awt.Color(255, 255, 255));
        panelRound2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane3.setBorder(null);

        colaTerminado.setBackground(new java.awt.Color(255, 255, 255));
        colaTerminado.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153), 3), "Cola de Terminados", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(153, 153, 153))); // NOI18N

        javax.swing.GroupLayout colaTerminadoLayout = new javax.swing.GroupLayout(colaTerminado);
        colaTerminado.setLayout(colaTerminadoLayout);
        colaTerminadoLayout.setHorizontalGroup(
            colaTerminadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1122, Short.MAX_VALUE)
        );
        colaTerminadoLayout.setVerticalGroup(
            colaTerminadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 966, Short.MAX_VALUE)
        );

        jScrollPane3.setViewportView(colaTerminado);

        panelRound2.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 350, 670, 140));

        CPU.setBackground(new java.awt.Color(255, 255, 255));
        CPU.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 204, 0), 3));

        javax.swing.GroupLayout CPULayout = new javax.swing.GroupLayout(CPU);
        CPU.setLayout(CPULayout);
        CPULayout.setHorizontalGroup(
            CPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 234, Short.MAX_VALUE)
        );
        CPULayout.setVerticalGroup(
            CPULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 214, Short.MAX_VALUE)
        );

        panelRound2.add(CPU, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 50, 240, 220));

        jScrollPane1.setBorder(null);

        colaBloqueado.setBackground(new java.awt.Color(255, 255, 255));
        colaBloqueado.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 102, 0), 3), "Cola de Bloqueados", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(255, 102, 0))); // NOI18N

        javax.swing.GroupLayout colaBloqueadoLayout = new javax.swing.GroupLayout(colaBloqueado);
        colaBloqueado.setLayout(colaBloqueadoLayout);
        colaBloqueadoLayout.setHorizontalGroup(
            colaBloqueadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 646, Short.MAX_VALUE)
        );
        colaBloqueadoLayout.setVerticalGroup(
            colaBloqueadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(colaBloqueado);

        panelRound2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 200, 670, 140));

        jScrollPane2.setBorder(null);

        colaListo.setBackground(new java.awt.Color(255, 255, 255));
        colaListo.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 153, 255), 3), "Cola de Listos", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(51, 153, 255))); // NOI18N

        javax.swing.GroupLayout colaListoLayout = new javax.swing.GroupLayout(colaListo);
        colaListo.setLayout(colaListoLayout);
        colaListoLayout.setHorizontalGroup(
            colaListoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 646, Short.MAX_VALUE)
        );
        colaListoLayout.setVerticalGroup(
            colaListoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(colaListo);

        panelRound2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, 670, 140));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel3.setText("CPU");
        panelRound2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 10, -1, -1));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GUI/Diseño sin título (2).png"))); // NOI18N
        panelRound2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 700));

        getContentPane().add(panelRound2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1060, 700));

        jMenu1.setText("Archivo");

        jMenuItem1.setText("Crear Proceso");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Configuración");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        AddNewProcess v = new AddNewProcess();
        v.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SimuladorCPU.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SimuladorCPU.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SimuladorCPU.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SimuladorCPU.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                 SwingUtilities.invokeLater(() -> new SimuladorCPU().setVisible(true));
//            }
//        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CPU;
    private javax.swing.JPanel colaBloqueado;
    private javax.swing.JPanel colaListo;
    private javax.swing.JPanel colaTerminado;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private GUI.PanelRound panelRound2;
    // End of variables declaration//GEN-END:variables
}
