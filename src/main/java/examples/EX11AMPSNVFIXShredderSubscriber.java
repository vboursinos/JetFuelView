package examples;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.MessageStream;
import com.crankuptheamps.client.NVFIXShredder;
import com.crankuptheamps.client.exception.AMPSException;

import java.util.Map;

/**
 * EX11AMPNVFIXShredderSubscriber
 * <p>
 * This sample subscribes to NVFIX messages. The program flow is simple:
 * <p>
 * * Connect to AMPS
 * * Logon
 * * Connect to the "messages" topic and shred
 * * messages to a key value data structure.
 * * Output all messages received to the console
 * <p>
 * This sample doesn't include error handling or connection
 * retry logic.
 * <p>
 * (c) 2013-2015 60East Technologies, Inc.  All rights reserved.
 * This file is a part of the AMPS Evaluation Kit.
 */
public class EX11AMPSNVFIXShredderSubscriber {

    // The location of the AMPS server.
    private static final String uri_ = "tcp://127.0.0.1:9007/amps/nvfix";

    /**
     * main method.
     *
     * @param args -- No command line options read.
     */
    public static void main(String[] args) {
        System.out.println("Starting the console subscriber.");

        Client client = new Client("ConsoleSubscriber");

        try {
            // connect to the AMPS server and logon
            client.connect(uri_);


            // subscribe to the test-topic topic.
            // when a message arrives, print the message.

            MessageStream ms = client.subscribe("messages");
            try {
                // Create a shredder -- since this just returns
                // the Map, we can reuse the same shredder.
                NVFIXShredder shredder = new NVFIXShredder((byte) 1);

                for (Message m : ms) {
                    // Skip messages with no data.
                    if (m.getCommand() != Message.Command.SOW &&
                            m.getCommand() != Message.Command.Publish) continue;

                    System.out.println("Got a message");

                    // Shred the message into a Map

                    Map<CharSequence, CharSequence> fields = shredder.toNVMap(m.getData());
                    // Iterate over the keys in the map and print the key and data
                    for (CharSequence key : fields.keySet()) {
                        System.out.println("  " + key + "=" + fields.get(key));
                    }
                }
            } finally  // close the message stream to release the subscription
            {
                ms.close();
            }

        } catch (AMPSException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);

        }
    }
}
