package examples;

import com.crankuptheamps.client.*;
import com.crankuptheamps.client.exception.AMPSException;

/**
 * EX08AMPSSubscribeForReplay
 * <p>
 * This sample retrieves messages from a state-of-the-world database. The program flow is simple:
 * <p>
 * * Connect to AMPS
 * * Logon
 * * Request replay from "messages-history" topic from the last message
 * received.
 * <p>
 * This sample doesn't include error handling or connection
 * retry logic.
 * <p>
 * (c) 2013-2016 60East Technologies, Inc.  All rights reserved.
 * This file is a part of the AMPS Evaluation Kit.
 */

public class AggregationExample {

    // The location of the AMPS server.
    private static final String uri_ = "tcp://192.168.56.101:8001/amps/json";

    /**
     * main method.
     *
     * @param args -- No command line options read.
     */

    public static void main(String[] args) {

        HAClient client = new HAClient("exampleSubscriberWithReplay1");

        try {

            DefaultServerChooser sc = new DefaultServerChooser();
            sc.add(uri_);
            client.setServerChooser(sc);

            client.connectAndLogon();

            // create the object to process the messages.

            MessagePrinter bmh = new MessagePrinter();

            System.out.println("... entering subscription ...");

            // Enter the subscription. This statement requests all
            // messages that have not been previously processed on the
            // "messages-history" topic.

            client.executeAsync(new Command(Message.Command.SOWAndSubscribe)
                            .setTopic("TEST_INSTRUMENTS")
                            .setSubId(new CommandId("sample-replay-id")).setOptions(Message.Options.OOF + Message.Options.Timestamp + "projection=[/symbol,avg(/price) as /avg_price],grouping=[/symbol]")
//              .setFilteer(("/ID='JetFuel_Deepak_2017-07-02T09:35:17.615_1'"))
                    ,
                    bmh);

//      $ ~/spark sow -server localhost:9007 -topic Orders -opts
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
