package examples;

import com.crankuptheamps.client.Client;
import com.crankuptheamps.client.exception.AMPSException;


/**
 * EX09AMPSPublishForReplay
 * <p>
 * This sample publishes messages to a topic in AMPS that
 * maintains a transaction log.
 * <p>
 * The program flow is simple:
 * <p>
 * * Connect to AMPS
 * * Logon
 * * Publish 100 messages at a time to the "messages-history" topic. Each
 * message published has a unique orderId. The program waits one second
 * between sets of 100 messages.
 * <p>
 * The "messages-history" topic is configured in config/sample.xml to
 * maintain a transaction log.
 * <p>
 * This sample doesn't include error handling or connection
 * retry logic.
 * <p>
 * (c) 2014-2015 60East Technologies, Inc.  All rights reserved.
 * This file is a part of the AMPS Evaluation Kit.
 */

public class EX09AMPSPublishForReplay {

    // The location of the AMPS server.
    private static final String uri_ = "tcp://127.0.0.1:9007/amps/json";

    /**
     * main method.
     *
     * @param args -- No command line options read.
     */
    public static void main(String[] args) {

        Client client = new Client("ReplayPublisher");

        try {
            // connect and logon
            client.connect(uri_);
            client.logon();

            int orderId = 1;

            String ordertemplate =
                    "{ \"orderId\" : %d, \"symbol\" : \"IBM\", \"size\" : 1000"
                            + ", \"price\" = 190.01}";


            while (true) {
                for (int number = 1; number < 100; number++) {
                    client.publish("messages-history",
                            String.format(ordertemplate, orderId));
                    orderId += 1;
                }
                System.out.println(".");

                Thread.sleep(1);
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
