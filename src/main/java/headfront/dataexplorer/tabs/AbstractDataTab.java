package headfront.dataexplorer.tabs;

import com.crankuptheamps.client.CommandId;
import com.crankuptheamps.client.Message;
import headfront.amps.AmpsConnection;
import headfront.convertor.MessageConvertor;
import headfront.dataexplorer.DataExplorerSelection;
import headfront.guiwidgets.PopUpDialog;
import headfront.utils.StringUtils;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Deepak on 06/07/2016.
 */
public abstract class AbstractDataTab extends Tab {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final AmpsConnection connection;
    private boolean showHistory;
    private MessageConvertor messageConvertor;
    private DataExplorerSelection selection;
    private List<String> recordIds;
    private List<Object> messageToProcess = new ArrayList<>();
    private final Object dataLock = new Object();
    private long receivedMessagesCount = 0;
    private int recordCount = 0;
    protected int messagesWithNoID= 0;
    private CommandId ampsCommandId;
    private Consumer<Integer> recordCountListener = s -> {
    };
    private Consumer<Long> msgCountListener = s -> {
    };
    private Consumer<String> subscriptionStatusListener = s -> {
    };
    private boolean sendUpdate;
    private static final String GROUP_BEGIN_MSG = "Starting to receive sow data";
    private static final String GROUP_END_MSG = "Received all sow data";
    private String lastSubscriptionStatus = "New Window";
    private boolean decodeMessage = true;
    protected List<String> interestedFields = new ArrayList<>();
    private Consumer<DataExplorerSelection> editSelectionPressed;

    /**
     * @param recordIds is actual ids of the record eg ID not the recordid eg Isin
     */
    public AbstractDataTab(String tabName, AmpsConnection connection, boolean showHistory, MessageConvertor messageConvertor,
                           DataExplorerSelection selection, List<String> recordIds) {
        super(tabName);
        this.connection = connection;
        this.showHistory = showHistory;
        this.messageConvertor = messageConvertor;
        this.selection = selection;
        this.recordIds = recordIds;
        createMessageProcessor();
    }

    protected void createTab() {
        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(createContent());
        Node bottomPanel = createBottomPanel();
        if (bottomPanel != null) {
            mainPane.setBottom(bottomPanel);
        }
        setContent(mainPane);
        setOnClosed(val -> {
            stopSubscription();
        });
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                startSendingUpdates();
            } else {
                stopSendingUpdate();
            }
        });
        setTooltip(new Tooltip(selection.getDescription()));
//        ContextMenu contextMenu = new ContextMenu();
//        MenuItem menuItem = new MenuItem("Edit");
//        menuItem.setOnAction(new EventHandler<ActionEvent>(){
//            @Override public void handle(ActionEvent e){
//                editSelectionPressed.accept(selection);
//            }
//        });
//        contextMenu.getItems().add(menuItem);
//        setContextMenu(contextMenu);
    }

    protected Node createBottomPanel() {
        return null;
    }

    public void stopSubscription() {
        if (connection != null) {
            connection.unsubscribe(ampsCommandId);
        }
    }

    private void stopSendingUpdate() {
        sendUpdate = false;
    }

    private void startSendingUpdates() {
        sendUpdate = true;
        msgCountListener.accept(receivedMessagesCount);
        recordCountListener.accept(recordCount);
        subscriptionStatusListener.accept(lastSubscriptionStatus);
    }

    private void createMessageProcessor() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        final ScheduledFuture scheduledFuture =
                scheduledExecutorService.scheduleAtFixedRate(() -> {
                    List<Object> messages;
                    synchronized (dataLock) {
                        if (messageToProcess.size() <= 0) {
                            return;
                        }
                        messages = messageToProcess;
                        messageToProcess = new ArrayList<>();
                    }
                    if (decodeMessage && !showHistory) {
                        // merge data but preserve the order just in case.
                        //if we dont have a sow key then still publish the message.
                        List<Object> dataToProcess = new ArrayList<>();
                        Map<Object, Map<String, Object>> dataBySowKey = new HashMap<>();
                        messages.forEach(obj -> {
                            Map<String, Object> newData = (Map<String, Object>) obj;
                            Object sowKey = newData.get(AmpsConnection.SOW_KEY);
                            if (sowKey == null) {
                                dataToProcess.add(newData);
                            } else {
                                //merge
                                Map<String, Object> oldData = dataBySowKey.get(sowKey);
                                if (oldData == null) {
                                    dataToProcess.add(newData);
                                    dataBySowKey.put(sowKey, newData);
                                } else {
                                    oldData.putAll(newData);
                                }
                            }
                        });
                        updateModel(dataToProcess);
                    } else {
                        updateModel(messages);
                    }
                    //@todo if we need message count
//                    if (sendUpdate) {
//                        msgCountListener.accept(receivedMessagesCount);
//                    }
                }, 100, 100, TimeUnit.MILLISECONDS);

        ScheduledExecutorService loggingService =
                Executors.newSingleThreadScheduledExecutor();
        loggingService.schedule(() -> {
            try {
                scheduledFuture.get();
            } catch (Exception e) {
                LOG.error("Exception thrown while processing messages ", e);
            }

        }, 1, TimeUnit.MILLISECONDS);
    }

    public void updateLastSubscriptionStatus(String message) {
        lastSubscriptionStatus = message;
        if (sendUpdate) {
            subscriptionStatusListener.accept(lastSubscriptionStatus);
        }
    }

    public void processAmpsMessage(Message message) {
        //@todo look at other states
        if (message.getCommand() == Message.Command.GroupBegin) {
            updateLastSubscriptionStatus(GROUP_BEGIN_MSG);
        }
        if (message.getCommand() == Message.Command.GroupEnd) {
            updateLastSubscriptionStatus(GROUP_END_MSG);
        }
        if (message.getCommand() == Message.Command.Ack && message.getAckType() == Message.AckType.Stats) {
            final long allValues = message.getMatches();
            if (allValues > AmpsConnection.MAX_RECORDS_PER_SUBSCRIPTION) {
                final NumberFormat numberFormat = NumberFormat.getInstance();
                final String formatedNumber = numberFormat.format(AmpsConnection.MAX_RECORDS_PER_SUBSCRIPTION);
                PopUpDialog.showInfoPopup("Too many records", "Large number of records was requested. " +
                        " Please refine your query, this view will only show the first " + formatedNumber + " records.");
                updateLastSubscriptionStatus("Restricting to " + formatedNumber + " records as too many were requested");
            }
        }
        if (decodeMessage) {
            processMessage(message);
        } else {
            sendRawMessage(message);
        }
    }

    private void sendRawMessage(Message message) {
        String dataToProcess = message.getData().trim();
        if (dataToProcess.length() > 0) {
            receivedMessagesCount++;
            try {
                String processedMessage = dataToProcess;
                if (message.getCommand() == Message.Command.OOF) {
                    processedMessage = "Received Delete " + dataToProcess;
                } else {
                    processedMessage = "Received Message " + dataToProcess;
                }
                synchronized (dataLock) {
                    messageToProcess.add(processedMessage);
                }
            } catch (Exception e) {
                LOG.warn("Unable to process message " + dataToProcess);
            }
        }
    }

    protected void processMessage(Message message) {
        String dataToProcess = message.getData().trim();
        if (dataToProcess.length() > 0) {
            receivedMessagesCount++;
            try {
                dataToProcess = StringUtils.removeInvalidCharFromJson(dataToProcess);
                Map<String, Object> dataMap = messageConvertor.convertToMap(dataToProcess);
                if (message.getCommand() == Message.Command.OOF) {
                    dataMap.put(AmpsConnection.OOF_KEY, true);
                } else {
                    dataMap.put(AmpsConnection.OOF_KEY, false);
                }
                dataMap.put(AmpsConnection.SOW_KEY, message.getSowKey());
                String timestamp = message.getTimestamp();
                if (timestamp != null) {
                    dataMap.put(AmpsConnection.TIMESTAMP, StringUtils.formatToReadableDate(timestamp));
                }
                synchronized (dataLock) {
                    messageToProcess.add(dataMap);
                }
            } catch (Exception e) {
                LOG.warn("Unable to process message " + dataToProcess, e);
            }
        }
    }

    public void setAmpsSubCommandID(CommandId ampsCommandId) {
        this.ampsCommandId = ampsCommandId;
        if (showHistory) {
            updateLastSubscriptionStatus("Subscribed to Amps. Waiting for journaled data from txn logs !!!");
        } else {
            updateLastSubscriptionStatus("Subscribed to Amps");
        }
    }

    public void clearData() {
        receivedMessagesCount = 0;
        sendRecordCount(0);
    }

    protected abstract Node createContent();

    public abstract void updateModel(List<? extends Object> messages);

    public void setDecodeMessage(boolean decodeMessage) {
        this.decodeMessage = decodeMessage;
    }

    public void setMessageCountListener(Consumer<Long> msgCountListener) {
        this.msgCountListener = msgCountListener;
    }

    public void onEditPressed(Consumer<DataExplorerSelection> editSelectionPressed) {
        this.editSelectionPressed = editSelectionPressed;
    }

    public void setRecordCountListener(Consumer<Integer> recordCountListener) {
        this.recordCountListener = recordCountListener;
    }

    public void setSubscriptionStatus(Consumer<String> subscriptionStatusListener) {
        this.subscriptionStatusListener = subscriptionStatusListener;
    }

    protected void sendRecordCount(int size) {
        recordCount = size;
        if (sendUpdate) {
            recordCountListener.accept(size);
        }
    }

    protected void addInterestedFields() {
        List<String> fields = selection.getFields();
        fields.forEach(field -> {
            String fieldToAdd = StringUtils.removeBrackets(field);
            if (!interestedFields.contains(fieldToAdd)) {
                interestedFields.add(fieldToAdd);
            }
        });
        // if we added fields then add core fields
        if (fields.size() > 0) {
            recordIds.forEach(idField ->{
                if (!interestedFields.contains(idField)) {
                    interestedFields.add(idField);
                }
            });
            interestedFields.add(AmpsConnection.OOF_KEY);
            interestedFields.add(AmpsConnection.TIMESTAMP);
        }
    }

    protected Map<String, Object> filterInterestedFields(Map<String, Object> dataMap) {
        Map<String, Object> filteredMap = new HashMap<String, Object>();
        if (interestedFields.size() > 0) {
            for (String field : interestedFields) {
                Object o = dataMap.get(field);
                if (o != null) {
                    filteredMap.put(field, o);
                }
            }
        } else {
            filteredMap = dataMap;
        }
        return filteredMap;
    }

    public void updateRecordsWithNoMessageCount() {
        messagesWithNoID++;
        Platform.runLater(() -> {
            updateLastSubscriptionStatus("Received " + messagesWithNoID + " messages with no ID and cant display it");
        });
    }

}