package de.hs.mannheim.modUro.controller.diagram;

import de.hs.mannheim.modUro.controller.diagram.fx.ChartViewer;
import de.hs.mannheim.modUro.model.MetricType;
import de.hs.mannheim.modUro.model.Simulation;
import de.hs.mannheim.modUro.model.diagram.SimulationDiagram;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SimulationDiagramController controls SimulationDiagramView.
 * @author Mathuraa Pathmanathan (mathuraa@hotmail.de)
 */
public class SimulationDiagramController {

    //Reference to BoxAndWhiskerPlotModel
    private SimulationDiagram simulationDiagram;

    @FXML
    private BorderPane leftPane;
    @FXML
    private BorderPane rightPane;
    @FXML
    private ChoiceBox leftMetricType;
    @FXML
    private ChoiceBox rightMetricType;

    private static Integer leftLastSelectedIndex;
    private static Integer rightLastSelectedIndex;

    private static String leftLastSelectedMetrictypename;
    private static String rightLastSelectedMetrictypename;

    public void init(Simulation simulation){
        this.simulationDiagram = new SimulationDiagram(simulation);

        if(leftLastSelectedIndex == null || rightLastSelectedIndex == null) {
            initializeChoiceboxContent();
        } else {
            if(simulationContainsMetricType()) {
                setChoiceBoxContent();
                setLeftChartContent(leftLastSelectedIndex);
                setRightChartContent(rightLastSelectedIndex);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("Metrictype Warning");
                alert.setContentText("Simulation does not have Metrictype: " + leftLastSelectedMetrictypename);
                alert.showAndWait();

                initializeChoiceboxContent();
            }

        }


        /*ChangeListerners for selected items in choicebox.*/
       leftMetricType.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
           @Override
           public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
               setLeftChartContent(newValue.intValue());
               leftLastSelectedIndex = newValue.intValue();
               leftLastSelectedMetrictypename = choiceBoxMetrictypeNames().get(leftLastSelectedIndex);

           }
       });

        rightMetricType.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setRightChartContent(newValue.intValue());
                rightLastSelectedIndex = newValue.intValue();
                rightLastSelectedMetrictypename = choiceBoxMetrictypeNames().get(rightLastSelectedIndex);
            }
        });
    }

    /**
     * Checks if simultion has the last selected Metrictype from another simultion.
     * @return
     */
    private boolean simulationContainsMetricType() {
        boolean containsMetricType = false;
        List<String> name = choiceBoxMetrictypeNames();

        if(name.contains(leftLastSelectedMetrictypename) && name.contains(rightLastSelectedMetrictypename)) {
            containsMetricType = true;
        }

        return containsMetricType;
    }

    /**
     * Initializes Choicebox Content.
     */
    private void initializeChoiceboxContent() {
        List<String> name = choiceBoxMetrictypeNames();

        int left = 0;
        int right = 0;

        for (String val: name) {
            if(val.equals("FitnessArrangement")) {
                left = name.indexOf("FitnessArrangement");
            }

            if(val.equals("FitnessVolume")) {
                right = name.indexOf("FitnessVolume");
            }
        }

        leftMetricType.setItems(FXCollections.observableArrayList(name));
        rightMetricType.setItems(FXCollections.observableArrayList(name));

        leftMetricType.getSelectionModel().select(left);
        rightMetricType.getSelectionModel().select(right);

        leftLastSelectedIndex = left;
        rightLastSelectedIndex = right;

        leftLastSelectedMetrictypename = name.get(leftLastSelectedIndex);
        rightLastSelectedMetrictypename = name.get(rightLastSelectedIndex);

        setLeftChartContent(left);
        setRightChartContent(right);
    }

    private List<String> choiceBoxMetrictypeNames() {
        List<String> name = new ArrayList<>();
        for (MetricType metricTypeItem : simulationDiagram.getMetricTypes()) {
            name.add(metricTypeItem.getName());
        }

        return name;
    }

    /**
     * Sets Content of Choicebox.
     */
    private void setChoiceBoxContent(){
        List<String> name = choiceBoxMetrictypeNames();

        leftMetricType.setItems(FXCollections.observableArrayList(name));
        rightMetricType.setItems(FXCollections.observableArrayList(name));

        leftMetricType.getSelectionModel().select(leftLastSelectedIndex.intValue());
        rightMetricType.getSelectionModel().select(rightLastSelectedIndex.intValue());

    }

    /**
     * Sets left Chartcontent.
     * @param selectedItemIndex
     */
    private void setLeftChartContent(int selectedItemIndex){
        XYDataset dataset = createDataset(simulationDiagram.getSimulationName(), simulationDiagram.getMetricTypes().get(selectedItemIndex).getMetricData());
        JFreeChart chart = createChart(dataset, simulationDiagram.getMetricTypes().get(selectedItemIndex).getName());
        ChartViewer viewer = new ChartViewer(chart);
        leftPane.setCenter(viewer);
        leftPane.layout();
    }

    /**
     * Sets right Chartcontent.
     * @param selectedItemIndex
     */
    private void setRightChartContent(int selectedItemIndex){
        XYDataset rightDataset = createDataset(simulationDiagram.getSimulationName(), simulationDiagram.getMetricTypes().get(selectedItemIndex).getMetricData());
        JFreeChart rightChart = createChart(rightDataset, simulationDiagram.getMetricTypes().get(selectedItemIndex).getName());
        ChartViewer rightViewer = new ChartViewer(rightChart);
        rightPane.setCenter(rightViewer);
        rightPane.layout();
    }

    /**
     * Creates JFreeChart. XYLineDiagram.
     * @param dataset
     * @return
     */
    private static JFreeChart createChart(XYDataset dataset, String metricTypeName) {
        String title = metricTypeName;

        JFreeChart xyLineChart = ChartFactory.createXYLineChart(
                title,    // title
                "t",      // x-axis label
                "f",      // y-axis label
                dataset);

        String fontName = "Palatino";
        xyLineChart.getTitle().setFont(new Font(fontName, Font.BOLD, 18));

        XYPlot plot = (XYPlot) xyLineChart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.getDomainAxis().setLowerMargin(0.0);
        plot.getDomainAxis().setLabelFont(new Font(fontName, Font.BOLD, 14));
        plot.getDomainAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font(fontName, Font.BOLD, 14));
        plot.getRangeAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));
        xyLineChart.getLegend().setItemFont(new Font(fontName, Font.PLAIN, 14));
        xyLineChart.getLegend().setFrame(BlockBorder.NONE);
        xyLineChart.getLegend().setHorizontalAlignment(HorizontalAlignment.CENTER);
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(false);
            renderer.setDrawSeriesLineAsPath(true);
            // set the default stroke for all series
            renderer.setAutoPopulateSeriesStroke(false);
            renderer.setSeriesPaint(0, Color.RED);
            renderer.setSeriesPaint(1, new Color(24, 123, 58));
            renderer.setSeriesPaint(2, new Color(149, 201, 136));
            renderer.setSeriesPaint(3, new Color(1, 62, 29));
            renderer.setSeriesPaint(4, new Color(81, 176, 86));
            renderer.setSeriesPaint(5, new Color(0, 55, 122));
            renderer.setSeriesPaint(6, new Color(0, 92, 165));
        }

        return xyLineChart;

    }

    /**
     * Creates Dataset.
     * @return
     */
    private static XYDataset createDataset(String simulationName, double[][] fitnessArray) {

        XYSeries xySerie = new XYSeries(simulationName);
        double x;
        double y;

        for(int i = 0; i<fitnessArray.length; i++) {
            x =  fitnessArray[i][0];
            y =  fitnessArray[i][1];
            xySerie.add(x,y);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(xySerie);
        return dataset;
    }
}
