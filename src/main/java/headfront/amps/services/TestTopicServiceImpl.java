package headfront.amps.services;

import com.crankuptheamps.client.CommandId;
import com.crankuptheamps.client.Message;
import headfront.convertor.JacksonJsonConvertor;
import headfront.convertor.MessageConvertor;
import javafx.scene.control.CheckBoxTreeItem;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Deepak on 28/06/2016.
 */
public class TestTopicServiceImpl implements TopicService {

    private final Random random = new Random();
    private MessageConvertor jsonConvertor = new JacksonJsonConvertor();

    @Override
    public List<String> getAllTopics() {
        String[] topicsFromServer = {"Deepak", "UAT_CLIENT_BOND_RISK_VIEW", "empty", "TRADE_HISTORY", "ORDER", "TRADES", "LONDON TRADES", "NY TRADES", "FFT TRADES", "/AMPS/NEW", "orders-new", "trades-new", "amps"};
        return Collections.unmodifiableList(Arrays.asList(topicsFromServer));

    }

    @Override
    public List<String> getAllTopicsNamesOnly() {
        String[] topicsFromServer = {"Deepak", "UAT_CLIENT_BOND_RISK_VIEW", "empty", "TRADE_HISTORY", "ORDER", "TRADES", "LONDON TRADES", "NY TRADES", "FFT TRADES", "/AMPS/NEW", "orders-new", "trades-new", "amps"};
        return Collections.unmodifiableList(Arrays.asList(topicsFromServer));

    }

    @Override
    public void setTopicDataFromConfig(String config) {

    }

    @Override
    public void setFullAmpsJsonFile(Map<String, Object> allAmpsJson) {

    }

    @Override
    public void setAmpsStatsMetaData(Map<String, Object> metaData) {

    }

    @Override
    public CheckBoxTreeItem<String> getAmpsStatsMetaData() {
        return new CheckBoxTreeItem<String>("Empty Root");
    }

    @Override
    public List<String> getSowKey(String topic) {
        return Arrays.asList("ID");
    }

    public boolean isLargeSow(String topic) {
        return false;
    }

    @Override
    public boolean doesTopicExists(String topic) {
        return false;
    }

    public Map<String, Object> getTopicDetails(String topic) {
        Map<String, Object> topicDetails = new HashMap<>();
        topicDetails.put(TOPIC_NAME, topic);
        topicDetails.put(TOPIC_TYPE, "Topic");
        topicDetails.put(TOPIC_KEY, "ID");
        topicDetails.put(TOPIC_MESSAGE_TYPE, "json");
        topicDetails.put(TOPIC_SOW_ENABLED, "true");
        topicDetails.put(TOPIC_TXN_ENABLED, "true");
        topicDetails.put(TOPIC_NO_OF_RECORDS, 4500);
        topicDetails.put(TOPIC_EXPIRY, "25h");
        return topicDetails;
    }

    @Override
    public TopicMetaData getTopicMetaData(String topic, String recordToSearch) {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return new TopicMetaData(getTopicDetails(topic), getRecords(topic), getFields(topic));
    }

    @Override
    public void clearTopicMetaData() {

    }

    public List<String> getRecords(String topic) {

        int rows = 99;
        if (topic.toLowerCase().contains("history")) {
            rows = (random.nextInt(3) + 1) * 1000000 + random.nextInt(1000000);
        } else if (topic.toLowerCase().contains("empty")) {
            rows = 0;
        } else {
            rows = (random.nextInt(6) + 1) * 1000 + random.nextInt(1000);
        }
        List<String> recordList = new ArrayList<>();
        String checkedTopic;
        if (topic.length() < 3) {
            checkedTopic = "TEST";
        } else {
            checkedTopic = topic;
        }
        IntStream.rangeClosed(1, rows).forEach(i ->
                recordList.add(checkedTopic.substring(0, 3) + i));
        return recordList;
    }


    private List<String> getFields(String topic) {
        List<String> fieldList = new ArrayList<>();
        fieldList.add("ID_" + topic);
        fieldList.add("INST_" + topic);
        fieldList.add("DESC_" + topic);
        fieldList.add("BidSpread_" + topic);
        fieldList.add("OfferSpread_" + topic);
        fieldList.add("Bid_" + topic);
        fieldList.add("BidQty_" + topic);
        fieldList.add("Offer_" + topic);
        fieldList.add("OfferQty_" + topic);
        IntStream.range(1, random.nextInt(50)).forEach(i -> fieldList.add("Field_" + i + " [int]"));
        return fieldList;
    }

    private List<String> getFieldsWithExData(String topic) {
        List<String> fieldListWithExamples = new ArrayList<>();
        fieldListWithExamples.add("ID " + topic + " = 'DE123'");
        fieldListWithExamples.add("INST " + topic + " = 'UST00074835'");
        fieldListWithExamples.add("DESC " + topic + " = 'US 3 yeat note'");
        fieldListWithExamples.add("BidSpread " + topic + " = 100.3");
        fieldListWithExamples.add("OfferSpread " + topic + " = 0.09");
        fieldListWithExamples.add("Bid " + topic + " = 100.1");
        fieldListWithExamples.add("BidQty " + topic + " = 10");
        fieldListWithExamples.add("Offer " + topic + " = 101.2");
        fieldListWithExamples.add("OfferQty " + topic + " = 15");
        return fieldListWithExamples;
    }

    @Override
    public CommandId subscribeToRecord(String topic, List<String> records, Consumer<Message> messageConsumer,
                                       boolean history, boolean subscribeOnly, boolean deltaSubscribe, String bookmark, String options) {
        subscribe(topic, messageConsumer);
        return null;
    }

    @Override
    public CommandId subscribeToFilter(String topic, String filter, String OrderBy, Consumer<Message> messageConsumer,
                                       boolean history, boolean subscribeOnly, boolean deltaSubscribe, String bookmark, String options) {
        subscribe(topic, messageConsumer);
        return null;
    }

    @Override
    public void addMetaDataListener(Runnable listener) {

    }

    @Override
    public List<String> getActiveUsers() {
        return null;
    }

    @Override
    public List<Map<String, Object>> getActiveSubscriptions(String user) {
        return null;
    }

    @Override
    public String getJetFuelExecuteFunctionBus() {
        return "FUNCTIONS_BUS";
    }

    @Override
    public String getJetFuelExecuteFunction() {
        return "FUNCTIONS";
    }

    private void subscribe(String topic, Consumer<Message> messageConsumer) {
        new Thread(() -> {
            while (true) {

                Message mockedMessage = mock(Message.class);
                when(mockedMessage.getData()).thenReturn(createTestMessage(topic));
                int i = random.nextInt(3);
                int command = 0;
                if (i == 0) {
                    command = Message.Command.GroupBegin;
                } else if (i == 1) {
                    command = Message.Command.GroupEnd;
                }
                when(mockedMessage.getCommand()).thenReturn(command);
                messageConsumer.accept(mockedMessage);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String createTestMessage(String topic) {
        Map<String, Object> data = new HashMap<>();
        data.put("ID", "DE000" + random.nextInt(1010));
        data.put("message", "Hola World " + random.nextInt(10));
        data.put("BID", 10.5 + random.nextInt(10));
        data.put("Topic", topic);
        data.put("OFFER1", 11.5 + random.nextInt(10));
        data.put("OFFER2", 11.5 + random.nextInt(10));
        data.put("OFFER3", 11.5 + random.nextInt(10));
        data.put("OFFER4", 11.5 + random.nextInt(10));
        data.put("OFFER5", 11.5 + random.nextInt(10));
        return jsonConvertor.convertToString(data);
    }
}
