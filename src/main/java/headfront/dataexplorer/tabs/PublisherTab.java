package headfront.dataexplorer.tabs;

import com.crankuptheamps.client.CommandId;
import com.crankuptheamps.client.Message;
import headfront.amps.AmpsConnection;
import headfront.dataexplorer.ContentProperties;
import headfront.guiwidgets.TextAreaDialog;
import headfront.utils.FileUtils;
import headfront.utils.GuiUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import org.controlsfx.tools.Borders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by Deepak on 08/07/2016.
 */
public class PublisherTab extends Tab {

    private Logger LOG = LoggerFactory.getLogger(PublisherTab.class);

    private TextArea publishTextArea = new TextArea();
    private TextArea logTextArea = new TextArea();
    private TextArea outputTextArea = new TextArea();
    private SplitPane splitPane = new SplitPane();
    private Consumer<Integer> recordCountListener = count -> {
    };
    private List<CommandId> activeCommands = new ArrayList<>();
    private int publishedMessage = 0;
    private AmpsConnection connection;
    private Image trashImage = new Image("images/icons/trash.png");
    private Image helpImage = new Image("images/icons/Help.png");
    private Image saveImage = new Image("images/icons/Save.png");
    private Image openImage = new Image("images/icons/open.png");
    private Image chooseImage = new Image("images/icons/choose.png");
    private Image sendToAmpsImage = new Image("images/icons/SendMessage.png");
    private final List<String> logMessages = new ArrayList<>();
    private Task<Void> currentAddToLogTask = null;
    private final List<String> allMessages = new ArrayList<>();
    private Task<Void> currentAddToAllLogTask = null;
    private ExecutorService executorServiceLog = Executors.newSingleThreadExecutor();
    private ExecutorService executorServiceAllLogs = Executors.newSingleThreadExecutor();

    public PublisherTab(String tabName, AmpsConnection connection) {
        super(tabName);
        this.connection = connection;
        setupTab();
        BorderPane publisherPanel = new BorderPane();
        Object promptText = ContentProperties.getInstance().getProperty("PublisherPanel.promptText");
        publishTextArea.setPromptText(promptText.toString());
        BorderPane labelledPublishPanel = new BorderPane();
        labelledPublishPanel.setPadding(new Insets(5, 0, 5, 0));
        Label descriptionPanel = new Label("Publish / Delete commands to send.");

        logTextArea.setWrapText(true);
        outputTextArea.setWrapText(true);
        Button openButton = new Button("", new ImageView(openImage));
        openButton.setTooltip(new Tooltip("Open saved messages"));
        openButton.setOnAction(e -> {
            String string = FileUtils.readFile();
            if (string != null) {
                publishTextArea.clear();
                publishTextArea.setText(string);
            }

        });
        Button saveButton = new Button("", new ImageView(saveImage));
        saveButton.setOnAction(e -> FileUtils.saveFile(publishTextArea.getText(), false));
        saveButton.setTooltip(new Tooltip("Save messages for future"));
        Button clearButton = new Button("", new ImageView(trashImage));
        clearButton.setTooltip(new Tooltip("Clear All"));
        clearButton.setOnAction(e -> {
            clearUpPreviousRun();
            publishTextArea.clear();
        });
        Button helpButton = new Button("", new ImageView(helpImage));
        helpButton.setTooltip(new Tooltip("Get Help on Publish syntax"));
        helpButton.setOnAction(e -> showHelpDialog());
        Button chooseTopic = new Button("", new ImageView(chooseImage));
        chooseTopic.setTooltip(new Tooltip("Choose Topic"));
        chooseTopic.setOnAction(e -> showTopicSelection());
        Button publishButton = new Button("Send To Amps", new ImageView(sendToAmpsImage));
        publishButton.setOnAction(e -> publishText());
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(3);
        buttonBox.getChildren().addAll(openButton, saveButton, clearButton, chooseTopic, helpButton, publishButton);
        descriptionPanel.setStyle("-fx-font-weight: bold;");
        BorderPane topPane = new BorderPane();
        descriptionPanel.alignmentProperty().setValue(Pos.BOTTOM_LEFT);
        topPane.setPadding(new Insets(5, 5, 5, 5));
        topPane.setRight(buttonBox);
        topPane.setLeft(descriptionPanel);
        labelledPublishPanel.setTop(topPane);
        labelledPublishPanel.setCenter(publishTextArea);
        publisherPanel.setCenter(labelledPublishPanel);
        Node titledPublisherPanel = Borders.wrap(publisherPanel)
                .lineBorder()
                .title("Publisher panel").innerPadding(5).outerPadding(10)
                .color(Color.BLUE)
                .thickness(1)
                .radius(5, 5, 5, 5)
                .build().build();
        Node titledLogTextArea = Borders.wrap(createLogPane())
                .lineBorder()
                .title("Log panel").innerPadding(5).outerPadding(10)
                .color(Color.BLUE)
                .thickness(1)
                .radius(5, 5, 5, 5)
                .build().build();
        logTextArea.setEditable(false);
        outputTextArea.setEditable(false);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(titledPublisherPanel, titledLogTextArea);
        setContent(splitPane);
    }

    private TabPane createLogPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        GridPane.setHgrow(tabPane, Priority.ALWAYS);
        GridPane.setVgrow(tabPane, Priority.ALWAYS);
        Tab logTab = new Tab("Full Logs");
        logTab.setContent(logTextArea);
        Tab outputTab = new Tab("Amps output");
        outputTab.setContent(outputTextArea);
        tabPane.getTabs().setAll(logTab, outputTab);
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

    private void clearUpPreviousRun() {
        activeCommands.forEach(c -> connection.unsubscribe(c));
        activeCommands.clear();
        logTextArea.clear();
        outputTextArea.clear();
        publishedMessage = 0;
        logMessages.clear();
        allMessages.clear();
        if (currentAddToLogTask != null) {
            currentAddToLogTask.cancel();
        }
        currentAddToLogTask = null;
        if (currentAddToAllLogTask != null) {
            currentAddToAllLogTask.cancel();
        }
        currentAddToAllLogTask = null;
        updateMessageSent();
    }

    private void publishText() {
        clearUpPreviousRun();
        addToLog("Processing Sending to AMPS at " + new Date());
        String selectedText = publishTextArea.selectedTextProperty().get();
        if (selectedText.trim().length() > 0) {
            addToLog("Sending selected text to AMPS");
        } else {
            selectedText = publishTextArea.getText();
        }
        if (selectedText.trim().length() == 0) {
            addToLog("No text found. Stopping now. See example by clicking on 'Help' button");
            return;
        }
        final String textToRun = selectedText;
        Runnable r = () -> {
            try {
                Map<String, String> variables = new HashMap();
                final String[] lines = textToRun.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (line.length() == 0) {
                        continue;
                    }
                    if (line.toLowerCase().startsWith("//")) {
                        addToLog("Skipping comment line  " + line);
                        continue;
                    }

                    addToLog("Going to process '" + line + "'");
                    int seperatorIndex = line.indexOf("->");
                    if (seperatorIndex == -1) {
                        addToLog("This line does not contain valid seperator '->' Stopping now. See example by clicking on 'Help' button");
                        return;
                    } else {
                        String command = line.substring(0, seperatorIndex);
                        String message = line.substring(seperatorIndex + 2, line.length()).trim();
                        if (command.toLowerCase().startsWith("wait")) {
                            long waitTime = Long.parseLong(message);
                            addToLog("Going to wait " + waitTime + " seconds.");
                            try {
                                Thread.sleep(waitTime * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }

                        String[] split = command.split("=");
                        if (split.length != 2) {
                            addToLog("Could not process " + command + " Stopping now. See example by clicking on 'Help' button");
                            return;
                        }
                        String topic = split[1].trim();
                        if (command.toLowerCase().startsWith("var")) {
                            variables.put("%" + topic.trim(),  message.trim());
                        } else if (command.toLowerCase().startsWith("publishdoc")) {
                            addToLog("Sending full doc to " + topic);
                            sendAll(topic, lines);
                            return;
                        } else if (command.toLowerCase().startsWith("delete")) {
                            message = replaceVariables(variables, message);
                            addToLog("Sending a Delete to topic " + topic + " with message " + message);
                            sendDelete(topic, message);
                        } else if (command.toLowerCase().startsWith("publish_delta")) {
                            message = replaceVariables(variables, message);
                            addToLog("Sending a publish Delta to topic " + topic + " with message " + message);
                            sendPublish(topic, message, true);
                        } else if (command.toLowerCase().startsWith("publish")) {
                            message = replaceVariables(variables, message);
                            addToLog("Sending a publish to topic " + topic + " with message " + message);
                            sendPublish(topic, message, false);
                        } else if (command.toLowerCase().startsWith("sowdeltasubscribe")) {
                            message = replaceVariables(variables, message);
                            addToLog("Sending a sowdeltasubscribe to topic " + topic + " with filter '" + message + "'");
                            activeCommands.add(connection.subscribeTopic(topic, message, "", this::processMessage, false, Message.Command.SOWAndDeltaSubscribe, ""));
                        } else if (command.toLowerCase().startsWith("sowsubscribe")) {
                            message = replaceVariables(variables, message);
                            addToLog("Sending a sowSubscribe to topic " + topic + " with filter '" + message + "'");
                            activeCommands.add(connection.subscribeTopic(topic, message, "", this::processMessage, false, Message.Command.SOWAndSubscribe, ""));
                        } else if (command.toLowerCase().startsWith("subscribe")) {
                            message = replaceVariables(variables, message);
                            addToLog("Sending a subscribe to topic " + topic + " with filter '" + message + "'");
                            activeCommands.add(connection.subscribeTopic(topic, message, "", this::processMessage, false, Message.Command.Subscribe, ""));
                        } else if (command.toLowerCase().startsWith("deltasubscribe")) {
                            message = replaceVariables(variables, message);
                            addToLog("Sending a deltaSubscribe to topic " + topic + " with filter '" + message + "'");
                            activeCommands.add(connection.subscribeTopic(topic, message, "", this::processMessage, false, Message.Command.DeltaSubscribe, ""));
                        } else if (command.toLowerCase().startsWith("sow")) {
                            message = replaceVariables(variables, message);
                            addToLog("Sending a sow to topic " + topic + " with filter '" + message + "'");
                            activeCommands.add(connection.subscribeTopic(topic, message, "", this::processMessage, false, Message.Command.SOW, ""));
                        } else {
                            addToLog("Unknown command " + command + " Stopping now. See example by clicking on 'Help' button");
                            return;
                        }
                    }
                }
                addToLog("All Messages Processed !!!");
            } catch (Exception e) {
                LOG.error("Unable to process messages", e);
                addToLog("Exception thrown - Stopping " + e.getMessage());
            }

        };
        new Thread(r).start();

    }

    private String replaceVariables(Map<String, String> variables, String line) {
        String replacedLine = line;
        if (variables.size() > 0) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                replacedLine = replacedLine.replaceAll(entry.getKey(), entry.getValue());
            }
        }
        return replacedLine;

    }

    private void sendAll(String topic, String[] lines) throws Exception {
        StringBuilder builder = new StringBuilder();
        String firstLine = lines[0];
        builder.append(firstLine.substring(firstLine.indexOf(">") + 1));
        for (int i = 1; i < lines.length; i++) {
            builder.append(lines[i]);
        }
        connection.publish(topic, null, builder.toString());
    }

    private void sendPublish(String topic, String message, boolean deltaPublish) throws Exception {
        publishedMessage++;
        updateMessageSent();
        if (deltaPublish) {
            connection.publishDelta(topic, null, message);
        } else {
            connection.publish(topic, null, message);
        }
    }

    private void sendDelete(String topic, String message) throws Exception {
        publishedMessage++;
        updateMessageSent();
        connection.deleteCommand(topic, message);
    }

    private void addToLogAndAmpsOutput(String message) {
        synchronized (allMessages) {
            String time = LocalDateTime.now().toString();
            String fullMessage = time + " " + message;
            allMessages.add(fullMessage);
        }
        if (currentAddToAllLogTask == null || (currentAddToAllLogTask != null && currentAddToAllLogTask.isDone())) {
            currentAddToAllLogTask = new ScreeWriterTask(allMessages, logTextArea, outputTextArea);
            executorServiceAllLogs.submit(currentAddToAllLogTask);
        }
    }

    private void addToLog(String message) {
        synchronized (logMessages) {
            logMessages.add(message);
        }
        if (currentAddToLogTask == null || (currentAddToLogTask != null && currentAddToLogTask.isDone())) {
            currentAddToLogTask = new ScreeWriterTask(logMessages, logTextArea);
            executorServiceLog.submit(currentAddToLogTask);
        }
    }

    private void showHelpDialog() {
        Object helpText = ContentProperties.getInstance().getProperty("PublisherPanel.help");
        TextAreaDialog dlg = new TextAreaDialog(helpText.toString());
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(null);
        dlg.setTitle("Data Explorer Publish Help");
        dlg.getDialogPane().setHeaderText("You can send Publish and Delete commands from here. Selecting a line will only send that otherwise all the commands will be sent.");
        dlg.show();
    }

    private void showTopicSelection() {
        GuiUtil.showSelectionList(connection.getSowStatusService().getAllTopic(),
                "Choose a Topic", "Select a topic from list below.", this::selectedTopic);
    }

    private void selectedTopic(String topic) {
        addToLog("Selected topic " + topic);
        publishTextArea.appendText("\nPUBLISH=" + topic + " -> { }");
    }

    public void setRecordCountListener(Consumer<Integer> recordCountListener) {
        this.recordCountListener = recordCountListener;
        updateMessageSent();
    }

    private void processMessage(Message message) {
        String data = message.getData().trim();
        if (message.getCommand() == Message.Command.GroupBegin) {
            addToLogAndAmpsOutput("Got GroupBegin from topic " + message.getTopic());
        }
        if (message.getCommand() == Message.Command.GroupEnd) {
            addToLogAndAmpsOutput("Got GroupEnd from topic " + message.getTopic());
        }
        if (message.getCommand() == Message.Command.OOF) {
            addToLogAndAmpsOutput("Got OOF from topic " + message.getTopic() + " for " + data+ "\n");
            return;
        }
        if (data.length() > 0) {
            addToLogAndAmpsOutput("Got data from topic " + message.getTopic() + " -> " + data + "\n");
        }
    }

    private void updateMessageSent() {
        recordCountListener.accept(publishedMessage);
    }

    class ScreeWriterTask extends Task<Void> {

        private List<String> allMessages;
        private TextArea[] areaToWrite;

        public ScreeWriterTask(List<String> allMessages, TextArea... areaToWrite) {
            this.allMessages = allMessages;
            this.areaToWrite = areaToWrite;
        }

        @Override
        protected Void call() throws Exception {
            Thread.sleep(500);
            if (!isCancelled()) {
                List<String> messagesToWrite = new ArrayList<>();
                synchronized (allMessages) {
                    messagesToWrite.addAll(allMessages);
                    allMessages.clear();
                }
                StringBuilder builder = new StringBuilder();
                messagesToWrite.forEach(msg -> builder.append(msg + "\n"));
                final String text = builder.toString();
                messagesToWrite.clear();

                Platform.runLater(() -> {
                    try {
                        for (TextArea textarea : areaToWrite) {
                            textarea.appendText(text);
                        }
                    } catch (Exception e) {
                        LOG.error("Exception thrown while updating logs " + text, e);
                    }
                });
            }
            return null;
        }
    }
}
