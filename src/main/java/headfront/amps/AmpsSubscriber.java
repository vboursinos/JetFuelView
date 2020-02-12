package headfront.amps;

import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.exception.AMPSException;

/**
 * Created by Deepak on 28/03/2016.
 */
public class AmpsSubscriber {


    public AmpsSubscriber() {
        AmpsConnection connection = new AmpsConnection("AmpsSubaMultil", null, () -> {
        });
        connection.initialize();
//        connection.subscribeTopic(sowstatus, "", message -> {
        try {
            connection.subscribeTopic("/BBG/INSTRUMENT", "/Active='false'", "", message -> {
                System.out.println(message.getTopic() + " -> " + message.getData());
            }, false, Message.Command.SOWAndDeltaSubscribe, "");
        } catch (AMPSException e) {
            e.printStackTrace();
        }
        try {
            connection.subscribeTopic("/BBG/STATUS", "/Active='false'", "", message -> {
                System.out.println(message.getTopic() + " -> " + message.getData());
            }, false, Message.Command.SOWAndDeltaSubscribe, "");
        } catch (AMPSException e) {
            e.printStackTrace();
        }
        try {
            connection.subscribeTopic("/TRADEWEB/INSTRUMENT", "/Active='false'", "", message -> {
                System.out.println(message.getTopic() + " -> " + message.getData());
            }, false, Message.Command.SOWAndDeltaSubscribe, "");
        } catch (AMPSException e) {
            e.printStackTrace();
        }
        System.out.println("Subscribed....");
    }

    public static void main(String[] args) {
        new AmpsSubscriber();
    }
}
