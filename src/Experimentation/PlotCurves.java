package Experimentation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.axis.NumberAxis;


import java.io.*;
import java.nio.file.*;

/**
    L'avantage de cette Api orienté au traitement de donné (très simple à utiliser) on la comparant à R (pour les Stat)
    C'est un exemple que j'ai piqué et amélioré un peu
    Ressources :
    https://adityanivas3.blogspot.com/2008/09/jfree-chart-tutorial.html
    http://www.if.pw.edu.pl/~ertman/pojava/?download=jfreechart_tutorial.pdf
*/
public class PlotCurves {

    private static final String INPUT_CSV = "out/results_avg.csv";
    private static final String OUTPUT_PNG = "out/performance_curves.png";

    public static void main(String[] args) throws Exception {

        XYSeries compression = new XYSeries("Temps de compression (ms)");
        XYSeries decompression = new XYSeries("Temps de décompression (ms)");

        // Lecture CSV
        try (BufferedReader br = Files.newBufferedReader(Paths.get(INPUT_CSV))) {
            String line = br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                long size = Long.parseLong(parts[0]);
                long avgComp = Long.parseLong(parts[1]);
                long avgDec = Long.parseLong(parts[2]);

                compression.add(size, avgComp);
                decompression.add(size, avgDec);
            }
        }

        // Création dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(compression);
        dataset.addSeries(decompression);

        // Création graphique
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Performances : Huffman dynamique",
                "Taille du fichier (octets)",
                "Temps (ms)",
                dataset
        );

        NumberAxis xAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();

        xAxis.setNumberFormatOverride(new java.text.NumberFormat() {
            @Override
            public StringBuffer format(double value, StringBuffer buffer, java.text.FieldPosition pos) {
                if (value >= 1_000_000_000) return buffer.append(String.format("%.1f Go", value / 1_000_000_000));
                if (value >= 1_000_000)     return buffer.append(String.format("%.1f Mo", value / 1_000_000));
                if (value >= 1_000)         return buffer.append(String.format("%.1f Ko", value / 1_000));
                return buffer.append((long)value + " o");
            }

            @Override
            public StringBuffer format(long value, StringBuffer buffer, java.text.FieldPosition pos) {
                return format((double) value, buffer, pos);
            }

            @Override
            public Number parse(String source, java.text.ParsePosition parsePosition) {
                return null; // Pas utile ici
            }
        });

        // Sauvegarde en PNG
        ChartUtils.saveChartAsPNG(new File(OUTPUT_PNG), chart, 900, 600);

        System.out.println("Courbes générées dans : " + OUTPUT_PNG);
    }
}

