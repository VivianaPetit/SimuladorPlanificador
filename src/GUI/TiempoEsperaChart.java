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

public class TiempoEsperaChart extends JPanel {
    private DefaultCategoryDataset dataset;

    public TiempoEsperaChart() {
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
            "Tiempo de Espera Promedio por Proceso",
            "Tiempo (ciclos)", 
            "Tiempo de Espera",
            dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 400));
        add(chartPanel, BorderLayout.CENTER);
    }

    public void actualizarGrafico(int ciclo, double tiempoEsperaPromedio) {
        if (dataset != null) {
            dataset.addValue(tiempoEsperaPromedio, "Tiempo Espera", "" + ciclo);
        }
    }
}