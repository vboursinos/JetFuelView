package examples;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.exception.AMPSException;

/**
 * EX07AMPSSOWUpdater
 * <p>
 * This sample publishes messages to a topic in AMPS that
 * maintains a state-of-the-world (SOW) database.
 * The program flow is simple:
 * <p>
 * * Connect to AMPS
 * * Logon
 * * Publish a message with the expiration set.
 * * Publish a message to later be deleted.
 * * Publish a set of updates.
 * * Publish another set of updates. This set of updates will cause
 * messages to no longer match the sample filter.
 * * Delete a message.
 * <p>
 * The "messages-sow" topic is configured in config/sample.xml to
 * maintain a SOW database, where each messageNumber is a unique
 * message.
 * <p>
 * This sample doesn't include error handling or connection
 * retry logic.
 * <p>
 * (c) 2013-2015 60East Technologies, Inc.  All rights reserved.
 * This file is a part of the AMPS Evaluation Kit.
 */

public class EX07AMPSSOWUpdater {

    // The location of the AMPS server.
    private static final String uri_ = "tcp://127.0.0.1:9007/amps/json";

    /**
     * main method.
     *
     * @param args -- No command line options read.
     */
    public static void main(String[] args) {

        Client client = new Client("SOWConsolePublisher");

        // format string for a message with the two required fields.
        String dataFormat = "{\"messageNumber\" : %d" +
                ", \"message\" : \"%s\" }";

        // format string for a message with an optional field.
        String dataOptionFormat = "{\"messageNumber\" : %d " +
                ", \"message\" : \"%s\" " +
                ", \"optionalField\" : \"%s\" }";
        try {
            // connect and logon
            client.connect(uri_);
            client.logon();

            // publish a message with expiration set

            client.execute(new Command(Message.Command.Publish)
                    .setTopic("messages-sow")
                    .setData(String.format(dataFormat, 50000,
                            "Here and then gone..."))
                    .setExpiration(15));

            // publish a message to be deleted later on -- notice
            // that the publish method is a convenience method for execute
            // with a command object

            client.publish("messages-sow",
                    String.format(dataFormat, 500,
                            "I've got a bad feeling about this..."));

            // publish two sets of messages, the first one to match the
            // subscriber filter, the next one to make messages no longer
            // match the subscriber filter.

            // the first set of messages is designed so that the
            // sample that uses OOF tracking receives an updated message.

            for (int number = 0; number < 10000; number += 1250) {
                client.publish("messages-sow",
                        String.format(dataFormat, number, "Hello, World!"));
            }

            // the second set of messages is designed so that the
            // sample that uses OOF tracking receives an OOF message.

            for (int number = 0; number < 10000; number += 1250) {
                client.publish("messages-sow",
                        String.format(dataOptionFormat,
                                number, "Updated, World!", "ignore_me"));

            }

            // Delete the message to be deleted.

            client.execute(new Command(Message.Command.SOWDelete)
                    .setTopic("messages-sow")
                    .setFilter("/messageNumber = 500"));

            // wait up to 2 seconds for all messages to be published

            client.flush(2000);
            client.disconnect();

        } catch (AMPSException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }

    }

}
