package headfront.amps.services;

import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.MessageHandler;
import headfront.convertor.MessageConvertor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Deepak on 19/07/2016.
 */
public class SowStatusService implements MessageHandler {

    private Map<String, Map<String, Object>> currentSowStatus = new HashMap<>();
    private Map<String, String> topicToMessageType = new HashMap<>();
    private MessageConvertor messageConvertor;

    public SowStatusService(MessageConvertor messageConvertor) {
        this.messageConvertor = messageConvertor;
    }

    @Override
    public void invoke(Message message) {
        // store matches and last id
        String data = message.getData().trim();
        if (data.length() > 0) {
            Map<String, Object> dataMap = messageConvertor.convertToMap(data);
            Map<String, Object> sowStats = (Map<String, Object>) dataMap.get("SOWStats");
            if (sowStats != null) {
                String topic = (String) sowStats.get("topic");
                String messageType = (String) sowStats.get("message_type");
                if (topic != null) {
                    currentSowStatus.put(topic, sowStats);
                    topicToMessageType.put(topic, messageType);
                }
            }
        }
    }

    public Set<String> getAllTopic() {
        return currentSowStatus.keySet();
    }

    public String getTopicMessageType(String topic) {
        return topicToMessageType.get(topic);
    }

    public Map<String, Object> getTopicDetails(String topic) {
        return currentSowStatus.get(topic);
    }
}
