package headfront.amps.services;

import com.crankuptheamps.client.CommandId;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.exception.AMPSException;
import javafx.scene.control.CheckBoxTreeItem;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Deepak on 28/06/2016.
 */
public interface TopicService {

    String TOPIC_NAME = "Name";
    String TOPIC_TYPE = "Type";
    String TOPIC_KEY = "Key";
    String TOPIC_MESSAGE_TYPE = "Msg Type";
    String TOPIC_SOW_ENABLED = "Sow Enabled";
    String TOPIC_TXN_ENABLED = "Txn Enabled";
    String TOPIC_NO_OF_RECORDS = "No of Records";
    String TOPIC_EXPIRY = "Expiry";

    int NO_OF_RECORDS_TO_QUERY = 2000;

    List<String> getAllTopics();

    List<String> getAllTopicsNamesOnly();

    boolean doesTopicExists(String topic);

    void setTopicDataFromConfig(String config);

    void setFullAmpsJsonFile(Map<String, Object> allAmpsJson);

    void setAmpsStatsMetaData(Map<String, Object> metaData);

    CheckBoxTreeItem<String> getAmpsStatsMetaData();

    TopicMetaData getTopicMetaData(String topic, String recordToSearch);

    void clearTopicMetaData();

    List<String> getSowKey(String topic);

    boolean isLargeSow(String topic);

    CommandId subscribeToRecord(String topic,
                                List<String> records,
                                Consumer<Message> messageConsumer,
                                boolean history, boolean subscribeOnly, boolean deltaSubscribe, String bookmark, String options) throws AMPSException;

    CommandId subscribeToFilter(String topic,
                                String filter, String OrderBy,
                                Consumer<Message> messageConsumer,
                                boolean history, boolean subscribeOnly, boolean deltaSubscribe, String bookmark, String options) throws AMPSException;

    void addMetaDataListener(Runnable listener);

    List<String> getActiveUsers();

    List<Map<String, Object>> getActiveSubscriptions(String user);

     String getJetFuelExecuteFunctionBus() ;

     String getJetFuelExecuteFunction() ;

     default String getAmpsConnectionPort(String connType, String messageType){
         return "Coming Soon";
     }
}
