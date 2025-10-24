package GUI;

import Model.Process;
import javax.swing.*;
import java.awt.*;
import DataStruct.LinkedList;

public class PanelProcesos extends JPanel {
    private Fuentes tipoFuente;
    private LinkedList<Process> procesos;

    public PanelProcesos(LinkedList<Process> procesos) {
        this.procesos = procesos;
        tipoFuente = new Fuentes();

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        crearSubpaneles();
    }

    public void actualizarProcesos(LinkedList<Process> nuevosProcesos) {
        this.procesos = nuevosProcesos;
        removeAll();
        crearSubpaneles();
        revalidate();
        repaint();
    }

    private void crearSubpaneles() {
        // Panel de colas izquierdo (contiene todas las secciones apiladas)
        JPanel panelColas = new JPanel();
        panelColas.setLayout(new BoxLayout(panelColas, BoxLayout.Y_AXIS));
        panelColas.setBackground(new Color(245, 245, 245));

        // Creamos los paneles contenedor (título + scroll interno)
        JPanel contReady = crearCategoriaConScroll("Cola de Listos", new Color(70, 130, 180));
        JPanel contBlocked = crearCategoriaConScroll("Cola de Bloqueados", new Color(255, 140, 0));
        JPanel contFinished = crearCategoriaConScroll("Cola de Terminados", new Color(128, 128, 128));
        JPanel contSuspendReady = crearCategoriaConScroll("Suspendido Listo", new Color(106, 90, 205));
        JPanel contSuspendBlocked = crearCategoriaConScroll("Suspendido Bloqueado", new Color(205, 92, 92));

        // Añadimos al panelColas en el orden deseado
        panelColas.add(contReady);
        panelColas.add(Box.createVerticalStrut(10));
        panelColas.add(contBlocked);
        panelColas.add(Box.createVerticalStrut(10));
        panelColas.add(contFinished);
        panelColas.add(Box.createVerticalStrut(10));
        panelColas.add(contSuspendReady);
        panelColas.add(Box.createVerticalStrut(10));
        panelColas.add(contSuspendBlocked);

        // Panel CPU (derecha) - COMPACTO
        JPanel panelCPU = crearPanelCategoria("CPU", new Color(34, 139, 34));
        panelCPU.setLayout(new BorderLayout());
        panelCPU.setPreferredSize(new Dimension(250, 200));
        panelCPU.setMaximumSize(new Dimension(250, 200));

        // Panel para proceso en ejecución (CPU)
        JPanel areaCPU = new JPanel(new BorderLayout());
        areaCPU.setOpaque(false);
        areaCPU.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelCPU.add(areaCPU, BorderLayout.NORTH);

        // Llenamos las filas internas con las tarjetas según el estado
        JPanel filaReady = (JPanel) contReady.getClientProperty("fila");
        JPanel filaBlocked = (JPanel) contBlocked.getClientProperty("fila");
        JPanel filaFinished = (JPanel) contFinished.getClientProperty("fila");
        JPanel filaSuspendReady = (JPanel) contSuspendReady.getClientProperty("fila");
        JPanel filaSuspendBlocked = (JPanel) contSuspendBlocked.getClientProperty("fila");

        // rellenar con procesos
        if (procesos != null && !procesos.esVacio()) {
            for (int i = 0; i < procesos.getLenght(); i++) {
                Process p = procesos.getElementIn(i);
                JPanel tarjeta = crearTarjetaProceso(p);

                switch (p.getStatus()) {
                    case RUNNING -> areaCPU.add(tarjeta, BorderLayout.NORTH);
                    case READY -> filaReady.add(tarjeta);
                    case BLOCKED -> filaBlocked.add(tarjeta);
                    case TERMINATED -> filaFinished.add(tarjeta);
                    case SUSPEND_READY -> filaSuspendReady.add(tarjeta);
                    case SUSPEND_BLOCKED -> filaSuspendBlocked.add(tarjeta);
                    default -> {}
                }
            }
        }
        
        // Añadir todo al layout principal
        add(Box.createHorizontalStrut(15));
        add(panelColas, BorderLayout.CENTER);
        add(panelCPU, BorderLayout.EAST);
    }

    private JPanel crearCategoriaConScroll(String titulo, Color colorTitulo) {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BorderLayout());
        contenedor.setBackground(Color.WHITE);
        contenedor.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(colorTitulo, 2),
                titulo,
                0,
                0,
                tipoFuente.fuente(tipoFuente.nombre, Font.BOLD, 14),
                colorTitulo
        ));
        contenedor.setPreferredSize(new Dimension(620, 140));
        contenedor.setMaximumSize(new Dimension(Short.MAX_VALUE, 140));

        JPanel fila = new JPanel();
        fila.setLayout(new BoxLayout(fila, BoxLayout.X_AXIS));
        fila.setBackground(Color.WHITE);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(Color.WHITE);
        wrapperPanel.add(fila, BorderLayout.CENTER);
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scroll = new JScrollPane(wrapperPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(600, 120));
        
        JScrollBar horizontalBar = scroll.getHorizontalScrollBar();
        horizontalBar.setUnitIncrement(20);
        horizontalBar.setBlockIncrement(100);

        scroll.setWheelScrollingEnabled(true);
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        contenedor.add(scroll, BorderLayout.CENTER);
        contenedor.putClientProperty("fila", fila);

        return contenedor;
    }

    private JPanel crearPanelCategoria(String titulo, Color colorTitulo) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(colorTitulo, 2),
                titulo,
                0,
                0,
                tipoFuente.fuente(tipoFuente.nombre, Font.BOLD, 14),
                colorTitulo
        ));
        return panel;
    }

    private JPanel crearTarjetaProceso(Process p) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        tarjeta.setBackground(new Color(250, 250, 250));
        
        tarjeta.setMaximumSize(new Dimension(160, 90));
        tarjeta.setPreferredSize(new Dimension(160, 90));
        tarjeta.setMinimumSize(new Dimension(160, 90));

        JLabel id = new JLabel("ID: " + p.getPid());
        JLabel nombre = new JLabel("Nombre: " + p.getName());
        JLabel status = new JLabel("Estado: " + p.getStatus());
        JLabel pc = new JLabel("PC: " + p.getPc());
        JLabel mar = new JLabel("MAR: " + p.getMar());

        id.setFont(tipoFuente.fuente(tipoFuente.nombre, Font.PLAIN, 12));
        nombre.setFont(tipoFuente.fuente(tipoFuente.nombre, Font.PLAIN, 12));
        status.setFont(tipoFuente.fuente(tipoFuente.nombre, Font.PLAIN, 12));
        pc.setFont(tipoFuente.fuente(tipoFuente.nombre, Font.PLAIN, 12));
        mar.setFont(tipoFuente.fuente(tipoFuente.nombre, Font.PLAIN, 12));

        switch (p.getStatus()) {
            case RUNNING -> tarjeta.setBackground(new Color(220, 255, 220));
            case READY -> tarjeta.setBackground(new Color(220, 230, 255));
            case BLOCKED -> tarjeta.setBackground(new Color(255, 240, 200));
            case TERMINATED -> tarjeta.setBackground(new Color(230, 230, 230));
            case SUSPEND_READY -> tarjeta.setBackground(new Color(216, 191, 216));
            case SUSPEND_BLOCKED -> tarjeta.setBackground(new Color(240, 128, 128));
        }

        tarjeta.add(id);
        tarjeta.add(nombre);
        tarjeta.add(status);
        tarjeta.add(pc);
        tarjeta.add(mar);
        tarjeta.add(Box.createRigidArea(new Dimension(8, 0)));

        return tarjeta;
    }
}