package examples;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.MessageStream;
import com.crankuptheamps.client.exception.AMPSException;

import java.util.Random;

/**
 * QueueSubscriber
 * <p>
 * This is a minimalist way of subscribing to an at-least-once queue
 * in AMPS. The program flow is simple:
 * <p>
 * * Connect to AMPS
 * * Logon
 * * Subscribe to all messages published on the "sample-queue" topic
 * * Output the messages to the console
 * <p>
 * This sample uses automatic acknowledgement of the messages in the
 * queue. With this setting, the client will handle acknowledgement
 * for the previous message each time the MessageStream returns
 * a new message. (See the Developer Guide for full details.)
 * <p>
 * (c) 2014-2016 60East Technologies, Inc.  All rights reserved.
 * This file is a part of the AMPS Evaluation Kit.
 */

public class QueueSubscriber {

    // The location of the AMPS server.
    private static final String uri_ = "tcp://127.0.0.1:9007/amps/json";

    /**
     * main method.
     * <p>
     * first argument is the name of the subscriber
     */
    public static void main(String[] args) {
        String id = "";

        if (args.length > 0) {
            id = args[0];
        } else {
            id = Integer.toString(new Random().nextInt(100));
        }

        System.out.println("Starting the console subscriber: " + id);
        Client client = new Client("QueueSubscriber-" + id);

        try {
            // enable automatic acknowledgment on queue subscriptions
            client.setAutoAck(true);

            // connect to the AMPS server and logon
            client.connect(uri_);
            client.logon();

            System.out.println(id + " connected.");

            // subscribe to the sample-queue topic.
            // when a message arrives, print the message.

            MessageStream ms = client.subscribe("sample-queue");
            try {
                System.out.println(id + " subscribed.");
                for (Message m : ms) {
                    System.out.println("[" + id + "]: " + m.getData());
                } // when the loop retrieves the next message, the previous message is marked for
                // acknowledgement.
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
