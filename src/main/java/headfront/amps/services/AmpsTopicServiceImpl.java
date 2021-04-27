package headfront.amps.services;

import com.crankuptheamps.client.CommandId;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.MessageHandler;
import com.crankuptheamps.client.exception.AMPSException;
import headfront.amps.AmpsConnection;
import headfront.convertor.XmlConvertor;
import headfront.jetfuel.execute.JetFuelExecuteConstants;
import headfront.utils.MessageUtil;
import headfront.utils.PrimativeClassUtil;
import headfront.utils.StringUtils;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Deepak on 19/07/2016.
 */
public class AmpsTopicServiceImpl implements TopicService {

    private static final Logger LOG = LoggerFactory.getLogger(AmpsTopicServiceImpl.class);
    private XmlConvertor xmlConvertor = new XmlConvertor(true);
    private volatile CommandId currentCommand = null;
    private volatile CommandId currentCommand2 = null;
    private AmpsConnection connection;
    private Map<String, Map<String, Object>> allTopicDetails = new HashMap<>();
    private Map<Pair<String, String>, String> messageTypeToPortMapping = new HashMap<>();
    private List<String> allSowsAndType = new ArrayList<>();
    private List<String> allSowsNames = new ArrayList<>();

    private static final String TOPIC_STR = "Topic";
    private static final String CONFLATED_TOPIC_STR = "Conflated Topic";
    private static final String VIEW_STR = "View";
    private static final String QUEUE_STR = "Queue";

    private static final String DEFAULT_UNKNOWN = "-";
    private CheckBoxTreeItem<String> ampsStatsMetaData;
    private CountDownLatch waitForTopicReply = null;
    private Set<Runnable> metaDataListener = new HashSet<>();
    private Map<String, Object> latestAmpsData = new ConcurrentHashMap<>();
    private Map<String, String> userToID = new ConcurrentHashMap<>();
    private Map<String, TopicMetaData> cachedTopicMetaData = new ConcurrentHashMap<>();

    private String jetFuelExecuteFunctionBus = null;
    private String jetFuelExecuteFunction = null;

    public AmpsTopicServiceImpl(AmpsConnection connection) {
        this.connection = connection;
        connection.addConnectionStatusListener((connected, message) -> {
            if (!connected) {
                if (waitForTopicReply != null) {
                    long count = waitForTopicReply.getCount();
                    if (count != 0) {
                        waitForTopicReply.countDown();
                        count--;
                    }
                }
            }
        });
    }

    @Override
    public List<String> getAllTopics() {
        return allSowsAndType;
    }

    @Override
    public List<String> getAllTopicsNamesOnly() {
        return allSowsNames;
    }

    @Override
    public void setTopicDataFromConfig(String config) {
        allTopicDetails.clear();
        allSowsAndType.clear();
        allSowsNames.clear();
        Map<String, Object> stringObjectMap = xmlConvertor.convertToMap(config);
        Map<String, Object> sows = (Map<String, Object>) stringObjectMap.get("sow");
        // really bad code ahead.
        setTypeAndMessageTypeMapping(stringObjectMap);
        final HashMap actions = (HashMap) stringObjectMap.get("actions");
        try {
            if (actions != null) {
                final Collection values = actions.values();
                values.forEach(l -> {
                    if (l == null) {
                        return;
                    }
                    List list = (List) l;
                    list.forEach(m -> {
                        if (m == null) {
                            return;
                        }
                        Map map = (Map) m;
                        Object aDoObject = map.get("do");
                        if (aDoObject instanceof List) {
                            List aDoList = (List) aDoObject;
                            for (Object listMap : aDoList) {
                                Map properListMap = (Map) listMap;
                                if (properListMap.containsKey("module")) {
                                    final String module = properListMap.get("module").toString();
                                    if (module.equalsIgnoreCase("amps-action-do-publish-message")) {
                                        aDoObject = properListMap;
                                        break;
                                    }
                                }
                            }
                        }
                        if (aDoObject instanceof Map) {
                            Map aDo = (Map) aDoObject;
                            if (aDo.containsKey("module")) {
                                final String module = aDo.get("module").toString();
                                if (module.equalsIgnoreCase("amps-action-do-delete-sow")) {
                                    if (aDo.containsKey("options")) {
                                        final Map options = (Map) aDo.get("options");
                                        final Object topic = options.get("topic");
                                        final Object filter = options.get("filter");
                                        if (topic != null && filter != null) {
                                            String strTopic = topic.toString();
                                            String strFitler = filter.toString();
                                            if (strFitler.contains(JetFuelExecuteConstants.FUNCTION_PUBLISHER_NAME)) {
                                                // Dont choose topics with CLIENT
                                                if (strTopic != null && !strTopic.contains("CLIENT")) {
                                                    jetFuelExecuteFunction = strTopic;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (module.equalsIgnoreCase("amps-action-do-publish-message")) {
                                    if (aDo.containsKey("options")) {
                                        final Map options = (Map) aDo.get("options");
                                        final Object topic = options.get("topic");
                                        final Object filter = options.get("data");
                                        if (topic != null && filter != null) {
                                            String strTopic = topic.toString();
                                            String strFitler = filter.toString();
                                            if (strFitler.contains(JetFuelExecuteConstants.FUNCTION_CALLER_HOSTNAME)) {
                                                // Dont choose topics with CLIENT
                                                if (strTopic != null && !strTopic.contains("CLIENT")) {
                                                    jetFuelExecuteFunctionBus = strTopic;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                });
            }
        } catch (Exception e) {
            LOG.warn("Unable to set JetFuelExecute Configs. This not configured so its ok.");
        }

        processData(sows, "topic", this::processTopic);
        processData(sows, "topicdefinition", this::processTopic);
        processData(sows, "conflatedtopic", this::processConflatedTopic);
        processData(sows, "view", this::processViews);
        processData(sows, "viewdefinition", this::processViews);
        processData(sows, "queue", this::processQueues);
        processData(sows, "queuedefinition", this::processQueues);
    }

    private void setTypeAndMessageTypeMapping(Map<String, Object> stringObjectMap) {
        Map transports = (Map) stringObjectMap.get("transports");
        List transport = (List) transports.get("transport");
        transport.forEach(item -> {
            Map mapOfTransports = (Map) item;
            Object messageType = mapOfTransports.get("messagetype");
            if (messageType != null) {
                String port = mapOfTransports.get("inetaddr").toString();
                String type = mapOfTransports.get("type").toString();
                Pair pair = new Pair(messageType.toString(), type);
                messageTypeToPortMapping.put(pair, port);
            }
        });
        LOG.info("Loaded transports " + messageTypeToPortMapping);
    }

    @Override
    public void setFullAmpsJsonFile(Map<String, Object> allAmpsJson) {
        latestAmpsData.clear();
        latestAmpsData.putAll(allAmpsJson);
    }

    @Override
    public void setAmpsStatsMetaData(Map<String, Object> metaData) {
        CheckBoxTreeItem<String> root = new CheckBoxTreeItem<>("amps");
        addChild(metaData.get("amps"), root);
        this.ampsStatsMetaData = root;
        metaDataListener.forEach(Runnable::run);
    }

    private void addChild(Object datatToAdd, CheckBoxTreeItem<String> root) {
        if (datatToAdd instanceof Map) {
            Map map = (Map) datatToAdd;
            map.forEach((key, value) -> {
                CheckBoxTreeItem<String> newRoot = new CheckBoxTreeItem<>(key.toString());
                root.getChildren().add(newRoot);
                addChild(value, newRoot);
            });
        }
        if (datatToAdd instanceof List) {
            List list = (List) datatToAdd;
            list.forEach(item -> {
                if (item instanceof Map) {
                    Map mapItem = (Map) item;
                    Object id = mapItem.remove("id");
                    CheckBoxTreeItem<String> newRoot = new CheckBoxTreeItem<>(id.toString());
                    root.getChildren().add(newRoot);
                    addChild(mapItem, newRoot);
                }
//                else {
//                    root.getChildren().add(new CheckBoxTreeItem<>(datatToAdd.toString()));
//                }
            });
        }
//        else {
//            root.getChildren().add(new CheckBoxTreeItem<>(datatToAdd.toString()));
//        }

    }

    @Override
    public CheckBoxTreeItem<String> getAmpsStatsMetaData() {
        return ampsStatsMetaData;
    }

    private void processData(Map<String, Object> sows, String type, Consumer<Object> processData) {
        Object alldData = sows.get(type);
        if (alldData != null) {
            if (alldData instanceof List) {
                List listOfData = (List) alldData;
                for (Object data : listOfData) {
                    processData.accept(data);
                }
            } else {
                processData.accept(alldData);
            }
        }
    }

    private void processCommonFields(Map<String, Object> mapData, Map<String, Object> topicDetails) {
        checkAndAdd(mapData, topicDetails, TOPIC_NAME, "topic");
        checkAndAdd(mapData, topicDetails, TOPIC_NAME, "name");
        checkAndAdd(mapData, topicDetails, TOPIC_MESSAGE_TYPE, "messagetype");
        allTopicDetails.put(topicDetails.get(TOPIC_NAME).toString(), topicDetails);
        String type = (String) topicDetails.get(TOPIC_TYPE);
        if (type == null) {
            type = "Unknown";
        }
        allSowsAndType.add(topicDetails.get(TOPIC_NAME).toString() + " - " + type);
        allSowsNames.add(topicDetails.get(TOPIC_NAME).toString());
    }

    private void processConflatedTopic(Object conflatedTopic) {
        Map<String, Object> mapData = (Map) conflatedTopic;
        Map<String, Object> topicDetails = createTopicDetails();
        topicDetails.put(TOPIC_TYPE, CONFLATED_TOPIC_STR);
        topicDetails.put(TOPIC_SOW_ENABLED, "False");
        topicDetails.put(TOPIC_TXN_ENABLED, "False");
        processCommonFields(mapData, topicDetails);
        String underlyingTopic = (String) mapData.get("underlyingtopic");
        if (underlyingTopic != null) {
            Map<String, Object> underlyingTopicDetails = allTopicDetails.get(underlyingTopic);
            if (underlyingTopicDetails != null) {
                Object underlyingTopicKey = underlyingTopicDetails.get(TOPIC_KEY);
                if (underlyingTopicKey != null) {
                    topicDetails.put(TOPIC_KEY, underlyingTopicKey);
                } else {
                    topicDetails.put(TOPIC_KEY, "SOW_KEY");
                }
            }
        }
    }

    private void processTopic(Object topic) {
        Map<String, Object> mapData = (Map) topic;
        Map<String, Object> topicDetails = createTopicDetails();
        topicDetails.put(TOPIC_TYPE, TOPIC_STR);
        checkAndAdd(mapData, topicDetails, TOPIC_KEY, "key");
        checkAndAdd(mapData, topicDetails, TOPIC_EXPIRY, "expiration");
        Object fileName = mapData.get("filename");
        if (fileName != null) {
            topicDetails.put(TOPIC_SOW_ENABLED, "True");
        }
        processCommonFields(mapData, topicDetails);
    }

    private void processViews(Object view) {
        Map<String, Object> mapData = (Map) view;
        Map<String, Object> topicDetails = createTopicDetails();
        topicDetails.put(TOPIC_TYPE, VIEW_STR);
        topicDetails.put(TOPIC_EXPIRY, "Broker Transient");
        topicDetails.put(TOPIC_SOW_ENABLED, "False");
        topicDetails.put(TOPIC_TXN_ENABLED, "False");
        Object grouping = mapData.get("grouping");
        if (grouping != null) {
            List<String> keys = new ArrayList<>();
            Map<String, Object> mappedGrouping = (Map) grouping;
            Object fieldList = mappedGrouping.get("field");
            if (fieldList instanceof List) {
                List<String> fieldLists = (List) fieldList;
                fieldLists.forEach(val -> {
                    String stringValue = val.toString();
                    String fieldName = StringUtils.getFieldName(stringValue);
                    keys.add(fieldName);
                });
            } else {
                keys.add(StringUtils.getFieldName(fieldList.toString()));
            }
            topicDetails.put(TOPIC_KEY, keys);
        }
        processCommonFields(mapData, topicDetails);
    }

    private void processQueues(Object queue) {
        Map<String, Object> mapData = (Map) queue;
        Map<String, Object> topicDetails = createTopicDetails();
        topicDetails.put(TOPIC_TYPE, QUEUE_STR);
        processCommonFields(mapData, topicDetails);
    }

    private void checkAndAdd(Map<String, Object> sourceData, Map<String, Object> topicDetails, String fieldName, String fieldToCheck) {
        Object data = sourceData.get(fieldToCheck);
        if (data != null) {
            topicDetails.put(fieldName, data);
        }
    }

    public boolean isLargeSow(String topic) {
        Map<String, Object> data = connection.getSowStatusService().getTopicDetails(topic);
        if (data != null) {
            Object allKeys = data.get("record_count");
            if (allKeys != null) {
                Integer num = (Integer) allKeys;
                if (num > TopicService.NO_OF_RECORDS_TO_QUERY) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> getSowKey(String topic) {
        List<String> keys = new ArrayList<>();
        if (topic != null) {
            Map<String, Object> data = allTopicDetails.get(topic);
            if (data != null) {
                Object allKeys = data.get(TOPIC_KEY);
                if (allKeys instanceof List) {
                    List listKeys = (List) allKeys;
                    listKeys.forEach(k -> keys.add(k.toString().replaceFirst("/", "")));
                } else {
                    keys.add(allKeys.toString().replaceFirst("/", ""));

                }
                return keys;
            }
        }
        keys.add("ID");
        return keys;
    }

    public boolean doesTopicExists(String topic) {
        Map<String, Object> data = allTopicDetails.get(topic);
        return data != null;
    }


    private boolean isQueue(String topic) {
        Map<String, Object> data = allTopicDetails.get(topic);
        if (data != null) {
            String type = (String) data.get(TOPIC_TYPE);
            if (type != null) {
                if (type.equalsIgnoreCase("queue")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void clearTopicMetaData() {
        cachedTopicMetaData.clear();
    }


    @Override
    public TopicMetaData getTopicMetaData(String topic, String recordToSearch) {
        boolean largeSow = isLargeSow(topic);
        boolean doubleQuery = largeSow && (recordToSearch == null || recordToSearch.trim().length() == 0);
        if (doubleQuery) {
            final TopicMetaData topicMetaData = cachedTopicMetaData.get(topic);
            if (topicMetaData != null) {
                return topicMetaData.copy();
            }
        }
        if (connection.getConnected()) {
            try {
                if (currentCommand != null) {
                    connection.unsubscribe(currentCommand);
                    currentCommand = null;
                }
                if (currentCommand2 != null) {
                    connection.unsubscribe(currentCommand2);
                    currentCommand2 = null;
                }
                Set<String> records = new HashSet<>();
                Set<String> fields = new HashSet<>();
                Map<String, String> processedFields = new HashMap<>();
                List<String> sowKeys = getSowKey(topic);
                waitForTopicReply = new CountDownLatch(doubleQuery ? 2 : 1);
                MessageHandler handler = message -> {
                    String data = message.getData().trim();
                    if (data.length() > 0) {
                        data = StringUtils.removeInvalidCharFromJson(data);
                        Map<String, Object> mappedData = connection.getMessageConvertor(topic).convertToMap(data);
                        if (sowKeys.size() == 1) {
                            Object key = mappedData.get(sowKeys.get(0));
                            if (key != null) {
                                records.add(key.toString());
                            }
                        } else {
                            StringJoiner joiner = new StringJoiner(StringUtils.KEY_SEPERATOR);
                            sowKeys.forEach(k -> {
                                if (k.contains("/")) {
                                    String[] parts = k.split("/");
                                    Object leafNode = MessageUtil.getLeafNode(mappedData, parts[parts.length - 1]);
                                    joiner.add(leafNode == null ? "null" : leafNode.toString());

                                } else {
                                    Object o = mappedData.get(k);
                                    joiner.add(o == null ? "null" : o.toString());
                                }
                            });
                            String newKey = joiner.toString();
                            if (newKey.length() > 1) {
                                records.add(newKey);
                            }
                        }
                        mappedData.forEach((key, value) -> {
                            String oldValue = processedFields.get(key);
                            if (oldValue == null || oldValue.contains("null")) {
                                String primativeType = PrimativeClassUtil.getPrimativeType(value);
                                String keyWithValue = key + primativeType;
                                processedFields.put(key, keyWithValue);
                            }
                        });
                    }
                    if (message.getCommand() == Message.Command.GroupEnd) {
                        currentCommand = null;
                        fields.addAll(processedFields.values());
                        LOG.info("Got full reply from amps " + records.size() + " records and " + fields.size() + " fields.");
                        if (waitForTopicReply != null) {
                            waitForTopicReply.countDown();
                        }
                    }
                };
                if (doubleQuery) {
                    currentCommand = connection.getTopicDetails(topic, true, sowKeys, NO_OF_RECORDS_TO_QUERY / 2, recordToSearch, handler);
                    currentCommand2 = connection.getTopicDetails(topic, false, sowKeys, NO_OF_RECORDS_TO_QUERY / 2, recordToSearch, handler);
                } else {
                    currentCommand = connection.getTopicDetails(topic, true, sowKeys, NO_OF_RECORDS_TO_QUERY, recordToSearch, handler);
                }
                boolean await = waitForTopicReply.await(12, TimeUnit.SECONDS);// if after 12 seconds we dont have are respose bail out
                if (await) {
                    Map<String, Object> topicDetails = connection.getSowStatusService().getTopicDetails(topic);
                    waitForTopicReply = null;
                    TopicMetaData newTopicData = new TopicMetaData(getTopicDetails(topic, topicDetails),
                            createList(records), createList(fields));
                    final TopicMetaData topicMetaData = cachedTopicMetaData.get(topic);
                    if (topicMetaData != null) {
                        fields.addAll(topicMetaData.copy().getFields());
                    }
                    if (largeSow) {
                        cachedTopicMetaData.put(topic, newTopicData.copy());
                    }
                    return newTopicData;
                } else {
                    LOG.error("Got time out while processing getTopicMetaData for topic " + topic);
                    connection.unsubscribe(currentCommand);
                    connection.unsubscribe(currentCommand2);
                }
            } catch (Exception e) {
                LOG.error("Unable to process getTopicMetaData for topic " + topic + " message " + e.getMessage(), e);
            }
        }
        waitForTopicReply = null;
        return null;
    }

    private List<String> createList(Set<String> fields) {
        List<String> list = new ArrayList<>();
        list.addAll(fields);
        return list;
    }

    private int getCommandToUse(String topic, boolean subscribeOnly, boolean deltaSubscribe) {
        int command = Message.Command.SOWAndDeltaSubscribe;
        if (isQueue(topic)) {
            command = Message.Command.SOW;
        } else {
            if (subscribeOnly) {
                if (deltaSubscribe) {
                    command = Message.Command.DeltaSubscribe;
                } else {
                    command = Message.Command.Subscribe;
                }
            } else { // sow query
                if (deltaSubscribe) {
                    command = Message.Command.SOWAndDeltaSubscribe;
                } else {
                    command = Message.Command.SOWAndSubscribe;
                }
            }
        }
        return command;
    }

    @Override
    public CommandId subscribeToRecord(String topic, List<String> records, Consumer<Message> messageConsumer,
                                       boolean history, boolean subscribeOnly, boolean deltaSubscribe, String bookmark, String options) throws AMPSException {

        int command = getCommandToUse(topic, subscribeOnly, deltaSubscribe);
        if (records.isEmpty()) {
            return connection.subscribeTopic(topic, "", "", messageConsumer, history, command, bookmark, options);
        } else {
            List<String> keys = getSowKey(topic);
            String filter = "";
            if (keys.size() == 1) {
                String recordsToSearch = records.toString().replace("[", "").replace("]", "");
                recordsToSearch = "'" + recordsToSearch + "'";
                recordsToSearch = recordsToSearch.replace(", ", "','");
                filter = "/" + keys.get(0) + " IN (" + recordsToSearch + ")";
            } else {
                Set[] allFilters = new Set[keys.size()];
                for (int i = 0; i < allFilters.length; i++) {
                    allFilters[i] = new HashSet<String>();
                }
                for (String record : records) {
                    String[] split = record.split(StringUtils.KEY_SEPERATOR);
                    for (int i = 0; i < split.length; i++) {
                        allFilters[i].add(split[i]);
                    }
                }
                for (int i = 0; i < keys.size(); i++) {
                    boolean hasNull = false;
                    Set<String> nonNullData = new HashSet<>();
                    for (Object val : allFilters[i]) {
                        if (val == null || val.toString().equalsIgnoreCase("null")) {
                            hasNull = true;
                        } else {
                            nonNullData.add(val.toString());
                        }
                    }
                    if (nonNullData.size() > 0) {
                        String result = String.join("','", nonNullData);
                        String subFilter = "'" + result + "'";
                        if (hasNull) {
                            filter = filter + " (";
                        }
                        filter = filter + "/";
                        filter = filter + keys.get(i) + " IN (" + subFilter + ") ";
                        if (hasNull) {
                            filter = filter + " OR /" + keys.get(i) + " is null )";
                        }
                        filter = filter + " AND ";
                    } else {
                        filter = filter + "/" + keys.get(i) + " is null AND ";
                    }
                }

                filter = filter.substring(0, filter.length() - 4);
            }
            return connection.subscribeTopic(topic, filter, "", messageConsumer, history, command, bookmark, options);

        }
    }

    @Override
    public void addMetaDataListener(Runnable listener) {
        metaDataListener.add(listener);
    }

    @Override
    public CommandId subscribeToFilter(String topic, String filter, String OrderBy, Consumer<Message> messageConsumer,
                                       boolean history, boolean subscribeOnly, boolean deltaSubscribe, String bookmark, String options) throws AMPSException {

        int command = getCommandToUse(topic, subscribeOnly, deltaSubscribe);
        return connection.subscribeTopic(topic, filter, OrderBy, messageConsumer, history, command, bookmark, options);
    }

    public Map<String, Object> getTopicDetails(String topic, Map<String, Object> sowTopicDetails) {
        Map<String, Object> fullTopicDetails = allTopicDetails.get(topic);
        if (fullTopicDetails != null) {
            if (isQueue(topic)) {
                return fullTopicDetails;
            }
            if (sowTopicDetails != null) {
                fullTopicDetails.put(TOPIC_MESSAGE_TYPE, sowTopicDetails.get("message_type"));
                fullTopicDetails.put(TOPIC_NO_OF_RECORDS, sowTopicDetails.get("record_count"));
                return fullTopicDetails;
            }
        }
        return createTopicDetails();
    }

    private Map<String, Object> createTopicDetails() {
        Map<String, Object> topicDetails = new HashMap<>();
        topicDetails.put(TOPIC_NAME, DEFAULT_UNKNOWN);
        topicDetails.put(TOPIC_TYPE, DEFAULT_UNKNOWN);
        topicDetails.put(TOPIC_KEY, "/ID");
        topicDetails.put(TOPIC_MESSAGE_TYPE, DEFAULT_UNKNOWN);
        topicDetails.put(TOPIC_SOW_ENABLED, DEFAULT_UNKNOWN);
        topicDetails.put(TOPIC_TXN_ENABLED, DEFAULT_UNKNOWN);
        topicDetails.put(TOPIC_NO_OF_RECORDS, 0);
        topicDetails.put(TOPIC_EXPIRY, DEFAULT_UNKNOWN);
        return topicDetails;
    }

    @Override
    public List<String> getActiveUsers() {
        if (latestAmpsData != null) {
            final Object amps = latestAmpsData.get("amps");
            if (amps != null) {
                Map<Object, Object> ampsMap = (Map) amps;
                if (ampsMap != null) {
                    final Object instance = ampsMap.get("instance");
                    if (instance != null) {
                        Map<Object, Object> instanceMap = (Map) instance;
                        if (instanceMap != null) {
                            final Object clients = instanceMap.get("clients");
                            if (clients != null) {
                                List<Map<String, Object>> clientList = (List) clients;
                                List<String> clientsToReturn = new ArrayList<>();
                                for (Map<String, Object> aClient : clientList) {
                                    final String client_name = aClient.get("client_name").toString();
                                    clientsToReturn.add(client_name);
                                    userToID.put(client_name, aClient.get("id").toString());
                                }
                                return clientsToReturn;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> getActiveSubscriptions(String user) {
        final String userID = userToID.get(user);
        if (userID != null) {
            if (latestAmpsData != null) {
                final Object amps = latestAmpsData.get("amps");
                if (amps != null) {
                    Map<Object, Object> ampsMap = (Map) amps;
                    if (ampsMap != null) {
                        final Object instance = ampsMap.get("instance");
                        if (instance != null) {
                            Map<Object, Object> instanceMap = (Map) instance;
                            if (instanceMap != null) {
                                final Object subscriptions = instanceMap.get("subscriptions");
                                if (subscriptions != null) {
                                    List<Map<String, Object>> subscriptionsList = (List) subscriptions;
                                    List<Map<String, Object>> subscriptionsToReturn = new ArrayList<>();
                                    for (Map<String, Object> sub : subscriptionsList) {
                                        final String client_id = sub.get("client_id").toString();
                                        if (client_id.equals(userID)) {
                                            Map<String, Object> newMap = new HashMap<>(sub);
                                            newMap.put("client_name", user);
                                            subscriptionsToReturn.add(newMap);
                                        }
                                    }
                                    return subscriptionsToReturn;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public String getJetFuelExecuteFunctionBus() {
        return jetFuelExecuteFunctionBus;
    }

    public String getJetFuelExecuteFunction() {
        return jetFuelExecuteFunction;
    }
}
