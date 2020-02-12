package examples;

import com.crankuptheamps.client.*;
import com.crankuptheamps.client.exception.AMPSException;
import com.crankuptheamps.client.fields.Field;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * CompositeMessageSubscriber
 * <p>
 * Simple example demonstrating receiving and parsing a composite message
 * from a topic in AMPS.
 * <p>
 * The program flow is simple:
 * <p>
 * * Connect to AMPS, using the transport configured for composite json-binary
 * messages
 * * Logon
 * * Subscribe to the topic, using a filter that refers to the json part of the
 * message
 * * For each message received:
 * - Parse the message
 * - Extract the json part of the message
 * - Re-create the binary part of the message as a Java object
 * - Print the contents of the message
 * <p>
 * This sample doesn't include error handling or connection
 * retry logic.
 * <p>
 * This file is a part of the AMPS Evaluation Kit.
 */

////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2010-2015 60East Technologies Inc., All Rights Reserved.
//
// This computer software is owned by 60East Technologies Inc. and is
// protected by U.S. copyright laws and other laws and by international
// treaties.  This computer software is furnished by 60East Technologies
// Inc. pursuant to a written license agreement and may be used, copied,
// transmitted, and stored only in accordance with the terms of such
// license agreement and with the inclusion of the above copyright notice.
// This computer software or any other copies thereof may not be provided
// or otherwise made available to any other person.
//
// U.S. Government Restricted Rights.  This computer software: (a) was
// developed at private expense and is in all respects the proprietary
// information of 60East Technologies Inc.; (b) was not developed with
// government funds; (c) is a trade secret of 60East Technologies Inc.
// for all purposes of the Freedom of Information Act; and (d) is a
// commercial item and thus, pursuant to Section 12.212 of the Federal
// Acquisition Regulations (FAR) and DFAR Supplement Section 227.7202,
// Government’s use, duplication or disclosure of the computer software
// is subject to the restrictions set forth by 60East Technologies Inc..
//
////////////////////////////////////////////////////////////////////////////


public class CompositeMessageSubscriber {

    public static void main(String[] args)
            throws AMPSException, IOException, ClassNotFoundException {
        // Create the client
        HAClient client = new HAClient("CompositeSubscriber");

        try {
            // Add URIs and connect the client.
            DefaultServerChooser sc = new DefaultServerChooser();
            sc.add("tcp://127.0.0.1:9017/amps");
            client.setServerChooser(sc);
            client.connectAndLogon();

            // Construct the parser to use
            CompositeMessageParser parser = new CompositeMessageParser();

            // Create the MessageStream. This uses try-with resources, and
            // may need to be modified for older versions of Java.
            try (MessageStream stream = client.subscribe("messages", "/0/number % 3 == 0")) {
                for (Message message : stream) {
                    // Parse the message and get the number of parts.
                    int parts = parser.parse(message);

                    // Get the contents of the message
                    String json_part = parser.getString(0);
                    Field binary = new Field();
                    parser.getPart(1, binary);

                    // Recreate the List<Double> from the binary
                    // part of the message.
                    ByteArrayInputStream inBytes = new ByteArrayInputStream(binary.buffer, binary.position, binary.length);
                    ObjectInputStream listReader = new ObjectInputStream(inBytes);
                    List<Double> theData = (List<Double>) listReader.readObject();

                    // Print the message
                    System.out.println("Received message with " + parts + " parts.");
                    System.out.println(json_part);
                    for (Double d : theData) {
                        System.out.print(d + " ");
                    }
                    System.out.print("\n");

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }

    }

}
