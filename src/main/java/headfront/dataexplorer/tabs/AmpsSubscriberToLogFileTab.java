package headfront.dataexplorer.tabs;

import com.crankuptheamps.client.CommandId;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.exception.AMPSException;
import headfront.amps.AmpsConnection;
import headfront.amps.services.TopicService;
import headfront.convertor.JacksonJsonConvertor;
import headfront.dataexplorer.tabs.dialog.JetFuelDateSelectorDialog;
import headfront.guiwidgets.PopUpDialog;
import headfront.utils.FileUtils;
import headfront.utils.StringUtils;
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
import javafx.stage.Modality;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.tools.Borders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Deepak on 08/07/2016.
 */
public class AmpsSubscriberToLogFileTab extends Tab {

    private static final Logger LOG = LoggerFactory.getLogger(AmpsSubscriberToLogFileTab.class);
    private TextArea logTextArea = new TextArea();
    private ComboBox<String> topicComboBox;
    private List<String> messagesToWrite = new ArrayList<>();
    private Consumer<Integer> recordCountListener = count -> {
    };
    private int fileNameCounter = 0;
    private int publishedMessage = 0;
    private CommandId currentAmpsSubCommandId = null;
    private AmpsConnection connection;
    private final TextField filterTextField = TextFields.createClearableTextField();

    private File choosenDir;
    private Button chooseButton = new Button("Choose Folder");
    private Button subscribeToAmps = new Button("Subscribe from NOW");
    private Button historicSubscribeToAmps = new Button("Historic Subscribe");
    private JacksonJsonConvertor jsonConvertor = new JacksonJsonConvertor();
    private int writeBatchSize = 10_000;
    private boolean sendUpdate = true;

    public AmpsSubscriberToLogFileTab(String tabName, AmpsConnection connection, TopicService topicService) {
        super(tabName);
        this.connection = connection;
        setupTab();
        BorderPane mainPane = new BorderPane();

        filterTextField.setMinWidth(80);
        filterTextField.setText("");
        filterTextField.setPromptText("Optional filter");
        ObservableList<String> listToUse = FXCollections.observableArrayList();
        listToUse.addAll(topicService.getAllTopicsNamesOnly());
        FXCollections.sort(listToUse);
        topicComboBox = new ComboBox<>(listToUse);
        topicComboBox.setEditable(true);
        TextFields.bindAutoCompletion(topicComboBox.getEditor(), topicComboBox.getItems());
        chooseButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose folder where the logs will be saved");
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
        subscribeToAmps.setOnAction(e -> {
            publishedMessage = 0;
            updateMessageSent();
            subscribeToAmps(null);
        });
        historicSubscribeToAmps.setOnAction(e -> {
            JetFuelDateSelectorDialog jetFuelDateSelectorDialog = new JetFuelDateSelectorDialog("Select the time from which JetFuel should start reading the journal");
            jetFuelDateSelectorDialog.initModality(Modality.APPLICATION_MODAL);
            jetFuelDateSelectorDialog.initOwner(null);
            Optional<String> selectedValue = jetFuelDateSelectorDialog.showAndWait();
            selectedValue.ifPresent(val -> {
                publishedMessage = 0;
                updateMessageSent();
                subscribeToAmps(val);
            });
        });
        Button clearOutput = new Button("Clear log panel");
        clearOutput.setOnAction(e -> {
            logTextArea.clear();
            publishedMessage = 0;
            updateMessageSent();
        });
        Button stopSendingToAmps = new Button("Stop Subscription");
        stopSendingToAmps.setOnAction(e -> {
            if (currentAmpsSubCommandId != null) {
                connection.unsubscribe(currentAmpsSubCommandId);
                currentAmpsSubCommandId = null;
            }
            enableButtons();
            synchronized (messagesToWrite) {
                addToLog("Stopping Subscription to amps and writing final " + messagesToWrite.size() + " messages to log");
                List<String> copyOfMesages = new ArrayList<>(messagesToWrite);
                messagesToWrite.clear();
                writeToFile(copyOfMesages);
            }

            fileNameCounter = 0;
        });
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(5, 5, 5, 5));
        topPanel.setSpacing(10);
        topPanel.getChildren().addAll(new Label("Topic"), topicComboBox, filterTextField, chooseButton, subscribeToAmps, historicSubscribeToAmps, stopSendingToAmps, clearOutput);
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
        if (choosenDir.isDirectory()) {
            addToLog("Log file directory is  " + file.getAbsolutePath());
        } else {
            PopUpDialog.showWarningPopup("Directory not selected",
                    "Please choose directory where log files will be written.");
        }
    }

    private void stopSendingUpdate() {
        sendUpdate = false;
    }

    private void startSendingUpdates() {
        sendUpdate = true;
        updateMessageSent();
    }

    private void subscribeToAmps(String bookmark) {
        disableButtons();
        if (choosenDir != null) {
            addToLog("Starting subscription");
            final String topic = topicComboBox.getValue();
            final String filter = filterTextField.getText();
            try {
                Consumer<Message> messageConsumer = m -> {
                    if (m.getCommand() == Message.Command.Ack && m.getAckType() == Message.AckType.Completed) {
                        addToLog("Received all the data from journals. Now listening to new updates. Press on 'Stop Subscription' to write current messages to log file");
                    }
                    final String trim = m.getData().trim();
                    if (trim.length() > 0) {
                        publishedMessage++;
                        updateMessageSent();
                        synchronized (messagesToWrite) {
                            if (m.getCommand() != Message.Command.OOF) {
                                String formatedDateTime = StringUtils.formatToLogDate(m.getTimestamp());
                                //2017-07-29T11:16:07.6770050+01:00 [28] trace: 12-0001 client[JetFuel_Deepak] publish command received: {"c":"delta_publish","t":"/BBG/QUOTES"}{"ID":"QUOTES_1016","Status":46,"RecordPrefix":"QUOTES","AutoNegOn":false,"Trader":"Asian Fusion","QuoteOn":false,"Count":10322,"BidQty":17.49,"OfferQty":19.18,"Offer":83.63,"TraderID":53,"Bid":74.07,"Topic":"QUOTES"}
                                messagesToWrite.add(formatedDateTime + " [28] trace: 12-0001 client[JetFuel_Deepak] publish command received: {\"c\":\"delta_publish\",\"t\":\"" + m.getTopic() + "\"}" + trim);
                                if (messagesToWrite.size() >= writeBatchSize) {
                                    List<String> copyOfMesages = new ArrayList<>(messagesToWrite);
                                    messagesToWrite.clear();
                                    addToLog(new Date() + " - Writing a batch of " + writeBatchSize + " messages to file");
                                    writeToFile(copyOfMesages);
                                }
                            }
                        }
                    }
                };
                if (bookmark != null) {
                    currentAmpsSubCommandId = connection.subscribeTopic(topic, filter, "", messageConsumer,
                            true, Message.Command.Subscribe, bookmark, "");
                } else {
                    currentAmpsSubCommandId = connection.subscribeTopic(topic, filter, "", messageConsumer,
                            false, Message.Command.Subscribe, "");
                }
                addToLog("Subscribed to Amps...");
            } catch (AMPSException e) {
                addToLog("Unable to subscribe to topic " + topic + " and filter " + filter + " error " + e.getMessage());
                LOG.error("Unable to subscribe to topic " + topic + " and filter " + filter + " error " + e.getMessage(), e);
            }
        } else {
            PopUpDialog.showWarningPopup("Directory not selected",
                    "Please choose directory from which contains amps logs.");
            enableButtons();
        }
    }

    private void writeToFile(List<String> copyOfMessages) {
        final String fileIndex = StringUtils.getPaddedString("" + fileNameCounter++, 10, "0");
        FileUtils.saveFile(new File(choosenDir.getAbsoluteFile() + File.separator + "JetFuelLogWriter_" + fileIndex + ".log"), copyOfMessages, false);
    }

    private void disableButtons() {
        topicComboBox.setDisable(true);
        filterTextField.setDisable(true);
        chooseButton.setDisable(true);
        subscribeToAmps.setDisable(true);
        historicSubscribeToAmps.setDisable(true);
    }

    private void enableButtons() {
        topicComboBox.setDisable(false);
        filterTextField.setDisable(false);
        chooseButton.setDisable(false);
        subscribeToAmps.setDisable(false);
        historicSubscribeToAmps.setDisable(false);
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
