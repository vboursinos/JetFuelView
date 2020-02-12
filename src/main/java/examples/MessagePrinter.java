package examples;

import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.MessageHandler;

/**
 * Class to handle received messages. The class
 * implements an invoke(Message) method. That method
 * is called for each message received by the subscribe
 * command.
 *
 * @author 60East
 */
public class MessagePrinter implements MessageHandler {

    /**
     * Method for handling AMPS messages. This implementation
     * prints the message data to stdout.
     *
     * @param m the message to handle.
     */

    public void invoke(Message m) {
        System.out.println(m.getData());
    }
}
