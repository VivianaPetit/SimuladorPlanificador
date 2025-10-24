/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import DataStruct.LinkedList;
import Model.CPU;
import Model.Process;
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
import java.util.Random;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
/**
 *
 * @author vivia
 */
public class SimuladorCPU extends javax.swing.JFrame {
    private CPU cpu;
    private Scheduler scheduler;
    public static LinkedList<Process> procesos;
    private Timer timer;
    private int tiempo = 0;
    private boolean corriendo = false;
    public static boolean simulacionActiva = false;
    private Thread cpuThread;
    private Thread uiThread;
    private ButtonGroup grupoPoliticas;
    
    

    public SimuladorCPU() {
        procesos = new DataStruct.LinkedList<>();
        //this.procesos = procesosCargados; // guardamos la lista que viene de Carga
        initComponents();
        
        iniciarReloj(1000);
        
        colaListo.setLayout(new BoxLayout(colaListo, BoxLayout.X_AXIS));
        colaBloqueado.setLayout(new BoxLayout(colaBloqueado, BoxLayout.X_AXIS));
        colaTerminado.setLayout(new BoxLayout(colaTerminado, BoxLayout.X_AXIS));
        CPU.setLayout(new BoxLayout(CPU, BoxLayout.X_AXIS));
        
        int[] quantums = {3, 6, 9}; // niveles de prioridad 
         
        FCFS.addActionListener(e -> cambiarPlanificador(new FCFS()));
        SPN.addActionListener(e -> cambiarPlanificador(new SPN()));
        SRT.addActionListener(e -> cambiarPlanificador(new SRT()));
        RR.addActionListener(e -> cambiarPlanificador(new RR(5))); 
        HRRN.addActionListener(e -> cambiarPlanificador(new HRRN()));
        Feedback.addActionListener(e -> cambiarPlanificador(new Feedback(quantums)));
        
        grupoPoliticas = new ButtonGroup();
        grupoPoliticas.add(FCFS);
        grupoPoliticas.add(SPN);
        grupoPoliticas.add(SRT);
        grupoPoliticas.add(RR);
        grupoPoliticas.add(HRRN);
        grupoPoliticas.add(Feedback);

        setResizable(false);
        setLocationRelativeTo(null);
        setTitle("Simulador de Planificación de Procesos");
        setSize(1055, 700);
        setLayout(new BorderLayout(10,10)); 
       
        // Configurar el planificador a usar
        scheduler = new FCFS(); 
        politica.setText(scheduler.getClass().getSimpleName());

        // Crear CPU con el scheduler
        cpu = new CPU(scheduler);

        setVisible(true);
        
        CPU.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 50));
        CPU.setBackground(new Color(245, 245, 245));
        
        iniciarSimulacion();

    }
    
        public static LinkedList<Process> generarProcesosAleatorios(int cantidad) {
            LinkedList<Process> procesos = new LinkedList<>();
            Random rand = new Random();

            for (int i = 1; i <= cantidad; i++) {
                int pid = i;
                String name = "Proceso_" + i;
                int totalInstructions = rand.nextInt(16) + 5; // entre 5 y 20
                boolean cpuBound = rand.nextBoolean();
                int cyclesToException = cpuBound ? 0 : rand.nextInt(totalInstructions / 2) + 1;
                int exceptionServiceCycles = (cyclesToException > 0) ? rand.nextInt(4) + 1 : 0;
                int memoryNeeded = rand.nextInt(151) + 50; // entre 50 y 200
                int priority = rand.nextInt(10) + 1;       // entre 1 y 10
                int arrivalTime = rand.nextInt(16);        // entre 0 y 15

                Process proceso = new Process(pid, name, totalInstructions, cpuBound,
                        cyclesToException, exceptionServiceCycles, memoryNeeded,
                        priority, arrivalTime);

                procesos.insertFinal(proceso);
        }

        return procesos;
    }
        
        private void actualizarLabelPolitica(String nombre){
            politica.setText(nombre);
        }
        
        private void cambiarPlanificador(Scheduler nuevoScheduler) {
            this.scheduler = nuevoScheduler;
            cpu.setScheduler(nuevoScheduler);

            String nombre = nuevoScheduler.getClass().getSimpleName();
            actualizarLabelPolitica(nombre);

            JOptionPane.showMessageDialog(this,
                "Planificador cambiado a: " + nombre,
                "Cambio de planificación",
                JOptionPane.INFORMATION_MESSAGE
            );
            System.out.println("Planificador cambiado a: " + nombre);
        }

        private void iniciarReloj(int intervaloMs) {
           if (corriendo) return;
           corriendo = true;
           timer = new Timer();

           timer.scheduleAtFixedRate(new TimerTask() {
               @Override
               public void run() {
                   tiempo++;
                   SwingUtilities.invokeLater(() -> lblReloj.setText("" + tiempo));
               }
           }, 0, intervaloMs);
       }

       private void detenerReloj() {
           if (timer != null) {
               timer.cancel();
               corriendo = false;
           }
       }
       


        private void iniciarSimulacion() {
            if (simulacionActiva) return; // evitar múltiples ejecuciones

            simulacionActiva = true;

            // Hilo CPU (simulación)
            cpuThread = new Thread(() -> {
                while (simulacionActiva) {
                    if (!procesos.esVacio()) {
                        // Agregar procesos nuevos a la CPU si no están aún
                        for (int i = 0; i < procesos.getLenght(); i++) {
                            Process p = procesos.getElementIn(i);
                            if (!cpu.contieneProceso(p)) { // debes implementar contieneProceso() o verificar con un flag
                                cpu.addProcessQueue(p);
                                p.start();
                            }
                        }

                        // Ejecutar la CPU (planificación)
                        cpu.ejecutar();
                    } else {
                        // Si no hay procesos, esperar un poco antes de revisar de nuevo
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            });
            cpuThread.start();

            // Hilo de actualización de interfaz
            uiThread = new Thread(() -> {
                while (simulacionActiva) {
                    LinkedList<Process> listaProcesos = cpu.obtenerTodosLosProcesos();
                    SwingUtilities.invokeLater(() -> actualizarPaneles(listaProcesos));

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
            uiThread.start();
        }
        
        private void detenerSimulacion() {
            simulacionActiva = false;
            if (cpuThread != null) cpuThread.interrupt();
            if (uiThread != null) uiThread.interrupt();
           // cpu.detenerEjecucion(); // porsia
        }

       
        private JPanel crearTarjetaProceso(Process p) {
    JPanel tarjeta = new JPanel();
    tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
    tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
    ));
    tarjeta.setBackground(Color.WHITE);
    tarjeta.setMaximumSize(new Dimension(150, 110));
    tarjeta.setPreferredSize(new Dimension(150, 110));

    JLabel id = new JLabel("ID: " + p.getPid());
    JLabel nombre = new JLabel("Nombre: " + p.getName());
    JLabel estado = new JLabel(p.getStatus().toString());
    JLabel totalInstrucciones = new JLabel("Instrucciones: " + p.getTotalInstructions());
    JLabel PC = new JLabel("PC: " + p.getPc());
    JLabel MAR = new JLabel("MAR: " + p.getMar());

    // Centrar texto
    id.setAlignmentX(Component.CENTER_ALIGNMENT);
    nombre.setAlignmentX(Component.CENTER_ALIGNMENT);
    estado.setAlignmentX(Component.CENTER_ALIGNMENT);
    totalInstrucciones.setAlignmentX(Component.CENTER_ALIGNMENT);
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
    tarjeta.add(totalInstrucciones);
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

    
    private void actualizarPaneles(LinkedList<Process> procesos) {
        // Limpiar paneles
        colaListo.removeAll();
        colaBloqueado.removeAll();
        colaTerminado.removeAll();
        CPU.removeAll();

        // Agregar procesos a su panel correspondiente
        for (int i = 0; i < procesos.getLenght(); i++) {
            Process p = procesos.getElementIn(i);
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
        jPanel1 = new javax.swing.JPanel();
        lblReloj = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        politica = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        menupoliticas = new javax.swing.JMenu();
        FCFS = new javax.swing.JRadioButtonMenuItem();
        SPN = new javax.swing.JRadioButtonMenuItem();
        SRT = new javax.swing.JRadioButtonMenuItem();
        HRRN = new javax.swing.JRadioButtonMenuItem();
        Feedback = new javax.swing.JRadioButtonMenuItem();
        RR = new javax.swing.JRadioButtonMenuItem();
        menuMs = new javax.swing.JMenu();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem3 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem4 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem5 = new javax.swing.JRadioButtonMenuItem();

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

        panelRound2.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 360, 670, 150));

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
            .addGap(0, 184, Short.MAX_VALUE)
        );

        panelRound2.add(CPU, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 50, 240, 190));

        jScrollPane1.setBorder(null);

        colaBloqueado.setBackground(new java.awt.Color(255, 255, 255));
        colaBloqueado.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 102, 0), 3), "Cola de Bloqueados", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(255, 102, 0))); // NOI18N

        javax.swing.GroupLayout colaBloqueadoLayout = new javax.swing.GroupLayout(colaBloqueado);
        colaBloqueado.setLayout(colaBloqueadoLayout);
        colaBloqueadoLayout.setHorizontalGroup(
            colaBloqueadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 863, Short.MAX_VALUE)
        );
        colaBloqueadoLayout.setVerticalGroup(
            colaBloqueadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 524, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(colaBloqueado);

        panelRound2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 200, 670, 150));

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

        panelRound2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, 670, 150));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblReloj.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblReloj.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblReloj.setText("0");
        jPanel1.add(lblReloj, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 60, 160, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Planificación:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 110, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("Reloj Global");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(73, 20, 110, -1));

        politica.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        politica.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        politica.setText("jLabel1");
        jPanel1.add(politica, new org.netbeans.lib.awtextra.AbsoluteConstraints(123, 120, 100, -1));

        panelRound2.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 290, 240, 310));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel3.setText("CPU");
        panelRound2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 10, -1, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GUI/Diseño sin título (2).png"))); // NOI18N
        jLabel1.setText("jLabel1");
        panelRound2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        getContentPane().add(panelRound2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1060, 700));

        jMenu1.setText("Archivo");

        jMenuItem2.setText("Nuevos procesos (aleatorios)");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem1.setText("Crear proceso");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem3.setText("Cargar archivo de procesos");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Opciones");

        menupoliticas.setText("Políticas de planificación");

        FCFS.setSelected(true);
        FCFS.setText("FCFS");
        FCFS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FCFSActionPerformed(evt);
            }
        });
        menupoliticas.add(FCFS);

        SPN.setText("SPN");
        SPN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SPNActionPerformed(evt);
            }
        });
        menupoliticas.add(SPN);

        SRT.setText("SRT");
        SRT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SRTActionPerformed(evt);
            }
        });
        menupoliticas.add(SRT);

        HRRN.setText("HRRN");
        HRRN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HRRNActionPerformed(evt);
            }
        });
        menupoliticas.add(HRRN);

        Feedback.setText("Feedback");
        Feedback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FeedbackActionPerformed(evt);
            }
        });
        menupoliticas.add(Feedback);

        RR.setText("Round Robin");
        RR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RRActionPerformed(evt);
            }
        });
        menupoliticas.add(RR);

        jMenu2.add(menupoliticas);

        menuMs.setText("Duración de ciclos (ms)");

        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText("200");
        menuMs.add(jRadioButtonMenuItem1);

        jRadioButtonMenuItem2.setSelected(true);
        jRadioButtonMenuItem2.setText("jRadioButtonMenuItem1");
        menuMs.add(jRadioButtonMenuItem2);

        jRadioButtonMenuItem3.setSelected(true);
        jRadioButtonMenuItem3.setText("jRadioButtonMenuItem1");
        menuMs.add(jRadioButtonMenuItem3);

        jRadioButtonMenuItem4.setSelected(true);
        jRadioButtonMenuItem4.setText("jRadioButtonMenuItem1");
        menuMs.add(jRadioButtonMenuItem4);

        jRadioButtonMenuItem5.setSelected(true);
        jRadioButtonMenuItem5.setText("jRadioButtonMenuItem1");
        menuMs.add(jRadioButtonMenuItem5);

        jMenu2.add(menuMs);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        AddNewProcess v = new AddNewProcess();
        v.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        procesos = generarProcesosAleatorios(5);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        Carga v2 = new Carga();
        v2.setVisible(true);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void FCFSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FCFSActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FCFSActionPerformed

    private void SPNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SPNActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SPNActionPerformed

    private void SRTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SRTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SRTActionPerformed

    private void HRRNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HRRNActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_HRRNActionPerformed

    private void FeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FeedbackActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FeedbackActionPerformed

    private void RRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RRActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_RRActionPerformed

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
    private javax.swing.JRadioButtonMenuItem FCFS;
    private javax.swing.JRadioButtonMenuItem Feedback;
    private javax.swing.JRadioButtonMenuItem HRRN;
    private javax.swing.JRadioButtonMenuItem RR;
    private javax.swing.JRadioButtonMenuItem SPN;
    private javax.swing.JRadioButtonMenuItem SRT;
    private javax.swing.JPanel colaBloqueado;
    private javax.swing.JPanel colaListo;
    private javax.swing.JPanel colaTerminado;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem3;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem4;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblReloj;
    private javax.swing.JMenu menuMs;
    private javax.swing.JMenu menupoliticas;
    private GUI.PanelRound panelRound2;
    private javax.swing.JLabel politica;
    // End of variables declaration//GEN-END:variables
}
