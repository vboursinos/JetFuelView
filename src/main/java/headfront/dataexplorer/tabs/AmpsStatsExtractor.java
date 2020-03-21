package headfront.dataexplorer.tabs;

import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.exception.AMPSException;
import headfront.amps.services.AmpsStatsLoader;
import headfront.dataexplorer.tabs.dialog.JetFuelDateSelectorDialog;
import headfront.guiwidgets.DateTimePicker;
import headfront.guiwidgets.PopUpDialog;
import headfront.utils.FileUtils;
import headfront.utils.StringUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
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
import java.io.IOException;
import java.io.StringReader;
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
    private File choosenOutputDir = new File ("/Users/deepakcdo/Documents/MySpace/Dev/JetFuelView/out");
    private File choosenInputFile = new File("/Users/deepakcdo/Documents/MySpace/Dev/JetFuelView/statsExtractor/ExtractMemory.txt");
    private Button chooseOutputButton = new Button("Output Folder");
    private Button chooseInputFileButton = new Button("Input File");
    private Button extractButton = new Button("Extract");
    private DateTimePicker startDatePicker = new DateTimePicker();
    private DateTimePicker toDatePicker = new DateTimePicker();
    private boolean sendUpdate = true;
    private MaskerPane maskerPane = new MaskerPane();
    private String adminUrl;
    private String ampsInstanceName;

    public AmpsStatsExtractor(String tabName,String adminUrl, String ampsInstanceName) {
        super(tabName);
        this.adminUrl = adminUrl;
        this.ampsInstanceName = ampsInstanceName;
        setupTab();
        BorderPane mainPane = new BorderPane();
        startDatePicker.setDateTimeValue(LocalDateTime.now().minusDays(1));
        toDatePicker.setDateTimeValue(LocalDateTime.now());
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

        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(5, 5, 5, 5));
        topPanel.setSpacing(10);
        topPanel.getChildren().addAll(new Label("From Date"), startDatePicker,
                new Label("To Date"), toDatePicker,
                chooseOutputButton, chooseInputFileButton, extractButton, clearOutput);
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
        updateMessageSent();
        maskerPane.setVisible(true);
        Properties prop = new Properties();
        try {
            prop.load(new FileReader(choosenInputFile));
            CountDownLatch coundownLatch = new CountDownLatch(prop.size());
            Map<String, Object> allData = new ConcurrentHashMap<>();
            prop.entrySet().forEach(entry -> {
                System.out.println(ampsInstanceName + "--£" + entry.getKey() + "---" + adminUrl + "/" + entry.getValue());
            });
            Thread.sleep(1000);
            coundownLatch.await(10, TimeUnit.MINUTES);
            maskerPane.setVisible(false);

        } catch (Exception e) {
            LOG.error("Unable to extract stats ", e);
            maskerPane.setVisible(false);
        }


//        statsToGather.forEach(stats -> {
//                    Runnable r = () -> {
//                        new AmpsStatsLoader(connectionsStr, adminPortStr, json -> {
//                            if (json != null) {
//                                data.put(stats, json);
//                            } else {
//                                PopUpDialog.showWarningPopup("Did not receive data", "Did not receive data for " + StringUtils.getFullTreePath(stats) + ". Just showing the data we got.");
//                            }
//                            coundownLatch.countDown();
//                        }, 0, stats, chooser.getStartDate(), chooser.getToDate(), useSecureHttp);
//                    };
//                    new Thread(r).start();
//                }
//        );
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
