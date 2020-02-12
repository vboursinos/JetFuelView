package examples;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.FIXBuilder;
import com.crankuptheamps.client.exception.AMPSException;

public class EX12AMPSFIXBuilderPublisher {

    // The location of the AMPS server.
    private static final String uri_ = "tcp://127.0.0.1:9007/amps/fix";

    /**
     * main method.
     *
     * @param args -- No command line options read.
     */
    public static void main(String[] args) {
        Client client = new Client("ConsolePublisher");
        try {

            // Build the message payload

            // Create a builder with 1024 bytes of initial capacity,
            // using the default 0x01 delimiter.
            FIXBuilder builder = new FIXBuilder(1024, (byte) 1);

            // Add fields
            builder.append(0, "24");
            builder.append(1, "Here's another field");
            builder.append(2, "1234567890");

            // Create a string for the topic
            String topic = "messages";

            client.connect(uri_);
            System.out.println("connected..");
            client.logon();

            // publish the message, using the overload that takes a byte array and
            // length for the topic and payload.

            client.publish(topic.getBytes(), 0, topic.length(), builder.getBytes(), 0, builder.getSize());

            System.exit(0);
        } catch (AMPSException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }

    }

}

