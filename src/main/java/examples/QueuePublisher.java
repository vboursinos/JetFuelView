package examples;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.exception.AMPSException;

// QueuePublisher
//
//This is a minimalist way of publishing messages to a queue in AMPS.
//The program flow is simple:
//
//* Connect to AMPS
//* Logon
//* Publish messages the "sample-queue" topic 
//
//This sample doesn't include error handling or connection
//retry logic.
//
//(c) 2016 60East Technologies, Inc.  All rights reserved.
//This file is a part of the AMPS Evaluation Kit.


public class QueuePublisher {

    // The location of the AMPS server.
    private static final String uri_ = "tcp://127.0.0.1:9007/amps/json";

    /**
     * main method.
     *
     * @param args -- No command line options read.
     */
    public static void main(String[] args) {
        Client client = new Client("QueuePublisher");

        try {
            client.connect(uri_);
            System.out.println("Publisher connected..");
            client.logon();
            for (int i = 0; i < 1000; ++i) {
                client.publish("sample-queue",
                        "{\"message\" : \"Hello, World! This is message " + i + " \"}");
                Thread.sleep(250);
            }

            System.exit(0);
        } catch (InterruptedException e) {
            // For a command line sample, simply exit if sleep is interrupted.
        } catch (AMPSException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }

    }

}

