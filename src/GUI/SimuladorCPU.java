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
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 *
 * @author vivia
 */
public class SimuladorCPU extends javax.swing.JFrame {
    private CPU cpu;
    private Scheduler scheduler;
    public static LinkedList<Process> procesos;
    private javax.swing.Timer timer;
    private int tiempo = 0;
    private boolean corriendo = false;
    public static boolean simulacionActiva = false;
    private Thread cpuThread;
    private Thread uiThread;
    private ButtonGroup grupoPoliticas;
    private RendimientoCPU graficoFrame; 
    private ThroughputChart panelThroughput;
    
    
    private JTabbedPane tabbedPane;
    private RendimientoCPU panelUtilizacionCPU;
    private TiempoEsperaChart panelTiempoEspera;
    
    private int ciclosOcupados = 0;
    private int ultimoTiempo = 0;
  
    

    public SimuladorCPU() {
        procesos = new DataStruct.LinkedList<>();
        //this.procesos = procesosCargados; // guardamos la lista que viene de Carga
        initComponents();
        setVisible(true);
        
        colaListo.setLayout(new BoxLayout(colaListo, BoxLayout.X_AXIS));
        colaBloqueado.setLayout(new BoxLayout(colaBloqueado, BoxLayout.X_AXIS));
        colaTerminado.setLayout(new BoxLayout(colaTerminado, BoxLayout.X_AXIS));
        colaListoSuspendido.setLayout(new BoxLayout(colaListoSuspendido, BoxLayout.X_AXIS));
        colaBloqueadoSuspendido.setLayout(new BoxLayout(colaBloqueadoSuspendido, BoxLayout.X_AXIS));

        
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
        this.setSize(1380, 800);
        setLocationRelativeTo(null);
        setTitle("Simulador de Planificación de Procesos");
        panelRound2.setSize(1380, 800);
        setLayout(new BorderLayout(10,10)); 
        ms.setText("Cambiar duración de ciclo");
       
        // Configurar el planificador a usar
        scheduler = new FCFS(); 
        politica.setText(scheduler.getClass().getSimpleName());

        // Crear CPU con el scheduler
        cpu = new CPU(scheduler);
        
        //CPU.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        //CPU.setBackground(new Color(245, 245, 245));
        
        iniciarSimulacion();
        iniciarReloj();
        configurarTodasLasGraficas();

    }
    
    
    private void configurarGraficaUtilizacion() {
    // Crear el panel de la gráfica
    panelUtilizacionCPU = new RendimientoCPU();
    
    // Configurar el jPanel4 para contener la gráfica
    jPanel4.setLayout(new java.awt.BorderLayout());
    jPanel4.add(panelUtilizacionCPU, BorderLayout.CENTER);
    
    // Actualizar la interfaz
    jPanel4.revalidate();
    jPanel4.repaint();
}
    
    private void configurarTodasLasGraficas() {
    configurarGraficaUtilizacion();
    configurarGraficaThroughput();
    configurarGraficaTiempoEspera();
}
    
    private void configurarGraficaThroughput() {
    panelThroughput = new ThroughputChart();
    jPanel5.setLayout(new BorderLayout());  // jPanel5 es el de Throughput
    jPanel5.add(panelThroughput, BorderLayout.CENTER);
    jPanel5.revalidate();
    jPanel5.repaint();
}
    private void configurarGraficaTiempoEspera() {
    panelTiempoEspera = new TiempoEsperaChart();
    jPanel6.setLayout(new BorderLayout());  // jPanel6 es Tiempo de respuesta
    jPanel6.add(panelTiempoEspera, BorderLayout.CENTER);
    jPanel6.revalidate();
    jPanel6.repaint();
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
        
//        private void cambiarPlanificador(Scheduler nuevoScheduler) {
//            this.scheduler = nuevoScheduler;
//            cpu.setScheduler(nuevoScheduler);
//
//            String nombre = nuevoScheduler.getClass().getSimpleName();
//            actualizarLabelPolitica(nombre);
//
//            JOptionPane.showMessageDialog(this,
//                "Planificador cambiado a: " + nombre,
//                "Cambio de planificación",
//                JOptionPane.INFORMATION_MESSAGE
//            );
//            System.out.println("Planificador cambiado a: " + nombre);
//        }
        
        private void cambiarPlanificador(Scheduler nuevoScheduler) {
            // Llamamos al método de la CPU que maneja el cambio de scheduler y reencola los procesos
            cpu.cambiarScheduler(nuevoScheduler);

            // Actualizar el label de la política en la GUI
            actualizarLabelPolitica(nuevoScheduler.getClass().getSimpleName());

            JOptionPane.showMessageDialog(this,
                "Planificador cambiado a: " + nuevoScheduler.getClass().getSimpleName(),
                "Cambio de planificación",
                JOptionPane.INFORMATION_MESSAGE
            );

            System.out.println("Planificador cambiado a: " + nuevoScheduler.getClass().getSimpleName());
        }
        
        private double calcularTiempoEsperaPromedio() {
    LinkedList<Process> todosProcesos = cpu.obtenerTodosLosProcesos();
    int totalProcesos = 0;
    int sumaTiemposEspera = 0;
    
    for (int i = 0; i < todosProcesos.getLenght(); i++) {
        Process p = todosProcesos.getElementIn(i);
        if (p.getStatus() == Process.Status.TERMINATED && p.getCompletionTime() != -1) {
            // USAR completionTime en lugar de tiempo actual
            int tiempoEnSistema = p.getCompletionTime() - p.getArrivalTime();
            int tiempoServicio = p.getTotalInstructions();
            int tiempoEspera = tiempoEnSistema - tiempoServicio;
            
            sumaTiemposEspera += Math.max(0, tiempoEspera);
            totalProcesos++;
        }
    }
    
    return totalProcesos > 0 ? (double) sumaTiemposEspera / totalProcesos : 0.0;
}

        private void iniciarReloj() {
            if (corriendo) return;
            corriendo = true;

            timer = new javax.swing.Timer(CPU.cycleDurationMs, e -> {
                tiempo = (int) cpu.getCurrentTime();
                lblReloj.setText(String.valueOf(tiempo));
            });

            timer.start();
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
                            System.out.print("hola");
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
                    cpu.setLogListener(msg -> {
                            SwingUtilities.invokeLater(() -> {
                                jTextArea1.append(msg);
                                jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength()); // auto-scroll
                            });
                        });
                if (panelUtilizacionCPU != null) {
                    long tiempoCPU = cpu.getCurrentTime();
                    long ciclosOcupadosCPU = cpu.getBusyCycles();

                    double utilizacion = (tiempoCPU > 0) ? 
                        ((double) ciclosOcupadosCPU / tiempoCPU) * 100.0 : 0.0;



                    utilizacion = Math.min(utilizacion, 100.0);
                    panelUtilizacionCPU.actualizarGrafico((int) tiempoCPU, utilizacion);
                }

                if (panelThroughput != null) {
                    // Contar procesos terminados
                    int procesosCompletados = 0;
                    LinkedList<Process> todosProcesos = cpu.obtenerTodosLosProcesos();
                    for (int i = 0; i < todosProcesos.getLenght(); i++) {
                        Process p = todosProcesos.getElementIn(i);
                        if (p.getStatus() == Process.Status.TERMINATED) {
                            procesosCompletados++;
                        }
                    }
                    panelThroughput.actualizarGrafico((int)cpu.getCurrentTime(), procesosCompletados);
                }


                if (panelTiempoEspera != null) {
                    double tiempoEsperaPromedio = calcularTiempoEsperaPromedio();
                    panelTiempoEspera.actualizarGrafico((int)cpu.getCurrentTime(), tiempoEsperaPromedio);
                }


                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
            uiThread.start();
        }
        

       
        private JPanel crearTarjetaProceso(Process p) {
            JPanel tarjeta = new JPanel();
            tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
            tarjeta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1, true),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            tarjeta.setBackground(Color.WHITE);
            tarjeta.setMaximumSize(new Dimension(150, 120));
            tarjeta.setPreferredSize(new Dimension(150, 120));
            
            

            JLabel id = new JLabel("ID: " + p.getPid());
            JLabel nombre = new JLabel("Nombre: " + p.getName());
            JLabel estado = new JLabel(p.getStatus().toString());
            //JLabel totalInstrucciones = new JLabel("Instrucciones: " + p.getTotalInstructions());
            JLabel PC = new JLabel("PC: " + p.getPc());
            JLabel MAR = new JLabel("MAR: " + p.getMar());
            JLabel tipo;
            if (p.isCpuBound()) {
                tipo = new JLabel("Tipo: CPU Bound");
            } else {
                tipo = new JLabel("Tipo: I/O Bound");
            }
            
            // Centrar texto
            id.setAlignmentX(Component.CENTER_ALIGNMENT);
            nombre.setAlignmentX(Component.CENTER_ALIGNMENT);
            estado.setAlignmentX(Component.CENTER_ALIGNMENT);
            //totalInstrucciones.setAlignmentX(Component.CENTER_ALIGNMENT);
            PC.setAlignmentX(Component.CENTER_ALIGNMENT);
            MAR.setAlignmentX(Component.CENTER_ALIGNMENT);
            tipo.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Fuente y estilo
            id.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nombre.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            estado.setFont(new Font("Segoe UI", Font.BOLD, 12));
            estado.setForeground(new Color(60, 60, 60));

            tarjeta.add(id);
            tarjeta.add(nombre);
            tarjeta.add(estado);
            //tarjeta.add(totalInstrucciones);
            tarjeta.add(PC);
            tarjeta.add(MAR);
            tarjeta.add(tipo);

            // Colorear según estado
            switch (p.getStatus()) {
                //case RUNNING -> tarjeta.setBackground(new Color(210, 255, 210));
                case READY -> tarjeta.setBackground(new Color(210, 230, 255));
                case BLOCKED -> tarjeta.setBackground(new Color(255, 230, 190));
                case TERMINATED -> tarjeta.setBackground(new Color(220, 220, 220));
                case SUSPENDED_READY -> tarjeta.setBackground(new Color(153, 153, 255));
                case SUSPENDED_BLOCKED -> tarjeta.setBackground(new Color(255, 51, 51));
            }

            return tarjeta;
        }


    
    private void actualizarPaneles(LinkedList<Process> procesos) {
        // Limpiar paneles
        colaListo.removeAll();
        colaBloqueado.removeAll();
        colaTerminado.removeAll();
        colaListoSuspendido.removeAll();
        colaBloqueadoSuspendido.removeAll();
        
        // actualizar duracion de ciclo si aplica: 
        durationCycle.setText("" + (CPU.cycleDurationMs / 1000));
 
        Process procesoEnCPU = cpu.getCurrentProcess();
        
        // Agregar procesos a su panel correspondiente
        for (int i = 0; i < procesos.getLenght(); i++) {
            Process p = procesos.getElementIn(i);
            JPanel tarjeta = crearTarjetaProceso(p);
            
            // Si es el proceso activo en CPU, forzamos a mostrarlo ahí
            if (p == procesoEnCPU) {
                procesoejecucion.setText(procesoEnCPU.getName());
                id.setText("" + procesoEnCPU.getPid());
                pcCPU.setText("" + procesoEnCPU.getPc());
                marCPU.setText("" + procesoEnCPU.getMar());
                statusCPU.setText(procesoEnCPU.getStatus().toString());
                tipoCPU.setText(procesoEnCPU.isCpuBound() ? "CPU bound" : "I/O bound");
                continue;
            }
           

            switch (p.getStatus()) {
                case READY -> colaListo.add(tarjeta);
                case BLOCKED -> colaBloqueado.add(tarjeta);
                case TERMINATED -> colaTerminado.add(tarjeta);
                case SUSPENDED_READY -> colaListoSuspendido.add(tarjeta);
                case SUSPENDED_BLOCKED -> colaBloqueadoSuspendido.add(tarjeta);
            }
        }
        
        if (cpu.isSoEjecutando()) {
            procesoejecucion.setText("Sistema Operativo");
            id.setText("0");
            pcCPU.setText("" + cpu.getPc());
            marCPU.setText("" + cpu.getMar());
            statusCPU.setText(Process.Status.RUNNING.toString());
            tipoCPU.setText("CPU bound");
        } else if (procesoEnCPU != null) {
            // Si hay un proceso de usuario ejecutándose
            procesoejecucion.setText(procesoEnCPU.getName());
            id.setText("" + procesoEnCPU.getPid());
            pcCPU.setText("" + procesoEnCPU.getPc());
            marCPU.setText("" + procesoEnCPU.getMar());
            statusCPU.setText(procesoEnCPU.getStatus().toString());
            tipoCPU.setText(procesoEnCPU.isCpuBound() ? "CPU bound" : "I/O bound");
        } else {
            // Si no hay nada ejecutando
            procesoejecucion.setText("Proceso del sistema");
            id.setText("--");
            pcCPU.setText("--");
            marCPU.setText("--");
            statusCPU.setText("--");
            tipoCPU.setText("--");
        }
        
        cycles.setText(""+cpu.getTotalCycles());
        idle.setText("" + (cpu.getTotalCycles() - cpu.getBusyCycles()));
        busy.setText("" + cpu.getBusyCycles());

        // Actualizar interfaz
        colaListo.revalidate();
        colaListo.repaint();
        colaBloqueado.revalidate();
        colaBloqueado.repaint();
        colaTerminado.revalidate();
        colaTerminado.repaint();
        CPULabel.revalidate();
        CPULabel.repaint();
    }
    




    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelRound2 = new GUI.PanelRound();
        jScrollPane4 = new javax.swing.JScrollPane();
        colaListo = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        colaTerminado = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        colaBloqueado = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        colaListoSuspendido = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        colaBloqueadoSuspendido = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblReloj = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        politica = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        durationCycle = new javax.swing.JLabel();
        CPULabel = new javax.swing.JPanel();
        marCPU = new javax.swing.JLabel();
        statusCPU = new javax.swing.JLabel();
        pcCPU = new javax.swing.JLabel();
        procesoejecucion = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        busy = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        busyL = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        tipoCPU = new javax.swing.JLabel();
        cpuCiclosL = new javax.swing.JLabel();
        cycles = new javax.swing.JLabel();
        idleL = new javax.swing.JLabel();
        idle = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        id = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
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
        ms = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Simulador de Planificador de Procesos");
        setBackground(new java.awt.Color(51, 153, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMaximumSize(new java.awt.Dimension(1380, 700));
        setMinimumSize(new java.awt.Dimension(1380, 700));
        setPreferredSize(new java.awt.Dimension(1380, 700));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTabbedPane1.setName("Simulador"); // NOI18N

        panelRound2.setBackground(new java.awt.Color(255, 255, 255));
        panelRound2.setMaximumSize(new java.awt.Dimension(1380, 700));
        panelRound2.setMinimumSize(new java.awt.Dimension(1380, 700));
        panelRound2.setName(""); // NOI18N
        panelRound2.setPreferredSize(new java.awt.Dimension(1380, 700));
        panelRound2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane4.setBorder(null);

        colaListo.setBackground(new java.awt.Color(255, 255, 255));
        colaListo.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 153, 255), 3), "Cola de Listos", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(51, 153, 255))); // NOI18N

        javax.swing.GroupLayout colaListoLayout = new javax.swing.GroupLayout(colaListo);
        colaListo.setLayout(colaListoLayout);
        colaListoLayout.setHorizontalGroup(
            colaListoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1113, Short.MAX_VALUE)
        );
        colaListoLayout.setVerticalGroup(
            colaListoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 816, Short.MAX_VALUE)
        );

        jScrollPane4.setViewportView(colaListo);

        panelRound2.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 30, 490, 150));

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

        panelRound2.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 350, 480, 160));

        jScrollPane1.setBorder(null);

        colaBloqueado.setBackground(new java.awt.Color(255, 255, 255));
        colaBloqueado.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 102, 0), 3), "Cola de Bloqueados", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(255, 102, 0))); // NOI18N

        javax.swing.GroupLayout colaBloqueadoLayout = new javax.swing.GroupLayout(colaBloqueado);
        colaBloqueado.setLayout(colaBloqueadoLayout);
        colaBloqueadoLayout.setHorizontalGroup(
            colaBloqueadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 899, Short.MAX_VALUE)
        );
        colaBloqueadoLayout.setVerticalGroup(
            colaBloqueadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 587, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(colaBloqueado);

        panelRound2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 190, 490, 150));

        jScrollPane5.setBorder(null);

        colaListoSuspendido.setBackground(new java.awt.Color(255, 255, 255));
        colaListoSuspendido.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 255), 3), "Cola de Listos Suspendidos", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(153, 153, 255))); // NOI18N

        javax.swing.GroupLayout colaListoSuspendidoLayout = new javax.swing.GroupLayout(colaListoSuspendido);
        colaListoSuspendido.setLayout(colaListoSuspendidoLayout);
        colaListoSuspendidoLayout.setHorizontalGroup(
            colaListoSuspendidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 646, Short.MAX_VALUE)
        );
        colaListoSuspendidoLayout.setVerticalGroup(
            colaListoSuspendidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
        );

        jScrollPane5.setViewportView(colaListoSuspendido);

        panelRound2.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 30, 480, 150));

        jScrollPane6.setBorder(null);

        colaBloqueadoSuspendido.setBackground(new java.awt.Color(255, 255, 255));
        colaBloqueadoSuspendido.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 51, 51), 3), "Cola de Bloqueados Suspendidos", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(255, 51, 51))); // NOI18N

        javax.swing.GroupLayout colaBloqueadoSuspendidoLayout = new javax.swing.GroupLayout(colaBloqueadoSuspendido);
        colaBloqueadoSuspendido.setLayout(colaBloqueadoSuspendidoLayout);
        colaBloqueadoSuspendidoLayout.setHorizontalGroup(
            colaBloqueadoSuspendidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 646, Short.MAX_VALUE)
        );
        colaBloqueadoSuspendidoLayout.setVerticalGroup(
            colaBloqueadoSuspendidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
        );

        jScrollPane6.setViewportView(colaBloqueadoSuspendido);

        panelRound2.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 190, 480, 150));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel3.setText("CPU");
        panelRound2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 0, -1, -1));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblReloj.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblReloj.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblReloj.setText("0");
        jPanel1.add(lblReloj, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 40, 160, 30));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Duracion de un ciclo (segundos)");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, 220, 20));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Reloj Global");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 10, 110, -1));

        politica.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        politica.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        politica.setText("jLabel1");
        jPanel1.add(politica, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 80, 100, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Planificación:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 110, 20));

        durationCycle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        durationCycle.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        durationCycle.setText("jLabel1");
        jPanel1.add(durationCycle, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 120, 100, -1));

        panelRound2.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 350, 490, 160));

        CPULabel.setBackground(new java.awt.Color(255, 255, 255));
        CPULabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 204, 0), 3));
        CPULabel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        marCPU.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        marCPU.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        marCPU.setText("N/A");
        CPULabel.add(marCPU, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 170, 180, 30));

        statusCPU.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        statusCPU.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        statusCPU.setText("N/A");
        CPULabel.add(statusCPU, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 220, 180, 30));

        pcCPU.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pcCPU.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pcCPU.setText("N/A");
        CPULabel.add(pcCPU, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 120, 180, 30));

        procesoejecucion.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        procesoejecucion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        procesoejecucion.setText("N/A");
        CPULabel.add(procesoejecucion, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 40, 140, 30));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("ID");
        CPULabel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 40, 30));

        busy.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        busy.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        busy.setText("N/A");
        CPULabel.add(busy, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 590, 180, 30));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("Status");
        CPULabel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 220, 50, 30));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("MAR");
        CPULabel.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, 50, 30));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Proceso en ejecución:");
        CPULabel.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 150, 30));

        busyL.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        busyL.setText("Ocupado");
        CPULabel.add(busyL, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 590, 70, 30));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("Tipo");
        CPULabel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 270, 40, 30));

        tipoCPU.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tipoCPU.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tipoCPU.setText("N/A");
        CPULabel.add(tipoCPU, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 270, 180, 30));

        cpuCiclosL.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        cpuCiclosL.setText("Ciclos");
        CPULabel.add(cpuCiclosL, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 510, 40, 30));

        cycles.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cycles.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cycles.setText("N/A");
        CPULabel.add(cycles, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 510, 180, 30));

        idleL.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        idleL.setText("Idle");
        CPULabel.add(idleL, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 550, 40, 30));

        idle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        idle.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        idle.setText("N/A");
        CPULabel.add(idle, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 550, 180, 30));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("PC");
        CPULabel.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, 40, 30));

        id.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        id.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        id.setText("N/A");
        CPULabel.add(id, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 80, 180, 30));

        panelRound2.add(CPULabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 340, 640));

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(new java.awt.Color(255, 255, 255));
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("DialogInput", 0, 14)); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jScrollPane2.setViewportView(jTextArea1);

        panelRound2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 550, 890, 130));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Log");
        panelRound2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 520, 70, 30));

        jTabbedPane1.addTab("Simulador", panelRound2);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 668, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("Utilizacion del CPU", jPanel4);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 668, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("Throughput ", jPanel5);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 668, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab(" Tiempo de respuesta ", jPanel6);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 30, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 703, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 97, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Graficas", jPanel2);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
        jTabbedPane1.getAccessibleContext().setAccessibleName("Simulador");

        jMenu1.setText("Archivo");
        jMenu1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jMenu1MouseReleased(evt);
            }
        });

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

        ms.setText("jMenuItem4");
        ms.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                msMousePressed(evt);
            }
        });
        jMenu2.add(ms);

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

    private void jMenu1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu1MouseReleased
        
    }//GEN-LAST:event_jMenu1MouseReleased

    private void msMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msMousePressed
        // TODO add your handling code here:
        cycleDuration v3 = new cycleDuration();
        v3.setVisible(true);
        
    }//GEN-LAST:event_msMousePressed

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
    private javax.swing.JPanel CPULabel;
    private javax.swing.JRadioButtonMenuItem FCFS;
    private javax.swing.JRadioButtonMenuItem Feedback;
    private javax.swing.JRadioButtonMenuItem HRRN;
    private javax.swing.JRadioButtonMenuItem RR;
    private javax.swing.JRadioButtonMenuItem SPN;
    private javax.swing.JRadioButtonMenuItem SRT;
    private javax.swing.JLabel busy;
    private javax.swing.JLabel busyL;
    private javax.swing.JPanel colaBloqueado;
    private javax.swing.JPanel colaBloqueadoSuspendido;
    private javax.swing.JPanel colaListo;
    private javax.swing.JPanel colaListoSuspendido;
    private javax.swing.JPanel colaTerminado;
    private javax.swing.JLabel cpuCiclosL;
    private javax.swing.JLabel cycles;
    private javax.swing.JLabel durationCycle;
    private javax.swing.JLabel id;
    private javax.swing.JLabel idle;
    private javax.swing.JLabel idleL;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel lblReloj;
    private javax.swing.JLabel marCPU;
    private javax.swing.JMenu menupoliticas;
    private javax.swing.JMenuItem ms;
    private GUI.PanelRound panelRound2;
    private javax.swing.JLabel pcCPU;
    private javax.swing.JLabel politica;
    private javax.swing.JLabel procesoejecucion;
    private javax.swing.JLabel statusCPU;
    private javax.swing.JLabel tipoCPU;
    // End of variables declaration//GEN-END:variables
}
