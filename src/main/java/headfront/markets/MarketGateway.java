package headfront.markets;

import headfront.amps.AmpsConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

/**
 * Created by Deepak on 09/07/2016.
 */
public class MarketGateway {
    private static final Logger LOG = LoggerFactory.getLogger(MarketGateway.class);
    private AmpsConnection connection;
    private String TOPIC_ORDERS = "ORDERS";
    private String TOPIC_TRADES = "TRADES";
    private String TOPIC_RFQ = "RFQ";
    private String TOPIC_INSTRUMENT = "INSTRUMENT";
    private String TOPIC_STATUS = "STATUS";
    private String TOPIC_QUOTES = "QUOTES";
    private final Random random = new Random();
    private DataService dataService = null;
    private int noOfRecords;

    public MarketGateway(String marketName, String ratePerMinuteStr, String noOfRecordStr) {
        try {
            noOfRecords = Integer.parseInt(noOfRecordStr) + random.nextInt(555);
            dataService = new DataService(noOfRecords);
            connection = new AmpsConnection(marketName, null, () -> {
            });
            connection.initialize();
            int sleepTime = getSleepTime(ratePerMinuteStr);
            LOG.info("Publishing.... at interval of " + sleepTime + " millis");
            int publishCount = 0;
            boolean logInitialRecordSet = false;
            while (true) {
                publishMessage(marketName);
                if (publishCount < (noOfRecords * 6)) {
                    publishCount++;

                } else {
                    if (!logInitialRecordSet) {
                        logInitialRecordSet = true;
                        LOG.info("Sent Initial Snapshot data now will send updates");
                    }
                    Thread.sleep(sleepTime);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Unbale to start publisher " + e);
        }
    }

    private void publishMessage(String marketName) {
        int topicChoice = random.nextInt(100);
        try {
            if (topicChoice < 10) {
                Map<String, Object> recordDetails = dataService.getRandomOrder(TOPIC_ORDERS);
                connection.publishDelta("/" + marketName + "/" + TOPIC_ORDERS, "ID", recordDetails);
            } else if (topicChoice < 20) {
                Map<String, Object> recordDetails = dataService.getRandomTrade(TOPIC_TRADES);
                connection.publishDelta("/" + marketName + "/" + TOPIC_TRADES, "ID", recordDetails);
            } else if (topicChoice < 30) {
                Map<String, Object> recordDetails = dataService.getRandomRFQ(TOPIC_RFQ);
                connection.publishDelta("/" + marketName + "/" + TOPIC_RFQ, "ID", recordDetails);
            } else if (topicChoice < 40) {
                Map<String, Object> recordDetails = dataService.getRandomInstruments(TOPIC_INSTRUMENT);
                connection.publishDelta("/" + marketName + "/" + TOPIC_INSTRUMENT, "ID", recordDetails);
            } else if (topicChoice < 50) {
                Map<String, Object> recordDetails = dataService.getRandomStatus(TOPIC_STATUS);
                connection.publishDelta("/" + marketName + "/" + TOPIC_STATUS, "ID", recordDetails);
            } else {
                Map<String, Object> recordDetails = dataService.getRandomQuotes(TOPIC_QUOTES);
                connection.publishDelta("/" + marketName + "/" + TOPIC_QUOTES, "ID", recordDetails);
            }
        } catch (Exception e) {
            LOG.error("Unbale to publish message  ", e);
        }
    }

    private int getSleepTime(String ratePerMinStr) {
        int ratePerMin = Integer.parseInt(ratePerMinStr);
        return ((60 * 1000) / ratePerMin);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            String message = "Expected three arguments Marketname , rateOfPublishPerMinute , noOfRecords e.g. BBG 1 1000 got " + Arrays.toString(args);
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        new MarketGateway(args[0], args[1], args[2]);
    }

}
