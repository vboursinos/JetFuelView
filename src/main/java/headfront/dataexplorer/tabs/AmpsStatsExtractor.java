package headfront.dataexplorer.tabs;

import headfront.amps.services.AmpsStatsLoader;
import headfront.dataexplorer.ContentProperties;
import headfront.guiwidgets.DateTimePicker;
import headfront.guiwidgets.PopUpDialog;
import headfront.guiwidgets.TextAreaDialog;
import headfront.utils.FileUtils;
import headfront.utils.StringUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.tools.Borders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Deepak on 08/07/2016.
 */
public class AmpsStatsExtractor extends Tab {

    private static final Logger LOG = LoggerFactory.getLogger(AmpsStatsExtractor.class);
    private TextArea logTextArea = new TextArea();
    private Consumer<Integer> recordCountListener = count -> {
    };
    private int publishedMessage = 0;
    private File choosenOutputDir = new File("/Users/deepakcdo/Documents/MySpace/Dev/JetFuelView/out/");
    private File choosenInputFile = new File("/Users/deepakcdo/Documents/MySpace/Dev/JetFuelView/statsExtractor/ExtractMemory.txt");
    private Button chooseOutputButton = new Button("Output Folder");
    private Button chooseInputFileButton = new Button("Input File");
    private Button extractButton = new Button("Extract");
    private Button helplButton = new Button("Help");
    private DateTimePicker startDatePicker = new DateTimePicker();
    private DateTimePicker toDatePicker = new DateTimePicker();
    private boolean sendUpdate = true;
    private MaskerPane maskerPane = new MaskerPane();
    private String ampsInstanceName;
    private String connectionsStr;
    private String adminPortStr;
    private boolean useSecureHttp;

    public AmpsStatsExtractor(String tabName, String connectionsStr, String adminPortStr, boolean useSecureHttp,
                              String ampsInstanceName) {
        super(tabName);
        this.connectionsStr = connectionsStr;
        this.adminPortStr = adminPortStr;
        this.useSecureHttp = useSecureHttp;
        startDatePicker.setDateTimeValue(LocalDateTime.now().minusDays(1));
        toDatePicker.setDateTimeValue(LocalDateTime.now().plusMinutes(1));
        this.ampsInstanceName = ampsInstanceName.replace("[Instance - ", "").replace("]", "");
        setupTab();
        BorderPane mainPane = new BorderPane();
        chooseOutputButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose folder where the extracts  will be saved");
            File file = directoryChooser.showDialog(null);
            if (file != null) {
                validateOutputFolder(file);
            }
        });
        chooseInputFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Input Source File");
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                validateInputFile(file);
            }
        });

        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                startSendingUpdates();
            } else {
                stopSendingUpdate();
            }
        });
        extractButton.setOnAction(e -> {
            processExtract();
        });

        Button clearOutput = new Button("Clear log panel");
        clearOutput.setOnAction(e -> {
            logTextArea.clear();
            publishedMessage = 0;
            updateMessageSent();
        });
        helplButton.setOnAction(e -> {
            showHelpDialog();
        });

        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(5, 5, 5, 5));
        topPanel.setSpacing(10);
        topPanel.getChildren().addAll(new Label("From Date"), startDatePicker,
                new Label("To Date"), toDatePicker,
                chooseOutputButton, chooseInputFileButton, extractButton, clearOutput, helplButton);
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
        StackPane mainPanelWithMasker = new StackPane();
        maskerPane.setText("JetFuel is Extracting stats.");
        maskerPane.setVisible(false);
        mainPanelWithMasker.getChildren().addAll(mainPane, maskerPane);
        setContent(mainPanelWithMasker);
    }

    private void processExtract() {
        addToLog("--------------------Staring extract--------------------");
        addToLog("Using Input file " + choosenInputFile);
        addToLog("Using Output Directory " + choosenOutputDir);
        updateMessageSent();
        maskerPane.setVisible(true);
        Properties prop = new Properties();
        try {
            prop.load(new FileReader(choosenInputFile));
            CountDownLatch coundownLatch = new CountDownLatch(prop.size());
            Map<String, Object> allData = new ConcurrentHashMap<>();
            prop.entrySet().forEach(entry -> {
                if (!entry.getKey().toString().startsWith("#")) {
                    Runnable r = () -> {
                        addToLog("Loading stats for key " + entry.getKey() + "and Value " + entry.getValue());
                        new AmpsStatsLoader(connectionsStr, adminPortStr, json -> {
                            try {
                                if (json != null) {
                                    addToLog("Got data for " + entry.getKey());
                                    List<String> data = new ArrayList<>();
                                    data.add(json);
                                    allData.put(ampsInstanceName + "--" + entry.getKey(), json);
                                    File fileToWrite = new File(choosenOutputDir, ampsInstanceName + "--" + entry.getKey() + ".csv");
                                    addToLog("Writing file " + fileToWrite.getAbsolutePath());
                                    FileUtils.saveFile(fileToWrite, data, false);
                                    addToLog("Written file " + fileToWrite.getAbsolutePath());
                                } else {
                                    addToLog("Did not get data for " + entry.getKey());
                                    PopUpDialog.showWarningPopup("Did not receive data", "Did not receive data for " + entry.getValue() + ". Just showing the data we got.");
                                }
                                coundownLatch.countDown();
                            } catch (Exception e) {
                                addToLog("Unable to extract  stats for key " + entry.getKey() + " " + e.getMessage());
                                LOG.error("Unable to extract stats for " + entry.getKey(), e);

                            }
                        }, 0, entry.getValue().toString(),
                                startDatePicker.getDateTimeValue(), toDatePicker.getDateTimeValue(), useSecureHttp, "csv");
                    };
                    new Thread(r).start();
                }
            });
            coundownLatch.await(10, TimeUnit.SECONDS);
            writeAllFile(allData);
            maskerPane.setVisible(false);
            addToLog("--------------------Finished  extract--------------------");
        } catch (Exception e) {
            LOG.error("Unable to extract stats ", e);
            maskerPane.setVisible(false);
        }
    }

    private void writeAllFile(Map<String, Object> sourceData) {
        addToLog("Creating All File");
        StringBuffer headers = new StringBuffer();
        headers.append("Timestamp,");
        Map<String, StringBuffer> allData = new LinkedHashMap();
        sourceData.entrySet().forEach(e -> {
                    headers.append(e.getKey() + ",");
                    addToAllData(allData, e.getValue());
                }
        );

        List<String> listToWrite = new ArrayList<>();
        listToWrite.add(headers.toString());
        allData.entrySet().forEach(e -> {
            String txt = e.getKey() + "," + e.getValue().toString();
            listToWrite.add(txt);

        });
        File fileToWrite = new File(choosenOutputDir, ampsInstanceName + "--ALL.csv");
        addToLog("Writing All file " + fileToWrite.getAbsolutePath());
        FileUtils.saveFile(fileToWrite, listToWrite, false);
        addToLog("Written All file " + fileToWrite.getAbsolutePath());
    }

    private void addToAllData(Map<String, StringBuffer> allData, Object value) {
        String[] lines = value.toString().split("\n");
        for (String line : lines) {
            String[] nameValuePair = line.split(",");
            StringBuffer bufferToUpdate = allData.get(nameValuePair[0]);
            if (bufferToUpdate == null) {
                bufferToUpdate = new StringBuffer();
                allData.put(nameValuePair[0], bufferToUpdate);
            }
            bufferToUpdate.append(nameValuePair[1] + ",");
        }
    }

    private void validateOutputFolder(File file) {
        choosenOutputDir = file;
        if (choosenOutputDir.isDirectory()) {
            addToLog("Extract  directory is  " + file.getAbsolutePath());
        } else {
            PopUpDialog.showWarningPopup("Directory not selected",
                    "Please choose directory where Extract will be written.");
        }
    }

    private void validateInputFile(File file) {
        choosenInputFile = file;
        if (choosenInputFile.isDirectory()) {
            PopUpDialog.showWarningPopup("File not selected",
                    "Please choose Input file for the extract.");
        } else {
            addToLog("Input File is " + file.getAbsolutePath());
        }
    }

    private void stopSendingUpdate() {
        sendUpdate = false;
    }

    private void startSendingUpdates() {
        sendUpdate = true;
        updateMessageSent();
    }


    private void writeToFile(List<String> copyOfMessages) {
        final String fileIndex = StringUtils.getPaddedString("" + publishedMessage++, 10, "0");
        FileUtils.saveFile(new File(choosenOutputDir.getAbsoluteFile() + File.separator + "JetFuelLogWriter_" + fileIndex + ".log"), copyOfMessages, false);
    }


    private TabPane createLogPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        GridPane.setHgrow(tabPane, Priority.ALWAYS);
        GridPane.setVgrow(tabPane, Priority.ALWAYS);
        Tab logTab = new Tab("Full Logs");
        logTab.setContent(logTextArea);
        tabPane.getTabs().setAll(logTab);
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

    private void showHelpDialog() {
        Object helpText = ContentProperties.getInstance().getProperty("statsExtractor.help");
        TextAreaDialog dlg = new TextAreaDialog(helpText.toString());
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(null);
        dlg.setTitle("Stats Extractor Help ");
        dlg.getDialogPane().setHeaderText("You can extract amps stats from here. Below is an example of the expected input file");
        dlg.show();
    }
}
