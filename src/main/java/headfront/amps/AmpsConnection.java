package headfront.amps;

import com.crankuptheamps.client.*;
import com.crankuptheamps.client.exception.AMPSException;
import com.crankuptheamps.client.exception.ConnectionException;
import com.crankuptheamps.client.exception.DisconnectedException;
import headfront.amps.services.SowStatusService;
import headfront.convertor.JacksonJsonConvertor;
import headfront.convertor.MessageConvertor;
import headfront.convertor.NvFixConvertor;
import headfront.guiwidgets.PopUpDialog;
import headfront.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Deepak on 28/03/2016.
 */
public class AmpsConnection {

    private static final Logger LOG = LoggerFactory.getLogger(AmpsConnection.class);

    public static final String SOW_STATS_TOPIC = "/AMPS/SOWStats";
    public static final String CLIENT_STATUS_TOPIC = "/AMPS/ClientStatus";
    public static final String OOF_KEY = "OutOfFocus";
    public static final String SOW_KEY = "SOW_KEY";
    public static final String TIMESTAMP = "AmpsTimeStamp";
    private boolean notifyCantconnectInitally = false;

    private MessageConvertor defaultMessageConvertor = new JacksonJsonConvertor();
    private volatile HAClient ampsHaClient;
    private String connectionName;
    private String connectionURI;
    private Runnable forceStopListener;
    public static final int MAX_RECORDS_PER_SUBSCRIPTION = 10000;

    private int heartbeatIntervalSecs = 10;
    private boolean appendUsernameToConnectionName = true;
    private boolean appendHostnameToConnectionName = false;
    private final Map<CommandId, String> currentSubscriptions = new HashMap<>();
    private Map<String, MessageConvertor> messageConvertors = new HashMap<>();
    private long publishCount = 0;
    private long unPublishCount = 0;
    private Boolean connected = false;
    private int bookmarkSubCount = 0;
    private SowStatusService sowStatusService = new SowStatusService(defaultMessageConvertor);
    private List<BiConsumer<Boolean, String>> connectionStatusListeners = new ArrayList<>();

    public AmpsConnection(String conName, String conURI, Runnable forceStopListener) {
        this.connectionName = conName;
        this.connectionURI = conURI;
        this.forceStopListener = forceStopListener;
        if (connectionURI == null) {
            connectionURI = "tcp://192.168.56.101:8001/amps/json";
        }
//        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanShutDown));
    }


    public void initialize() {
        final String fullConnectionName = getConnectionNameToUse();
        try {
            this.ampsHaClient = new HAClient(fullConnectionName);
            CountDownLatch initialConnect = new CountDownLatch(1);
            final ServerChooser sc = new ServerChooser(state -> {
                connected = state;
                sendFirstConnectionStatus();
            }, forceStopListener);
            final String[] servers = connectionURI.split(",");
            for (String server : servers) {
                sc.add(server);
            }
            setupAmpsConnection();
            ampsHaClient.setBookmarkStore(new MemoryBookmarkStore());
            ampsHaClient.setServerChooser(sc);
            ampsHaClient.setHeartbeat(heartbeatIntervalSecs);
            String conNameNoPassword = StringUtils.removePassword(connectionURI);
            LOG.info("Trying to connect to Amps Server " + conNameNoPassword);
            new Thread(() -> {
                try {
                    ampsHaClient.connectAndLogon();
                    initialConnect.countDown();
                } catch (ConnectionException e) {
                    LOG.error("Unable to connect to Amps Server " + e, e);
                }
            }).start();
            // wait to be connected
            initialConnect.await();
            ampsHaClient.sowAndDeltaSubscribe(sowStatusService, SOW_STATS_TOPIC, 1000);
            LOG.info("Connected to Amps Server with name " + fullConnectionName);
        } catch (Exception e) {
            LOG.error("Unable to connect to Amps Server " + e, e);
        }
    }

    public void sendConnectionStatus() {
        connectionStatusListeners.forEach(l -> l.accept(connected, connected ? "Connected to Amps" : "Disconnected from Amps"));
    }

    public void sendFirstConnectionStatus() {
        sendConnectionStatus();
        if (!notifyCantconnectInitally) {
            notifyCantconnectInitally = true;
            if (!connected) {
                PopUpDialog.showWarningPopup("Connection error", "Cant Connect to Amps", 5000);
            }
        }
    }

    public Boolean getConnected() {
        return connected;
    }

    public MessageConvertor getMessageConvertor(String topic) {
        String topicMessageType = sowStatusService.getTopicMessageType(topic);
        if (topicMessageType != null) {
            MessageConvertor messageConvertor = messageConvertors.get(topicMessageType);
            if (messageConvertor != null) {
                return messageConvertor;
            }

        }
        return defaultMessageConvertor;
    }

    private void setupAmpsConnection() {
        ampsHaClient.setFailedWriteHandler(new FailureHandler());
        ampsHaClient.setExceptionListener(e -> LOG.error("Exception thrown in the AMPS library.", e));
        ampsHaClient.setLastChanceMessageHandler(message -> LOG.error("Message from LastChanceHandler " + message.toString()));
        messageConvertors.put("json", new JacksonJsonConvertor());
        messageConvertors.put("fix", new NvFixConvertor());
        messageConvertors.put("nvfix", new NvFixConvertor());
    }

    public void publishDelta(final String topic, final String id, final Map<String, Object> mappedData) throws Exception {
        if (mappedData.size() > 0) {
            final String messageToSend = getMessageConvertor(topic).convertToString(mappedData);
            if (messageToSend != null) {
                publishDelta(topic, id, messageToSend);
            }
        }
    }

    public void publish(final String topic, final String id, final String message) throws Exception {
        if (message != null && message.length() > 0) {
            try {
                ampsHaClient.publish(topic, message);
                publishCount++;
            } catch (Exception e) {
                LOG.error("Unable to publish message to " + topic + " with id " + id +
                        " and data " + message, e);
                throw e;
            }
        }
    }

    public void publishDelta(final String topic, final String id, final String message) throws Exception {
        if (message != null && message.length() > 0) {
            try {
                ampsHaClient.deltaPublish(topic, message);
                publishCount++;
            } catch (Exception e) {
                LOG.error("Unable to publish Delta message to " + topic + " with id " + id +
                        " and data " + message, e);
                throw e;
            }
        }
    }

    public void deleteCommand(final String topic, final String deleteCommand) throws Exception {
        try {
            ampsHaClient.sowDelete(topic, deleteCommand, 10000);
            unPublishCount++;
            LOG.debug("Sent delete command  " + deleteCommand + " to topic " + topic);
        } catch (Exception e) {
            LOG.error("Unable to send delete command  " + deleteCommand + " to topic " + topic, e);
            throw e;
        }
    }

    public void deleteRecord(final String topic, final String idField, final String id) throws Exception {
        try {
            ampsHaClient.sowDelete(topic, "/" + idField + " = '" + id + "'", 10000);
            unPublishCount++;
            LOG.debug("Deleted record id " + id + " from topic " + topic);
        } catch (Exception e) {
            LOG.error("Unable to delete message to " + topic + " id " + id, e);
            throw e;
        }
    }

    public CommandId subscribeTopic(final String topicToSubscribe, final String subscriptionFilter, String orderBy,
                                    final Consumer<Message> consumer, boolean history, int command, String options) throws AMPSException {
        return subscribeTopic(topicToSubscribe, subscriptionFilter, orderBy, consumer, history, command, Client.Bookmarks.EPOCH, options);

    }

    public CommandId subscribeTopic(final String topicToSubscribe, final String subscriptionFilter, String orderBy,
                                    final Consumer<Message> consumer, boolean history, int command, String bookmarkStart, String options) throws AMPSException {
        CommandId commandId = null;
        try {
            Command commandToSend = null;
            if (history) {
                commandToSend = new Command(Message.Command.Subscribe);
                commandToSend.setBookmark(bookmarkStart);
                commandToSend.addAckType(Message.AckType.Completed);
                commandToSend.setOptions(Message.Options.Timestamp + options);
            } else {
                commandToSend = new Command(command);
                commandToSend.setOptions(Message.Options.SendKeys + Message.Options.NoEmpties + Message.Options.OOF + Message.Options.Timestamp + options);
                commandToSend.setBatchSize(1000);
                commandToSend.setAckType(Message.AckType.Stats);
                commandToSend.setTopN(MAX_RECORDS_PER_SUBSCRIPTION);
            }
            commandToSend.setTopic(topicToSubscribe.trim());
            commandToSend.setFilter(subscriptionFilter);
            if (orderBy != null && orderBy.trim().length() > 0) {
                commandToSend.setOrderBy(orderBy);
            }

            String subscription = "Topic[" + topicToSubscribe + "] filter[" + subscriptionFilter + "] subscribeType [" + command + "]";
            LOG.info("Created subscription request " + commandToSend);
            commandToSend.setTimeout(10000);
            commandId = ampsHaClient.executeAsync(commandToSend, consumer::accept);
            currentSubscriptions.put(commandId, subscription);
            LOG.info("Subscribed to " + subscription + " with history = " + history + " and commandID " + commandId);

        } catch (AMPSException e) {
            LOG.error("Unable to subscribe to topic " + topicToSubscribe + " with filter " + subscriptionFilter, e);
            throw e;
        }
        return commandId;
    }

    private void sendPageRequest(Long startID, String topic, String filter) throws AMPSException {
        Message message = ampsHaClient.allocateMessage();
        message.setCommand(Message.Command.SOW);
        CommandId id = CommandId.nextIdentifier();
        message.setCommandId(id);
        message.setQueryId(id);
        message.setAckType(Message.AckType.Stats);
        message.setTopic(topic);
        message.setBatchSize(500);
        message.setTopN(1000);
        if (startID != 0) {
            filter = filter + " and /ID <" + startID;
        }
        message.setFilter(filter);
        message.setOrderBy("/ID DESC");
        ampsHaClient.send(this::processMessage, message, 1000);
    }

    public CommandId getTopicDetails(String topic, boolean desc, List<String> idKey, int rows, String idFilter, MessageHandler handler) throws Exception {
        if (getConnected()) {
            try {
                Command getTopicDetailsCommand = new Command(Message.Command.SOW);
//                CommandId newId = CommandId.nextIdentifier();
//                getTopicDetailsCommand.setCommandId(newId);
//                getTopicDetailsCommand.setSubId(newId);
//                getTopicDetailsCommand.setQueryId(newId);
//                getTopicDetailsCommand.setCommand();
                getTopicDetailsCommand.setAckType(Message.AckType.Stats);
                getTopicDetailsCommand.setTopic(topic);
                getTopicDetailsCommand.setBatchSize(rows);
                getTopicDetailsCommand.setTopN(rows);
                if (desc) {
                    getTopicDetailsCommand.setOrderBy("/" + idKey.get(0) + " DESC");
                } else {
                    getTopicDetailsCommand.setOrderBy("/" + idKey.get(0) + " ASC");
                }
                if (idFilter != null && idFilter.toString().length() > 0) {
                    if (idKey.size() == 1) {
                        getTopicDetailsCommand.setFilter("/" + idKey.get(0) + " LIKE '(?i)" + idFilter + "'");
                    } else {
                        String filter = "";
                        for (String s : idKey) {
                            filter = filter + "/" + s + " LIKE '(?i)" + idFilter + "' OR ";
                        }
                        filter = filter.substring(0, filter.length() - 3);
                        getTopicDetailsCommand.setFilter(filter);
                    }
                } else {
                    getTopicDetailsCommand.setFilter("1=1");
                }
                getTopicDetailsCommand.setTimeout(10000);
                LOG.info("Sending Topic Details query " + getTopicDetailsCommand + " to amps");
                return ampsHaClient.executeAsync(getTopicDetailsCommand, handler);
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    private void processMessage(Message m) {
        // store matches and last id
        String data = m.getData();
        if (m.getCommand() == Message.Command.Ack) {
            System.out.println("Got ack we will have " + m.getMatches() + " matches");
        } else {
            System.out.println("Data topic " + m.getTopic() + " message " + data);
            // get last id from here to use for next query
        }
    }


    public void unsubscribe(CommandId commandId) {
        if (commandId != null) {
            if (ampsHaClient != null && getConnected()) {
                try {
                    ampsHaClient.unsubscribe(commandId);
                    LOG.info("Unsubscribed to commandID " + commandId + ", " + currentSubscriptions.get(commandId));
                } catch (DisconnectedException e) {
                    LOG.error("Unable to unsubscribe commandID " + commandId + ", " + currentSubscriptions.get(commandId), e);
                }
            }
        }
    }

    public void unsubscribeAll() {
        if (ampsHaClient != null) {
            try {
                ampsHaClient.unsubscribe();
            } catch (DisconnectedException e) {
                LOG.error("Unable to close all subscriptions", e);
            }
        }
    }

    public String getConnectionNameToUse() {
        StringBuilder builder = new StringBuilder(connectionName);
        if (appendUsernameToConnectionName) {
            builder.append("_").append(System.getProperty("user.name"));
        }
        if (appendHostnameToConnectionName) {
            builder.append("_").append(getHostname());
        }
        return builder.toString();
    }


    public void cleanShutDown() {
        LOG.info("Doing a clean shut down of amps");
        if (ampsHaClient != null) {
//            unsubscribeAll();
            ampsHaClient.disconnect();
            ampsHaClient.close();
        }
    }

    public HAClient getHaClient() {
        return ampsHaClient;
    }

    private String getHostname() {
        try {
            final String hostName = InetAddress.getLocalHost().getHostName();
            if (hostName.contains(".")) {
                return hostName.substring(0, hostName.indexOf("."));
            }
            return hostName;
        } catch (final Exception e1) {
            LOG.error("Could not get the current machaine name", e1);
            return "HOST_UNKNOWN";
        }
    }


    public void setAppendUsernameToConnectionName(boolean appendUsernameToConnectionName) {
        this.appendUsernameToConnectionName = appendUsernameToConnectionName;
    }

    public void setAppendHostnameToConnectionName(boolean appendHostnameToConnectionName) {
        this.appendHostnameToConnectionName = appendHostnameToConnectionName;
    }

    public SowStatusService getSowStatusService() {
        return sowStatusService;
    }

    public void addConnectionStatusListener(BiConsumer<Boolean, String> connectionStatusListener) {
        connectionStatusListeners.add(connectionStatusListener);
        sendConnectionStatus();
    }


    private static class FailureHandler implements FailedWriteHandler {

        @Override
        public void failedWrite(Message message, int i) {

            LOG.error("Unable to write this message " +
                    "sequenceNumber " + message.getSequence() +
                    ", operation " + i +
                    ", topic " + message.getTopic() +
                    ", data " + message.getData() +
                    ", correlationId " + message.getCorrelationId() +
                    ", reason " + message.getReasonText());
        }
    }

}
