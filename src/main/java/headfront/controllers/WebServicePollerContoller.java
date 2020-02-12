package headfront.controllers;

import headfront.convertor.JacksonJsonConvertor;
import headfront.messages.OutputMessage;
import headfront.services.WebstatServicePoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebServicePollerContoller {


    @Autowired
    private WebstatServicePoller webServicePoller;
    private JacksonJsonConvertor jsonConvertor = new JacksonJsonConvertor();

    private static final Logger LOG = LoggerFactory.getLogger(WebServicePollerContoller.class);


    @MessageMapping("/webPollerCmd")
    @SendTo("/topic/webPollerFeed")
    public OutputMessage subscribe(String message) throws Exception {
        LOG.info("Got webServicePoller Subscribe Message " + message);
        Map<String, Object> data = jsonConvertor.convertToMap(message);
        String status = webServicePoller.registerUrl(data.get("name").toString(), data.get("url").toString(), "/topic/webPollerFeed");
        return new OutputMessage("<h3>" + status + "</h3>");
    }

}