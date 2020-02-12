package examples;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.exception.AMPSException;

//EX02AMPSConsolePublisher
//
//This is a minimalist way of publishing messages to a topic in AMPS.
//The program flow is simple:
//
//* Connect to AMPS
//* Logon
//* Publish a message the "messages" topic 
//
//This sample doesn't include error handling or connection
//retry logic.
//
//(c) 2014-2016 60East Technologies, Inc.  All rights reserved.
//This file is a part of the AMPS Evaluation Kit.


public class EX02AMPSConsolePublisher {

    // The location of the AMPS server.
    private static final String uri_ = "tcp://127.0.0.1:9007/amps/json";

    /**
     * main method.
     *
     * @param args -- No command line options read.
     */
    public static void main(String[] args) {
        Client client = new Client("ConsolePublisher");

        try {
            client.connect(uri_);
            System.out.println("connected..");
            client.logon();

            client.publish("messages", "{\"message\" : \"Hello, World!\"}");

            System.exit(0);
        } catch (AMPSException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }

    }

}

