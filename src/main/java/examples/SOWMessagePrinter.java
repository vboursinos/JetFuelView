package examples;

import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.MessageHandler;
import com.crankuptheamps.client.fields.ReasonField;

/**
 * Class to handle received messages. The class
 * implements an invoke(Message) method. That method
 * is called for each message received by the subscribe
 * command.
 *
 * @author 60East
 */
public class SOWMessagePrinter implements MessageHandler {

    private boolean sowfinished_ = false;


    /**
     * Method for handling AMPS messages. This implementation
     * prints the message data to stdout.
     *
     * @param m the message to handle.
     */


    public void invoke(Message m) {

        if (m.getCommand() == Message.Command.OOF) {
            System.out.println("Message no longer in focus because : "
                    + ReasonField.encodeReason(m.getReason()) +
                    " : " + m.getData());
            return;

        }
        if (m.getCommand() == Message.Command.GroupBegin) {
            System.out.println("Receiving messages from SOW (beginning of group).");
        }
        System.out.println(m.getData());
        if (m.getCommand() == Message.Command.GroupEnd) {
            System.out.println("Finished receiving messages from SOW (end of group).");
            sowfinished_ = true;
        }

    }


    public boolean isSOWFinished() {
        return sowfinished_;
    }


}
