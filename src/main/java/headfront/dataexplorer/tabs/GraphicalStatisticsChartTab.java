package headfront.dataexplorer.tabs;

import headfront.amps.services.AmpsStatsLoader;
import headfront.amps.services.TopicService;
import headfront.convertor.JacksonJsonConvertor;
import headfront.dataexplorer.tabs.statistics.StatsTimeSeriesDataChooser;
import headfront.guiwidgets.PopUpDialog;
import headfront.utils.MessageUtil;
import headfront.utils.StringUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.control.MaskerPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Deepak on 25/10/2016.
 */
public class GraphicalStatisticsChartTab extends Tab {

    private static final Logger LOG = LoggerFactory.getLogger(GraphicalStatisticsChartTab.class);
    private ObservableList<TreeItem<String>> stats = null;
    private StatsTimeSeriesDataChooser chooser;
    private boolean useSecureHttp;
    private JacksonJsonConvertor jsonConvertor = new JacksonJsonConvertor();
    private ChartPanel chartPanel;
    private String connectionsStr;
    private String adminPortStr;
    private MaskerPane maskerPane = new MaskerPane();
    private boolean sendUpdate;
    private Consumer<Integer> recordCountListener = s -> {
    };
    private int recordCount;

    public GraphicalStatisticsChartTab(String connectionsStr, String adminPortStr,
                                       TopicService topicService, Stage mainStage,
                                       Runnable reloadTreeListener, boolean useSecureHttp) {
        super("Graphical Stats");
        this.connectionsStr = connectionsStr;
        this.adminPortStr = adminPortStr;
        chooser = new StatsTimeSeriesDataChooser(false, reloadTreeListener);
        this.useSecureHttp = useSecureHttp;
        chooser.setTopicService(topicService);
        chooser.initOwner(mainStage);
        BorderPane pane = new BorderPane();
        Button showChooserButton = new Button("Show Stats Chooser");
        showChooserButton.setOnAction(e -> {
            chooser.showAndWait();
            sendRecordCount(0);
            if (chooser.isUserSelectedValidSettings()) {
                stats = chooser.getAllStats();
                ArrayList<String> data = new ArrayList<>();
                stats.forEach(treeNode -> {
                    data.add(StringUtils.getFullTreePath(treeNode));
                });
                updateGraph();
            }
        });
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                startSendingUpdates();
            } else {
                stopSendingUpdate();
            }
        });
        BorderPane topPanel = new BorderPane();
        topPanel.setPadding(new Insets(5, 5, 5, 5));
        topPanel.setCenter(showChooserButton);
        pane.setTop(topPanel);
        final SwingNode chartSwingNode = new SwingNode();
        chartPanel = new ChartPanel(null);
        chartSwingNode.setContent(chartPanel);
        pane.setCenter(chartSwingNode);
        StackPane mainPanelWithMasker = new StackPane();
        maskerPane.setText("JetFuel is calculating Graphical TimeSeries.");
        maskerPane.setVisible(false);
        mainPanelWithMasker.getChildren().addAll(pane, maskerPane);
        setContent(mainPanelWithMasker);
    }

    private void stopSendingUpdate() {
        sendUpdate = false;
    }

    private void startSendingUpdates() {
        sendUpdate = true;
        recordCountListener.accept(recordCount);
    }

    protected void sendRecordCount(int size) {
        recordCount = size;
        if (sendUpdate) {
            recordCountListener.accept(size);
        }
    }

    public void setRecordCountListener(Consumer<Integer> recordCountListener) {
        this.recordCountListener = recordCountListener;
    }

    private void updateGraph() {
        ObservableList<TreeItem<String>> statsToGather = chooser.getAllStats();
        CountDownLatch coundownLatch = new CountDownLatch(statsToGather.size());
        Map<TreeItem, String> data = new ConcurrentHashMap<>();
        maskerPane.setVisible(true);
        statsToGather.forEach(stats -> {
                    Runnable r = () -> {
                        new AmpsStatsLoader(connectionsStr, adminPortStr, json -> {
                            if (json != null) {
                                data.put(stats, json);
                            } else {
                                PopUpDialog.showWarningPopup("Did not receive data", "Did not receive data for " + StringUtils.getFullTreePath(stats) + ". Just showing the data we got.");
                            }
                            coundownLatch.countDown();
                        }, 0, stats, chooser.getStartDate(), chooser.getToDate(), useSecureHttp);
                    };
                    new Thread(r).start();
                }
        );
        Runnable waitForResponse = () -> {
            try {
                boolean await = coundownLatch.await(20, TimeUnit.SECONDS);
                final JFreeChart chart = createChart(data);
                Platform.runLater(() -> {
                    chartPanel.setChart(chart);
                    sendRecordCount(data.size());
                    maskerPane.setVisible(false);
                    if (!await) {
                        PopUpDialog.showWarningPopup("Did not receive all the data", "Did not receive all the data from amps with the allocated time. Just showing the data we got.");
                    }
                });
            } catch (InterruptedException e) {
                LOG.warn("InterruptedException thrown while geting graphical stats", e);
            }
        };
        new Thread(waitForResponse).start();
    }

    private JFreeChart createChart(Map<TreeItem, String> data) {
        final XYDataset dataset = createDataset(data);
        final JFreeChart chart = createChart(dataset);
        return chart;
    }

    private TimeSeries createXYSeries(TreeItem treeNodeItem, String json) {
        String name = StringUtils.getFullTreePath(treeNodeItem);
        TimeSeries series1 = new TimeSeries(name, Second.class);
        Map<String, Object> data = jsonConvertor.convertToMap(json);
        data.forEach((key, value) -> {
            Object dataToProcess = MessageUtil.getLeafNode(data, treeNodeItem.getValue().toString());
            if (dataToProcess instanceof java.util.List) {
                java.util.List listToProcess = (java.util.List) dataToProcess;
                try {
                    listToProcess.forEach(m -> {
                        Map<String, Object> dataValues = (Map) m;
                        String timestamp = dataValues.get("timestamp").toString();
                        Object valueData = dataValues.get("value");
                        Double doubleValue = Double.parseDouble(valueData.toString());
                        int year = Integer.parseInt(timestamp.substring(0, 4));
                        int month = Integer.parseInt(timestamp.substring(4, 6));
                        int date = Integer.parseInt(timestamp.substring(6, 8));
                        int hour = Integer.parseInt(timestamp.substring(9, 11));
                        int min = Integer.parseInt(timestamp.substring(11, 13));
                        int sec = Integer.parseInt(timestamp.substring(13, 15));
                        series1.add(new Second(sec, min, hour, date, month, year), doubleValue);
                    });
                } catch (Exception e) {
                    PopUpDialog.showWarningPopup("Invalid data", name + " cant be charted as its not a number");
                    LOG.warn("Could not process " + name, e);
                }
            }
        });
        return series1;
    }

    private XYDataset createDataset(Map<TreeItem, String> data) {
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        data.forEach((key, value) -> {
            dataset.addSeries(createXYSeries(key, value));
        });
        return dataset;
    }

    private JFreeChart createChart(final XYDataset dataset) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "JetFuel TimeSeries. From " + chooser.getStartDate() + " To " + chooser.getToDate(),
                "Date Time",
                "Value",
                dataset,
                true,
                true,
                false
        );

//        chart.setBackgroundPaint(Color.white);
        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.black);
        //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        final DateAxis xaxis = (DateAxis) plot.getDomainAxis();
//        xaxis.setLabelAngle(Math.PI / 4.0);
        xaxis.setVerticalTickLabels(true);
        xaxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        xaxis.setDateFormatOverride(new SimpleDateFormat("dd-MMM HH:mm:ss"));
        final NumberAxis yaxis = (NumberAxis) plot.getRangeAxis();
        yaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return chart;
    }

}
