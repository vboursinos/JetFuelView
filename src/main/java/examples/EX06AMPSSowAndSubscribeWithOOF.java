package examples;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.exception.AMPSException;

/**
 * EX06AMPSSOWandSubscribeWithOOF
 * <p>
 * This sample retrieves messages from a state-of-the-world database. The program flow is simple:
 * <p>
 * * Connect to AMPS
 * * Logon
 * * Get the state-of-the-world for the "messages-sow" topic, filtered
 * to messages with a message number that is an even multiple of 10
 * and either no optionalField or an optionalField value other than
 * 'ignore_me'.
 * * Output all messages received to the console
 * <p>
 * This sample doesn't include error handling or connection
 * retry logic.
 * <p>
 * (c) 2013-2015 60East Technologies, Inc.  All rights reserved.
 * This file is a part of the AMPS Evaluation Kit.
 */

public class EX06AMPSSowAndSubscribeWithOOF {

    // The location of the AMPS server.
    private static final String uri_ = "tcp://127.0.0.1:9007/amps/json";

    /**
     * main method.
     *
     * @param args -- No command line options read.
     */

    public static void main(String[] args) {

        Client client = new Client("SOWandSubscribeConsoleSubscribeWithOOF");

        try {
            client.connect(uri_);
            client.logon();

            // create the object to process the messages.

            SOWMessagePrinter smp = new SOWMessagePrinter();

            System.out.println("... entering subscription ...");

            // request messages from the messages-sow topic where
            // the message number is mod 10, and optionalField is
            // either missing or, if present, is not 'ignore_me'. Retrieve in batches
            // of 5 messages at a time.  We also request OOF notifications.

            // For sample purposes, we use the asynchronous form of execute.

            client.executeAsync(new Command(Message.Command.SOWAndSubscribe)
                            .setTopic("messages-sow")
                            .setFilter("/messageNumber % 10 = 0 AND " +
                                    "(/optionalField is NULL OR " +
                                    " /optionalField <> 'ignore_me')")
                            .setOptions("oof")
                            .setBatchSize(5),
                    smp);

            System.out.println("Requested SOW messages.");

            // the results of the sow query arrive asynchronously,
            // so the sample sleeps to let the messages arrive.

            // this program uses this construct for sample purposes.
            // generally speaking, the program would use the results
            // of the query as they arrive.

            while (true) {
                Thread.sleep(100);
            }

        } catch (AMPSException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        } catch (InterruptedException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }


    }

}
