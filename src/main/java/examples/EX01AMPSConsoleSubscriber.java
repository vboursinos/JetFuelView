package examples;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.exception.AMPSException;

/**
 * EX01AMPSConsoleSubscriber
 * <p>
 * This is a minimalist way of subscribing to a topic in AMPS. The
 * program flow is simple:
 * <p>
 * * Connect to AMPS
 * * Logon
 * * Subscribe to all messages published on the "messages" topic
 * * Output the messages to the console
 * <p>
 * This sample doesn't include error handling or connection
 * retry logic.
 * <p>
 * (c) 2014-2016 60East Technologies, Inc.  All rights reserved.
 * This file is a part of the AMPS Evaluation Kit.
 */

public class EX01AMPSConsoleSubscriber {

    // The location of the AMPS server.
    private static final String uri_ = "tcp://192.168.56.101:8001/amps/json";

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
            client.logon();


            // subscribe to the messages topic with a timeout of 5000.
            // when a message arrives, print the message.

            client.subscribe(m -> {
                        System.out.println(new String(m.getBuffer()));
                    },
                    "/BBG/QUOTES", "", Message.Options.OOF, 3303);


        } catch (AMPSException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);

        }
    }
}
