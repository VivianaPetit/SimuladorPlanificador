/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

/**
 *
 * @author Jose
 */
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class ThroughputChart extends JPanel {
    private DefaultCategoryDataset dataset;
    private int procesosCompletadosAnterior = 0;

    public ThroughputChart() {
        configurarPanel();
        crearGrafico();
    }

    private void configurarPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new java.awt.Dimension(650, 450));
    }

    private void crearGrafico() {
        dataset = new DefaultCategoryDataset();
        
        JFreeChart chart = ChartFactory.createLineChart(
            "Throughput - Procesos Completados por Tiempo",
            "Tiempo (ciclos)", 
            "Procesos Completados",
            dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 400));
        add(chartPanel, BorderLayout.CENTER);
    }

    public void actualizarGrafico(int ciclo, int procesosCompletados) {
        if (dataset != null) {
            dataset.addValue(procesosCompletados, "Throughput", "" + ciclo);
        }
    }
}
