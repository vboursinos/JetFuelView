package examples;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.exception.AMPSException;


/**
 * EX03AMPSSOWConsolePublisher
 * <p>
 * This sample publishes messages to a topic in AMPS that
 * maintains a state-of-the-world (SOW) database.
 * The program flow is simple:
 * <p>
 * * Connect to AMPS
 * * Logon
 * * Publish 100 messages to the "messages-sow" topic
 * * Publish another message with a duplicate messageNumber to the topic,
 * effectively updating that message.
 * <p>
 * The "messages-sow" topic is configured in config/sample.xml to
 * maintain a SOW database, where each messageNumber is a unique
 * message.
 * <p>
 * This sample doesn't include error handling or connection
 * retry logic.
 * <p>
 * (c) 2013 60East Technologies, Inc.  All rights reserved.
 * This file is a part of the AMPS Evaluation Kit.
 */

public class EX03AMPSSOWConsolePublisher {

    // The location of the AMPS server.
    private static final String uri_ = "tcp://127.0.0.1:9007/amps/json";

    /**
     * main method.
     *
     * @param args -- No command line options read.
     */
    public static void main(String[] args) {

        Client client = new Client("SOWConsolePublisher");

        String dataFormat = "{\"messageNumber\" : %d" +
                ", \"message\" : \"%s\"}";

        try {
            // connect and logon
            client.connect(uri_);
            client.logon();


            // send 100 messages with unique message numbers
            // to fill the SOW database

            for (int number = 0; number < 100; ++number) {
                client.publish("messages-sow",
                        String.format(dataFormat, number, "Hello, World!"));
            }
            // Now make a change to message 5

            client.publish("messages-sow",
                    String.format(dataFormat, 5, "This is new information."));

            System.exit(0);
        } catch (AMPSException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }

    }

}
