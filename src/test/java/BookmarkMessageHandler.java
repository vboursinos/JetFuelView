import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.MessageHandler;
import com.crankuptheamps.client.exception.AMPSException;
import com.crankuptheamps.client.fields.ReasonField;

/**
 * Class to handle received messages. The class
 * implements an invoke(Message) method. That method
 * is called for each message received by the subscribe
 * command.
 *
 * @author 60East
 */
public class BookmarkMessageHandler implements MessageHandler {

    private boolean sowfinished_ = false;
    private Client client_ = null;


    /**
     * @param c The client to use to discard processed messages.
     */
    BookmarkMessageHandler(Client c) {
        client_ = c;
    }

    /**
     * Method for handling AMPS messages. This implementation
     * prints the message data to stdout.
     *
     * @param m the message to handle.
     */


    public void invoke(Message m) {

        try {

            if (m.getCommand() == Message.Command.OOF) {
                System.out.println("Message no longer in focus because : "
                        + ReasonField.encodeReason(m.getReason()) +
                        " : " + m.getData());
                client_.getBookmarkStore().discard(m);
                return;

            }
            if (m.getCommand() == Message.Command.GroupBegin) {
                System.out.println("Receiving messages from SOW (beginning of group).");
                sowfinished_ = false;
            }
            System.out.println(m.getData());
            if (m.getCommand() == Message.Command.GroupEnd) {
                System.out.println("Finished receiving messages from SOW (end of group).");
                sowfinished_ = true;
            }

            client_.getBookmarkStore().discard(m);

        } catch (AMPSException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }


    }


    public boolean isSOWFinished() {
        return sowfinished_;
    }


}
