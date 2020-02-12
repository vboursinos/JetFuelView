package headfront.controllers;

import headfront.convertor.JacksonJsonConvertor;
import headfront.messages.OutputMessage;
import headfront.services.SystemViewServicePoller;
import headfront.services.UserPreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class SystemViewContoller {

    private JacksonJsonConvertor jsonConvertor = new JacksonJsonConvertor();

    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
    private SimpMessagingTemplate template;
    private Random random = new Random();

    @Autowired
    public SystemViewContoller(SimpMessagingTemplate template) {
        this.template = template;
    }

    private static final Logger LOG = LoggerFactory.getLogger(SystemViewContoller.class);
    @Autowired
    SystemViewServicePoller systemViewServicePoller;
    @Autowired
    UserPreferencesService userPreferencesService;

    @MessageMapping("/registerServerCmd")
    @SendTo("/topic/registerServerReply")
    public OutputMessage registerSystem(String message) throws Exception {
        LOG.info("Got registerSystem message " + message);
        Map<String, Object> data = jsonConvertor.convertToMap(message);
        String env = data.get("env").toString();
        String conUrl = data.get("conUrl").toString();
        String adminUrl = data.get("adminUrl").toString();
        String status = systemViewServicePoller.registerUrl(env, conUrl, adminUrl, "/topic/registerServerReply");
        return new OutputMessage(status);
    }

    @MessageMapping("/systemViewGetCmd")
    @SendTo("/topic/registerServerReply")
    public OutputMessage getSystem(String message) throws Exception {
        LOG.info("Got getSystem Message " + message);
        systemViewServicePoller.sendRegisteredServer("/topic/registerServerReply");
        return new OutputMessage("Sent");
    }

    @MessageMapping("/systemViewModelGetCmd")
    @SendTo("/topic/systemViewModelGetReply")
    public OutputMessage getSavedSystemViewModel(String message) throws Exception {
        LOG.info("Got getSavedSystemViewModel Message " + message);
        String systemViewModel = userPreferencesService.getConfig(UserPreferencesService.SYSTEM_VIEW_MODEL);
        return new OutputMessage(systemViewModel);
    }

    @MessageMapping("/systemViewModelSaveCmd")
    @SendTo("/topic/systemViewModelSaveReply")
    public OutputMessage saveSystemViewModel(String message) throws Exception {
        LOG.info("Got saveSystemViewModel  Message " + message);
        userPreferencesService.saveConfig(UserPreferencesService.SYSTEM_VIEW_MODEL, message);
        return new OutputMessage("Saved");
    }

    @MessageMapping("/systemDiagramViewGetCmd")
    @SendTo("/topic/systemDiagramViewGetFeed")
    public OutputMessage getSystemDiagramView(String message) throws Exception {
        LOG.info("Got getSystemDiagramView Message " + message);
        List<Map<String, Object>> servers = new ArrayList<>();
        servers.add(createServer("LDN_PRIMARY", "LONDON", new String[]{"LDN_BACKUP", "FFT_PRIMARY", "FFT_BACKUP"}, new String[]{"PriceFeedPublisher", "JamesTraderClient", "KateSalesClient"}));
        servers.add(createServer("LDN_BACKUP", "LONDON", new String[]{"LDN_PRIMARY"}, new String[]{"RFQFeedPublisher", "BBGTradeFeedPublisher"}));
        servers.add(createServer("FFT_PRIMARY", "FRANKFURT", new String[]{"FFT_BACKUP"}, new String[]{"BBGQuoteGateway", "DeepakTraderClient"}));
        servers.add(createServer("FFT_BACKUP", "FRANKFURT", new String[]{"FFT_PRIMARY"}, new String[]{"HitRatioReportSubscriber"}));
        Map<String, Object> all = new HashMap<>();
        all.put("servers", servers);
        return new OutputMessage(jsonConvertor.convertToString(all));
    }

    @MessageMapping("/systemDiagramViewUpdateCmd")
    @SendTo("/topic/systemDiagramViewUpdateFeed")
    public void getSystemDiagramUpdateView(String message) throws Exception {
        LOG.info("Got getSystemDiagramUpdateView Message " + message);
        sendClientStream("/topic/systemDiagramViewUpdateFeed");

    }

    private Map<String, Object> createServer(String name, String group, String[] rep, String[] allClients) {
        Map<String, Object> server = new HashMap<>();
        server.put("name", name);
        server.put("group", group);
        String date = dateFormatter.format(LocalDateTime.now());
        server.put("connectedTime", date);
        server.put("messagesIn", random.nextInt(1500000));
        server.put("messagesOut", random.nextInt(100000));
        List<Map<String, Object>> replList = new ArrayList<>();
        for (String repLoc : rep) {
            Map<String, Object> replication = new HashMap();
            replication.put("name", repLoc);
            replication.put("type", "sync");
            replication.put("secondsBehind", random.nextInt(100) / 10);
            replList.add(replication);
        }
        server.put("replication", replList);

        List<Map<String, Object>> clientList = new ArrayList<>();
        for (String client : allClients) {
            clientList.add(createClient(client, name));
        }
        server.put("clients", clientList);
        return server;

    }

    private Map<String, Object> createClient(String client, String serverName) {
        Map<String, Object> newClient = new HashMap();
        newClient.put("name", client);
        String date = dateFormatter.format(LocalDateTime.now());
        newClient.put("connectedTime", date);
        newClient.put("messagesIn", random.nextInt(15000));
        newClient.put("messagesOut", random.nextInt(1000));
        String type = "INOUT";
        if (serverName.contains("PRIMARY")) {
            type = "IN";
        } else if (serverName.contains("BACKUP")) {
            type = "OUT";
        }
        if (client.equals("DeepakTraderClient")) {
            type = "INOUT";
        }
        newClient.put("type", type);
        newClient.put("msg_in", 10);
        newClient.put("msg_out", 170);
        newClient.put("ampsServer", serverName);
        return newClient;
    }

    private void sendClientStream(final String replyAddress) {
        new Thread(() -> {
            int count = 0;
            boolean client = true;
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String prefix = "GovyTraderClient";
                if (!client) {
                    prefix = "QuotePublisher";
                }
                client = !client;
                Map<String, Object> newClientsUpdates = new HashMap();
                List<Map<String, Object>> updatedClients = new ArrayList<>();
                updatedClients.add(createClient(getRandomClientName(prefix), "FFT_BACKUP"));
                newClientsUpdates.put("updatedClients", updatedClients);

                List<Map<String, Object>> deletedClients = new ArrayList<>();
                deletedClients.add(createClient(getRandomClientName(prefix), "FFT_BACKUP"));
                newClientsUpdates.put("deletedClients", deletedClients);
                String json = jsonConvertor.convertToString(newClientsUpdates);
                System.out.println("sending " + json);
                template.convertAndSend(replyAddress, new OutputMessage(json));
            }
        }).start();
    }

    private String getRandomClientName(String prefix) {
        int ran = random.nextInt(2);
        return prefix + "_" + ran;
    }
}