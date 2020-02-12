package headfront.amps.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 29/06/2016.
 */
public class TopicMetaData {

    private Map<String, Object> topicDetails;
    private List<String> records;
    private List<String> fields;

    public TopicMetaData(Map<String, Object> topicDetails, List<String> records,
                         List<String> fields) {
        this.topicDetails = topicDetails;
        this.records = records;
        this.fields = fields;
    }

    public Map<String, Object> getTopicDetails() {
        return topicDetails;
    }

    public List<String> getRecords() {
        return records;
    }

    public List<String> getFields() {
        return fields;
    }

    public int getRecordCount() {
        Integer recordCount = (Integer) topicDetails.get(TopicService.TOPIC_NO_OF_RECORDS);
        if (recordCount == null) {
            return 0;
        } else {
            return recordCount;
        }
    }

    public TopicMetaData copy() {
        return new TopicMetaData(new HashMap<>(topicDetails), new ArrayList<>(records), new ArrayList<>(fields));
    }

}
