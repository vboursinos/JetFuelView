package examples;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.MessageStream;
import com.crankuptheamps.client.exception.AMPSException;

/**
 * EX04AMPSSOWConsoleSubscriber
 * <p>
 * This sample retrieves messages from a state-of-the-world database. The program flow is simple:
 * <p>
 * * Connect to AMPS
 * * Logon
 * * Get the state-of-the-world for the "messages-sow" topic, filtered
 * messages with a message number less than 10.
 * * Output all messages received to the console
 * <p>
 * This sample doesn't include error handling or connection
 * retry logic.
 * <p>
 * (c) 2013-2015 60East Technologies, Inc.  All rights reserved.
 * This file is a part of the AMPS Evaluation Kit.
 */

public class EX04AMPSSOWConsoleSubscriber {

    // The location of the AMPS server.
    private static final String uri_ = "tcp://127.0.0.1:9007/amps/json";

    /**
     * main method.
     *
     * @param args -- No command line options read.
     */

    public static void main(String[] args) {

        Client client = new Client("SOWConsoleSubscriber");

        try {
            client.connect(uri_);
            client.logon();


            // request messages from the messages-sow topic where
            // the messageNumber field is less than 10.

            MessageStream ms = client.execute(new Command(Message.Command.SOW)
                    .setTopic("messages-sow")
                    .setFilter("/messageNumber < 10"));
            try {
                for (Message m : ms) {
                    if (m.getCommand() == Message.Command.GroupBegin) {
                        System.out.println("Receiving messages from SOW " +
                                "(beginning of group).");
                        continue;
                    }
                    if (m.getCommand() == Message.Command.GroupEnd) {
                        System.out.println("Finished receiving messages from"
                                + " SOW (end of group).");
                        continue;
                    }
                    System.out.println(m.getData());
                }
            } finally // release the SOW query by closing the message stream
            {
                ms.close();
            }


            System.exit(0);

        } catch (AMPSException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }

    }

}
