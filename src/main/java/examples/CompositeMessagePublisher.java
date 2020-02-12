package examples;


import com.crankuptheamps.client.CompositeMessageBuilder;
import com.crankuptheamps.client.DefaultServerChooser;
import com.crankuptheamps.client.HAClient;
import com.crankuptheamps.client.exception.AMPSException;
import com.crankuptheamps.client.fields.Field;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * CompositeMessagePublisher
 * <p>
 * Simple example demonstrating publishing a composite message to a topic
 * in AMPS.
 * <p>
 * The program flow is simple:
 * <p>
 * * Connect to AMPS, using the transport configured for composite json-binary
 * messages
 * * Logon
 * * Construct binary data for the message. For demonstration purposes,
 * the sample uses the same binary data for each message.
 * * Publish a set of messages to AMPS. For each message:
 * - Construct a json part that the subscriber can filter on.
 * - Construct a composite message payload.
 * - Publish the message.
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


public class CompositeMessagePublisher {

    public static void main(String[] args) throws AMPSException, IOException, ClassNotFoundException {
        HAClient client = new HAClient("CompositePubber");
        try {
            DefaultServerChooser sc = new DefaultServerChooser();
            sc.add("tcp://127.0.0.1:9017/amps");
            client.setServerChooser(sc);

            // Construct binary data

            List<Double> theData = new ArrayList<Double>();
            theData.add(1.0);

            for (double d = 1.0; d < 50.0; ++d) {
                if (d <= 1.0) {
                    theData.add(1.0);
                    continue;
                }
                theData.add(d + theData.get((int) d - 2));
            }

            // Make a byte array from the list
            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            ObjectOutputStream listWriter = new ObjectOutputStream(outBytes);
            listWriter.writeObject(theData);  // writes to underlying outBytes

            client.connectAndLogon();

            // Publish the messages
            String topic = "messages";

            for (int count = 1; count < 10; ++count) {
                // Construct a JSON part
                StringBuilder sb = new StringBuilder();
                sb.append("{\"binary_type\": \"double\"")
                        .append(", \"size\" : ").append(theData.size())
                        .append(", \"number\" : ").append(count)
                        .append(", \"message\" : \"Hi, world!\"")
                        .append("}");

                // Construct the composite
                CompositeMessageBuilder builder = new CompositeMessageBuilder();
                builder.append(sb.toString());
                builder.append(outBytes.toByteArray(), 0, outBytes.size());

                // Get a reference to the bytes in the builder
                Field outMessage = new Field();
                builder.setField(outMessage);

                client.publish(topic.getBytes(), 0, topic.getBytes().length,
                        outMessage.buffer, 0, outMessage.length);
            }
        } finally {

            client.close();
        }

    }
}
