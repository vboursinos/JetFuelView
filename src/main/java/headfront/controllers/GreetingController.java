package headfront.controllers;

import headfront.messages.InputMessage;
import headfront.messages.OutputMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

//todo Sort out
@Controller
public class GreetingController {


    private SimpMessagingTemplate template;

    @Autowired
    public GreetingController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("/start")
    @SendTo("/topic/greetings")
    public OutputMessage greeting(InputMessage message) throws Exception {
        Thread.sleep(300); // simulated delay
        sendStream();
        return new OutputMessage("Hola, " + message.getName() + "!");
    }

    private void sendStream() {
        new Thread(() -> {
            int count = 0;
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Stream " + count++);

                template.convertAndSend("/topic/greetings", new OutputMessage("Hola Streat " + count++));
            }
        }).start();
    }

}