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
public class DeleteMessageHandler implements MessageHandler {


    /**
     * Method for handling AMPS messages.
     * This implementation simply ignores messages.
     * Having a delete message handler allows you to do any
     * necessary cleanup on the messages when they're deleted.
     *
     * @param m the message to handle.
     */


    public void invoke(Message m) {

    }


}
