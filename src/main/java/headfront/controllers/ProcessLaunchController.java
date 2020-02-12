package headfront.controllers;

import headfront.messages.InputMessage;
import headfront.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ProcessLaunchController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessLaunchController.class);
    @Autowired
    private ProcessService processService;

    @MessageMapping("/launch")
    public void launchProcess(InputMessage message) throws Exception {
        processService.launchJavaProcess(message.getName());
    }

}