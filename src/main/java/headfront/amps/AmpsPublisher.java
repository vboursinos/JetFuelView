package headfront.amps;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Deepak on 28/03/2016.
 */
public class AmpsPublisher {
    long count = 1; // Change this to publish records from this id
    Random random = new Random();
    AmpsConnection connection = new AmpsConnection("AmpsPublisher", null, () -> {
    });
//    AmpsConnection connection = new AmpsConnection("AmpsPublisher", null, new NvFixConvertor());

    public AmpsPublisher() {
        connection.initialize();
        System.out.println("Publishing....");
        while (count < 10000000) {
            publishHistoricTrade("FAST_PRICE");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        while (true) {
//            int topicChoice = (int) (Math.random() * 100);
//            if (topicChoice < 33) {
//                createMessage("ORDERS");
//            } else if (topicChoice > 66) {
//                createMessage("TRADES");
//            } else {
//                createMessage("RFQ");
//            }
//            try {
//                Thread.sleep(7);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }


    private void publishHistoricTrade(String topic) {
        Map<String, Object> data = new HashMap<>();
        final int i = (int) (Math.random() * 10);
        data.put("ID", "Trade_" + i);
        data.put("EXN_ID", "TRADER1_" + i);
        data.put("EXN_NAME", "TRADER1_" + i);
        data.put("topic", topic);
        data.put("Bid", 100 + random.nextInt(100));
        data.put("Offer", 10.2 - random.nextInt(100));
        data.put("client", "Who knows");
        data.put("Date", new Date().toString());
        data.put("State", "Done");
        try {
            connection.publishDelta(topic, "ID", data);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createMessage(String topic) {
        int create = random.nextInt(100);
        String id = "" + random.nextInt(1000);
        if (create > 1) {
            Map<String, Object> data = new HashMap<>();
            data.put("ID", id);
            data.put("message", "Hola World " + count++);
            data.put("topic", topic);
            try {
                connection.publishDelta(topic, id, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                connection.deleteRecord(topic, "ID", id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new AmpsPublisher();
    }
}
