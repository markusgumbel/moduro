/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2014, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ----------------
 * ChartViewer.java
 * ----------------
 * (C) Copyright 2014, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 27-Jun-2014 : Version 1 (DG);
 *
 */

package de.hs.mannheim.modUro.controller.diagram.fx;

import de.hs.mannheim.modUro.controller.diagram.BoxAndWhiskerPlotController;
import de.hs.mannheim.modUro.controller.diagram.fx.interaction.ChartMouseEventFX;
import de.hs.mannheim.modUro.controller.diagram.fx.interaction.ChartMouseListenerFX;
import de.hs.mannheim.modUro.model.StatisticValues;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.util.ExportUtils;
import org.jfree.chart.util.ParamChecks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A control for displaying a {@link JFreeChart} in JavaFX (embeds a
 * {@link ChartCanvas}, adds drag zooming and provides a popup menu for export
 * to PNG/JPG/SVG and PDF formats).  Many behaviours(tooltips, zooming etc) are
 * provided directly by the canvas.
 * <p>
 * <p>THE API FOR THIS CLASS IS SUBJECT TO CHANGE IN FUTURE RELEASES.  This is
 * so that we can incorporate feedback on the (new) JavaFX support in
 * JFreeChart.</p>
 *
 * @since 1.0.18
 */
public class ChartViewer extends Control implements Skinnable,
        ChartMouseListenerFX {

    /**
     * The chart to display.
     */
    private JFreeChart chart;

    /**
     * The context menu that will be attached to the canvas.
     */
    private ContextMenu contextMenu;

    /**
     * Does the viewer show tooltips from the chart?
     */
    private boolean tooltipEnabled;

    /**
     * Storage for registered chart mouse listeners.
     */
    private transient List<ChartMouseListenerFX> chartMouseListeners;

    // TODO make it generic:
    private BoxAndWhiskerPlotController controller;

    /**
     * Creates a new viewer to display the supplied chart in JavaFX.
     *
     * @param chart the chart ({@code null} not permitted).
     */
    public ChartViewer(JFreeChart chart) {
        this(chart, true, null);
    }

    public ChartViewer(JFreeChart chart, BoxAndWhiskerPlotController c) {
        this(chart, true, c);
    }

    /**
     * Creates a new viewer instance.
     *
     * @param chart              the chart ({@code null} not permitted).
     * @param contextMenuEnabled enable the context menu?
     */
    public ChartViewer(JFreeChart chart, boolean contextMenuEnabled,
                       BoxAndWhiskerPlotController c) {
        controller = c;
        ParamChecks.nullNotPermitted(chart, "chart");
        this.chart = chart;
        getStyleClass().add("chart-control");
        this.contextMenu = createContextMenu();
        this.contextMenu.setOnShowing((WindowEvent event) -> {
            ChartViewer.this.setTooltipEnabled(false);
        });
        this.contextMenu.setOnHiding((WindowEvent event) -> {
            ChartViewer.this.setTooltipEnabled(true);
        });
        setContextMenu(this.contextMenu);
        this.tooltipEnabled = true;
        this.chartMouseListeners = new ArrayList<ChartMouseListenerFX>();
    }

    @Override
    public String getUserAgentStylesheet() {
        return ChartViewer.class.getResource("/FXML/diagram/css/chart-viewer.css")
                .toExternalForm();
    }

    /**
     * Returns the chart that is being displayed by this node.
     *
     * @return The chart (never {@code null}).
     */
    public JFreeChart getChart() {
        return this.chart;
    }

    /**
     * Sets the chart to be displayed by this node.
     *
     * @param chart the chart ({@code null} not permitted).
     */
    public void setChart(JFreeChart chart) {
        ParamChecks.nullNotPermitted(chart, "chart");
        this.chart = chart;
        ChartViewerSkin skin = (ChartViewerSkin) getSkin();
        skin.setChart(chart);
    }

    /**
     * Returns the flag that controls whether or not tooltips are displayed
     * for the chart.
     *
     * @return A boolean.
     */
    public boolean isTooltipEnabled() {
        return this.tooltipEnabled;
    }

    /**
     * Sets the flag that controls whether or not the chart tooltips are shown
     * by this viewer.
     *
     * @param enabled the new flag value.
     */
    public void setTooltipEnabled(boolean enabled) {
        this.tooltipEnabled = enabled;
        ChartViewerSkin skin = (ChartViewerSkin) getSkin();
        if (skin != null) {
            skin.setTooltipEnabled(enabled);
        }
    }

    /**
     * Returns the rendering info from the most recent drawing of the chart.
     *
     * @return The rendering info (possibly {@code null}).
     */
    public ChartRenderingInfo getRenderingInfo() {
        ChartViewerSkin skin = (ChartViewerSkin) getSkin();
        if (skin != null) {
            return skin.getRenderingInfo();
        }
        return null;
    }

    /**
     * Hides the zoom rectangle.  The work is delegated to the control's
     * current skin.
     */
    public void hideZoomRectangle() {
        ChartViewerSkin skin = (ChartViewerSkin) getSkin();
        skin.setZoomRectangleVisible(false);
    }

    /**
     * Sets the size and location of the zoom rectangle and makes it visible
     * if it wasn't already visible.  The work is delegated to the control's
     * current skin.
     *
     * @param x the x-location.
     * @param y the y-location.
     * @param w the width.
     * @param h the height.
     */
    public void showZoomRectangle(double x, double y, double w, double h) {
        ChartViewerSkin skin = (ChartViewerSkin) getSkin();
        skin.showZoomRectangle(x, y, w, h);
    }

    /**
     * Registers a listener to receive {@link ChartMouseEvent} notifications
     * from this viewer.
     *
     * @param listener the listener ({@code null} not permitted).
     */
    public void addChartMouseListener(ChartMouseListenerFX listener) {
        ParamChecks.nullNotPermitted(listener, "listener");
        this.chartMouseListeners.add(listener);
    }

    /**
     * Removes a listener from the list of objects listening for chart mouse
     * events.
     *
     * @param listener the listener.
     */
    public void removeChartMouseListener(ChartMouseListenerFX listener) {
        ParamChecks.nullNotPermitted(listener, "listener");
        this.chartMouseListeners.remove(listener);
    }

    /**
     * Creates the context menu.
     *
     * @return The context menu.
     */
    private ContextMenu createContextMenu() {
        final ContextMenu menu = new ContextMenu();

        Menu export = new Menu("Export As");

        MenuItem pngItem = new MenuItem("PNG...");
        pngItem.setOnAction((ActionEvent e) -> {
            handleExportToPNG();
        });
        export.getItems().add(pngItem);

        MenuItem jpegItem = new MenuItem("JPEG...");
        jpegItem.setOnAction((ActionEvent e) -> {
            handleExportToJPEG();
        });
        export.getItems().add(jpegItem);

        MenuItem tikzItem = new MenuItem("Tikz ...");
        tikzItem.setOnAction((ActionEvent e) -> {
            handleExportToTikz();
        });
        export.getItems().add(tikzItem);

        if (ExportUtils.isOrsonPDFAvailable()) {
            MenuItem pdfItem = new MenuItem("PDF...");
            pdfItem.setOnAction((ActionEvent e) -> {
                handleExportToPDF();
            });
            export.getItems().add(pdfItem);
        }
        if (ExportUtils.isJFreeSVGAvailable()) {
            MenuItem svgItem = new MenuItem("SVG...");
            svgItem.setOnAction((ActionEvent e) -> {
                handleExportToSVG();
            });
            export.getItems().add(svgItem);
        }
        menu.getItems().add(export);
        return menu;
    }

    /**
     * A handler for the export to PDF option in the context menu.
     */
    private void handleExportToPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(
                "Portable Document Format (PDF)", "pdf"));
        fileChooser.setTitle("Export to PDF");
        File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        if (file != null) {
            ExportUtils.writeAsPDF(this.chart, (int) getWidth(),
                    (int) getHeight(), file);
        }
    }

    /**
     * A handler for the export to SVG option in the context menu.
     */
    private void handleExportToSVG() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to SVG");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(
                "Scalable Vector Graphics (SVG)", "svg"));
        File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        if (file != null) {
            ExportUtils.writeAsSVG(this.chart, (int) getWidth(),
                    (int) getHeight(), file);
        }
    }

    /**
     * A handler for the export to PNG option in the context menu.
     */
    private void handleExportToPNG() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to PNG");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(
                "Portable Network Graphics (PNG)", "png"));
        File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        if (file != null) {
            try {
                ExportUtils.writeAsPNG(this.chart, (int) getWidth(),
                        (int) getHeight(), file);
            } catch (IOException ex) {
                // FIXME: show a dialog with the error
            }
        }
    }

    /**
     * A handler for the export to JPEG option in the context menu.
     */
    private void handleExportToJPEG() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to JPEG");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(
                "JPEG", "jpg"));
        File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        if (file != null) {
            try {
                ExportUtils.writeAsJPEG(this.chart, (int) getWidth(),
                        (int) getHeight(), file);
            } catch (IOException ex) {
                // FIXME: show a dialog with the error
            }
        }
    }

    /**
     * A handler for the export to Tikz option in the context menu.
     */
    private void handleExportToTikz() {
        //TODO implementation is dependent of diagram content, not generic.
        Map<String, StatisticValues> stats = controller.stats;
        List<String> sortedModels = new ArrayList<>(stats.keySet());
        //sortedModels.sort(String::compareTo);
        sortedModels.sort((s1, s2) -> s2.compareTo(s1));
        String ytickslabels = "yticklabels={" +
                sortedModels.stream().map(Object::toString)
                        .collect(Collectors.joining(", ")) +
                "}";
        List<String> yticksl = new ArrayList<>();
        for (int i = 2; i <= sortedModels.size() + 1; i++) {
            yticksl.add(i + "");
        }
        String ytick = "ytick={" +
                yticksl.stream().map(Object::toString)
                        .collect(Collectors.joining(", ")) +
                "}";
        String enn = "enn"; // ok.map(m = > m._1.toString + " = " + m._2.toString).mkString(", ")
        String s = "% " + enn + "\n";
        s = s + "\\begin{tikzpicture}\n" +
                " \\begin{axis}[\n" +
                " " + ytick + ",\n" +
                " " + ytickslabels + ",\n " +
                " height=.3*\\textheight,\n" +
                " xmin=0, xmax=1.0, width=.9*\\textwidth,\n" +
                " xtick={0,.1,.2,.3,.4,.5,.6,.7,.8,.9,1},\n" +
                " xticklabels={0,.1,.2,.3,.4,.5,.6,.7,.8,.9,1}\n" +
                " ]\n";

        int pos = 2;
        for (String m : sortedModels) {
            s = s +
                    " \\addplot+[boxplot prepared={draw position=" + pos + ",\n" +
                    "  lower whisker=" + stats.get(m).getMin() +
                    ", lower quartile=" + stats.get(m).getFirstPercentile() + ",\n" +
                    "  median=" + stats.get(m).getSecondPercentile() + ", upper quartile=" +
                    stats.get(m).getLastPercentile() + ",\n" +
                    "  upper whisker=" + stats.get(m).getMax() + ",\n" +
                    "  every box/.style={draw=black},\n" +
                    "  every whisker/.style={black},\n" +
                    "  every median/.style={black}}]\n" +
                    " coordinates {};\n";
            pos++;
        }
        s = s + " \\end{axis}\n" +
                "\\end{tikzpicture}\n";
        System.out.println(s);
    }

    @Override
    public void chartMouseClicked(ChartMouseEventFX event) {
        // relay the event from the canvas to our registered listeners
        for (ChartMouseListenerFX listener : this.chartMouseListeners) {
            listener.chartMouseClicked(event);
        }
    }

    @Override
    public void chartMouseMoved(ChartMouseEventFX event) {
        // relay the event from the canvas to our registered listeners
        for (ChartMouseListenerFX listener : this.chartMouseListeners) {
            listener.chartMouseMoved(event);
        }
    }

}

