package GUI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class RendimientoCPU extends JPanel {
    private DefaultCategoryDataset dataset;
    private ChartPanel chartPanel;

    public RendimientoCPU() {
        configurarPanel();
        crearGrafico();
    }

    private void configurarPanel() {
        // Configuración manual del panel
        setLayout(new BorderLayout());
        setPreferredSize(new java.awt.Dimension(650, 450));
    }

    private void crearGrafico() {
    dataset = new DefaultCategoryDataset();
    
    JFreeChart chart = ChartFactory.createLineChart(
        "Utilización del CPU (Acumulada)",  // ← CAMBIAR TÍTULO
        "Ciclos", 
        "Porcentaje Ocupado",
        dataset
    );

    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new java.awt.Dimension(600, 400));
    add(chartPanel, BorderLayout.CENTER);
}

    public void actualizarGrafico(int ciclo, double porcentaje) {
        if (dataset != null) {
            dataset.addValue(porcentaje, "CPU", "" + ciclo);
           
        }
    }
}