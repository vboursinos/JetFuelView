package headfront.dataexplorer.tabs;

import headfront.amps.AmpsConnection;
import headfront.amps.services.TopicService;
import headfront.convertor.JacksonJsonConvertor;
import headfront.guiwidgets.PopUpDialog;
import headfront.utils.FileUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.tools.Borders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Deepak on 08/07/2016.
 */
public class FilePublisherTab extends Tab {

    private static final Logger LOG = LoggerFactory.getLogger(FilePublisherTab.class);
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private TextArea logTextArea = new TextArea();
    private Consumer<Integer> recordCountListener = count -> {
    };
    private int publishedMessage = 0;
    private AmpsConnection connection;
    private File choosenDir;
    private List<Path> filesToProcess;
    private CheckBox overrideTopic = new CheckBox("Override Publish Topic");
    private ComboBox<String> topicComboBox;
    private TextField speedMultipler = new TextField("1");
    private Button chooseButton = new Button("Choose Folder");
    private Button sendToAmps = new Button("Send To Amps");
    private Thread currentThread = null;
    private volatile boolean stopCurrentPublisher = false;
    private JacksonJsonConvertor jsonConvertor = new JacksonJsonConvertor();
    private boolean sendUpdate = true;

    public FilePublisherTab(String tabName, AmpsConnection connection, TopicService topicService) {
        super(tabName);
        this.connection = connection;
        setupTab();
        BorderPane mainPane = new BorderPane();

        ObservableList<String> listToUse = FXCollections.observableArrayList();
        listToUse.addAll(topicService.getAllTopicsNamesOnly());
        FXCollections.sort(listToUse);
        topicComboBox = new ComboBox<>(listToUse);
        topicComboBox.setEditable(true);
        TextFields.bindAutoCompletion(topicComboBox.getEditor(), topicComboBox.getItems());
        Tooltip tip = new Tooltip("By default JetFuel will publish to topic from the log file. " +
                "To override the publish topic to publish check 'Override Publish Topic' and select a topic.");
        topicComboBox.setTooltip(tip);
        overrideTopic.setTooltip(tip);
        topicComboBox.setDisable(true);
        overrideTopic.setSelected(false);
        overrideTopic.selectedProperty().addListener((com, oldValue, newValue) -> {
            if (newValue) {
                topicComboBox.setDisable(false);
            } else {
                topicComboBox.setDisable(true);
                topicComboBox.getSelectionModel().clearSelection();
            }
        });
        chooseButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose folder of the logs to replay");
            File file = directoryChooser.showDialog(null);
            if (file != null) {
                processFolder(file);
            }
        });

        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                startSendingUpdates();
            } else {
                stopSendingUpdate();
            }
        });
        sendToAmps.setOnAction(e -> {
            publishedMessage = 0;
            updateMessageSent();
            stopCurrentPublisher = true;
            if (currentThread != null) {
                currentThread.interrupt();
            }
            currentThread = null;
            sendToAmps();
        });
        Button clearOutput = new Button("Clear log panel");
        clearOutput.setOnAction(e -> {
            logTextArea.clear();
            publishedMessage = 0;
            updateMessageSent();
        });
        Button stopSendingToAmps = new Button("Stop Sending To Amps");
        stopSendingToAmps.setOnAction(e -> {
            if (currentThread != null) {
                addToLog("Stopping Sending to amps");
                stopCurrentPublisher = true;
                currentThread.interrupt();
                enableButtons();
            }
        });
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(5, 5, 5, 5));
        topPanel.setSpacing(10);
        Label mulLabel = new Label("Speed multiplier");
        Tooltip tooltip = new Tooltip("1 will be log file pace, less than 1 will be as fast as possible, 2 will be twice as fast as log file pace.");
        mulLabel.setTooltip(tooltip);
        speedMultipler.setTooltip(tooltip);
        speedMultipler.setPrefColumnCount(3);
        topPanel.getChildren().addAll(chooseButton, mulLabel, speedMultipler, overrideTopic, topicComboBox, sendToAmps, stopSendingToAmps, clearOutput);
        topPanel.setAlignment(Pos.BASELINE_CENTER);
        mainPane.setTop(topPanel);
        Node titledLogTextArea = Borders.wrap(createLogPane())
                .lineBorder()
                .title("Log panel").innerPadding(5).outerPadding(10)
                .color(Color.BLUE)
                .thickness(1)
                .radius(5, 5, 5, 5)
                .build().build();
        mainPane.setCenter(titledLogTextArea);
        logTextArea.setEditable(false);
        setContent(mainPane);
    }

    private void processFolder(File file) {
        choosenDir = file;
        addToLog(LocalDateTime.now() + " Starting to process dir " + file.getAbsolutePath());
        filesToProcess = FileUtils.getFiles(file.getAbsolutePath(), ".log");
        addToLog("Found " + filesToProcess.size() + " filesToProcess");
        filesToProcess.forEach(f -> {
            addToLog("Found log file " + f.getFileName());
        });
    }

    private void stopSendingUpdate() {
        sendUpdate = false;
    }

    private void startSendingUpdates() {
        sendUpdate = true;
        updateMessageSent();
    }

    private void sendToAmps() {
        disableButtons();
        stopCurrentPublisher = false;
        Runnable r = () -> {
            if (choosenDir != null) {
                Path currentFile = null;
                String currentText = null;
                try {
                    final String multipler = speedMultipler.getText();
                    int intMultipiler = 0;
                    try {
                        intMultipiler = Integer.parseInt(multipler);
                    } catch (Exception e) {
                        enableButtons();
                        PopUpDialog.showErrorPopup("Invalid Multiplier", multipler + " is not valid. Please enter a valid int");
                        return;
                    }
                    String topicToUse = null;
                    if (overrideTopic.isSelected()) {
                        final String selectedItem = topicComboBox.getSelectionModel().getSelectedItem();
                        if (selectedItem != null) {
                            topicToUse = selectedItem;
                        } else {
                            enableButtons();
                            PopUpDialog.showErrorPopup("Invalid Topic", "Select a topic or uncheck 'Override Publish Topic'");
                            return;
                        }
                    }
                    String topicMsg = topicToUse == null ? "" : " on overriden topic " + topicToUse;
                    addToLog("Starting to send logs to amps" + topicMsg);
                    for (Path entry : filesToProcess) {
                        currentFile = entry;
                        addToLog(LocalDateTime.now() + " Processing " + entry.toString());
                        List<String> strings = Files.readAllLines(entry);
                        int size = strings.size();
                        addToLog("We have " + size + " logs lines to process");
                        long lastPublish = -1;
                        int processedMessages = 0;
                        for (String text : strings) {
                            if (!stopCurrentPublisher) {
                                currentText = text;
                                if (text.contains("publish command received: ") || text.contains("sow_delete command received: {")) {
                                    if (intMultipiler >= 1) {
                                        String date = text.substring(0, 23);
                                        LocalDateTime publishNow = LocalDateTime.parse(date, dateFormatter);
                                        long now = publishNow.toInstant(ZoneOffset.UTC).toEpochMilli();
                                        if (lastPublish != -1) {
                                            long timeToWait = now - lastPublish;
                                            if (timeToWait >= 1) {
                                                long mills = (timeToWait) / intMultipiler;
                                                if (mills > 0) {
                                                    Thread.sleep(mills);
                                                }
                                            }
                                        }
                                        lastPublish = now;
                                    }
                                    sendToAmps(topicToUse, text);
                                    publishedMessage++;
                                }
                                processedMessages++;
                                if ((processedMessages % 1000) == 0) {
                                    addToLog("Processed " + processedMessages + " out of " + size);
                                    updateMessageSent();
                                }
                            }
                        }
                        addToLog(LocalDateTime.now() + " Finished Processing " + entry.toString());
                    }
                    addToLog("Done. Published " + publishedMessage + " messages.");
                    updateMessageSent();
                    enableButtons();
                } catch (Exception e) {
                    addToLog("Unable to process file " + currentFile.getFileName() + " Error " + e.getMessage());
                    LOG.error("Unable to process text " + currentText + " from file " + currentFile.getFileName(), e);
                }
            } else {
                PopUpDialog.showWarningPopup("Directory not selected",
                        "Please choose directory from which contains amps logs.");
                enableButtons();
            }
        };
        currentThread = new Thread(r);
        currentThread.start();
    }


    private void disableButtons() {
        speedMultipler.setDisable(true);
        chooseButton.setDisable(true);
        sendToAmps.setDisable(true);
        topicComboBox.setDisable(true);
        overrideTopic.setDisable(true);
    }

    private void enableButtons() {
        speedMultipler.setDisable(false);
        chooseButton.setDisable(false);
        sendToAmps.setDisable(false);
        overrideTopic.setDisable(false);
        final String selectedItem = topicComboBox.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            topicComboBox.setDisable(true);
        } else {
            topicComboBox.setDisable(false);
        }
    }

    private TabPane createLogPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        GridPane.setHgrow(tabPane, Priority.ALWAYS);
        GridPane.setVgrow(tabPane, Priority.ALWAYS);
        Tab logTab = new Tab("Full Logs");
        logTab.setContent(logTextArea);
        tabPane.getTabs().setAll(logTab);//, outputTab);
        return tabPane;
    }

    private void setupTab() {
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                updateMessageSent();
            }
        });
        closableProperty().setValue(false);
    }

    private void sendToAmps(String topicToUse, String textToRun) {
        if (textToRun.contains("}{")) {
            // handle publish
            //2017-07-29T11:16:07.6770050+01:00 [28] trace: 12-0001 client[JetFuel_Deepak] publish command received: {"c":"delta_publish","t":"/BBG/QUOTES"}{"ID":"QUOTES_1016","Status":46,"RecordPrefix":"QUOTES","AutoNegOn":false,"Trader":"Asian Fusion","QuoteOn":false,"Count":10322,"BidQty":17.49,"OfferQty":19.18,"Offer":83.63,"TraderID":53,"Bid":74.07,"Topic":"QUOTES"}
            try {
                String[] parts = textToRun.split("\\{");
                String command = "{" + parts[1];
                String message = "{" + parts[2];
                Map<String, Object> commandMap = jsonConvertor.convertToMap(command);
                Object ampsCommand = commandMap.get("c");
                Object topic = commandMap.get("t");
                if (ampsCommand != null && topic != null) {
                    String ampsTopic = topic.toString();
                    if (ampsTopic.equals(AmpsConnection.CLIENT_STATUS_TOPIC) || ampsTopic.equals(AmpsConnection.SOW_STATS_TOPIC)) {
                        // dont publish internal topic
                        return;
                    }
                    if (topicToUse != null) {
                        ampsTopic = topicToUse;
                    }
                    if (ampsCommand.toString().equals("delta_publish")) {
                        connection.publishDelta(ampsTopic, null, message);
                    } else if (ampsCommand.toString().equals("publish")) {
                        connection.publish(ampsTopic, null, message);
                    } else {
                        LOG.warn("Cant process " + textToRun + " as we got this is the command " + command +
                                ". We decoded command as " + ampsCommand +
                                " and cant process it");
                    }
                } else {
                    LOG.warn("Cant process " + textToRun + " as we got this is the command " + command +
                            ". We decoded command as " + ampsCommand +
                            " and topic as " + topic);
                }
            } catch (Exception e) {
                addToLog("Exception thrown - Stopping " + e.getMessage());
                LOG.warn("Unable to process " + textToRun, e);
            }
        } else if (textToRun.contains("sow_delete command received:")) {
            // handle delete header
            // 2017-07-29T11:16:07.6718280+01:00 [28] trace: 12-0005 client[JetFuel_Deepak] sow_delete command received: {"c":"sow_delete","cid":"4","t":"/BBG/QUOTES","filter":"/ID='QUOTES_1016'","a":"processed,stats"}
            String[] parts = textToRun.split("\\{");
            String message = "{" + parts[1];
            final Map<String, Object> stringObjectMap = jsonConvertor.convertToMap(message);
            try {
                connection.deleteCommand(stringObjectMap.get("t").toString(),
                        stringObjectMap.get("filter").toString());
            } catch (Exception e) {
                LOG.warn("Cant process delete " + textToRun + " as decoded it to " + stringObjectMap +
                        " topic = " + stringObjectMap.get("t").toString() +
                        " filter = " + stringObjectMap.get("filter").toString());
            }
        }
    }

    private void addToLog(String message) {
        Platform.runLater(() -> {
            logTextArea.appendText(message);
            logTextArea.appendText("\n");
        });
    }


    public void setRecordCountListener(Consumer<Integer> recordCountListener) {
        this.recordCountListener = recordCountListener;
        updateMessageSent();
    }

    private void updateMessageSent() {
        if (sendUpdate) {
            recordCountListener.accept(publishedMessage);
        }
    }
}
