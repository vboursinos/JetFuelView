package headfront.controllers;


import headfront.messages.OutputMessage;
import headfront.services.SystemViewServicePoller;
import headfront.services.UserPreferencesService;
import headfront.utils.JacksonJsonConvertor;
import headfront.utils.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    private String[] servers = null;

    private Map<String, Map<String, Object>> allDataFromServer = new HashMap<>();

    @PostConstruct
    public void init() {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("JetFuelView.properties"));
            servers = properties.getProperty("servers").split(",");
            final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            ScheduledFuture<?> webServicePoller = scheduler.scheduleAtFixedRate(() -> {
                try {
                    for (String server : servers) {
                        final String serverStats = systemViewServicePoller.getServerStats(server);
                        if (serverStats.trim().length() > 1) {
                            updateState(serverStats);
                        }
                    }
                    Thread.sleep(100);
                } catch (Exception e) {
                    LOG.error("Exception thrown during processing request", e);
                }
            }, 0, 10, TimeUnit.SECONDS);

            // This to get the exception from the ScheduledExectureService
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    webServicePoller.get();
                } catch (Exception e) {
                    LOG.error("Exception thrown during processing webPollingRequests", e);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        List<Map<String, Object>> allServers = new ArrayList<>();
//        allServers.add(createServer("LDN_PRIMARY", "LONDON", new String[]{"LDN_BACKUP", "FFT_PRIMARY", "FFT_BACKUP"}, new String[]{"PriceFeedPublisher", "JamesTraderClient", "KateSalesClient"}));
//        allServers.add(createServer("LDN_BACKUP", "LONDON", new String[]{"LDN_PRIMARY"}, new String[]{"RFQFeedPublisher", "BBGTradeFeedPublisher"}));
//        allServers.add(createServer("FFT_PRIMARY", "FRANKFURT", new String[]{"FFT_BACKUP"}, new String[]{"BBGQuoteGateway", "DeepakTraderClient"}));
//        allServers.add(createServer("FFT_BACKUP", "FRANKFURT", new String[]{"FFT_PRIMARY"}, new String[]{"HitRatioReportSubscriber"}));
        allDataFromServer.values().forEach(allServers::add);
        Map<String, Object> all = new HashMap<>();
        all.put("servers", allServers);
        return new OutputMessage(jsonConvertor.convertToString(all));
    }

    @MessageMapping("/systemDiagramViewUpdateCmd")
    @SendTo("/topic/systemDiagramViewUpdateFeed")
    public void getSystemDiagramUpdateView(String message) throws Exception {
        LOG.info("Got getSystemDiagramUpdateView Message " + message);
//        sendClientStream("/topic/systemDiagramViewUpdateFeed");

    }

    private Map<String, Object> createServer(String name, String group, String date, Object reps, Object clients) {
        Map<String, Object> server = new HashMap<>();
        server.put("name", name);
        server.put("group", group);
        server.put("connectedTime", date);
        server.put("messagesIn", random.nextInt(1500000));
        server.put("messagesOut", random.nextInt(100000));
        if (reps != null) {
            List<Map<String, Object>> createdRep = new ArrayList<>();
            List<Map<String, Object>> repList = (List) reps;
            for (Map aRep : repList) {
                createdRep.add(createRep(aRep));
            }
            server.put("replication", createdRep);
        }

        if (clients != null) {
            List<Map<String, Object>> createdClients = new ArrayList<>();
            List<Map<String, Object>> clientList = (List) clients;
            for (Map aClient : clientList) {
                final String client_name = aClient.get("client_name").toString();
                if (!client_name.contains("amps-replication")) {
                    createdClients.add(createClient(aClient, name));
                }
            }
            server.put("clients", createdClients);
        }

        return server;

    }

    private Map<String, Object> createClient(Map client, String serverName) {
        Map<String, Object> newClient = new HashMap();
        newClient.put("name", client.get("client_name"));
        newClient.put("connectedTime", client.get("connect_time"));
        newClient.put("messagesIn", client.get("messages_in"));
        newClient.put("messagesOut", client.get("messages_out"));
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

    private Map<String, Object> createRep(Map rep) {
            Map<String, Object> replication = new HashMap();
            replication.put("name", rep.get("destination_name"));
            replication.put("type", rep.get("replication_type"));
            replication.put("secondsBehind", rep.get("seconds_behind"));
            return replication;
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
//                Map<String, Object> newClientsUpdates = new HashMap();
//                List<Map<String, Object>> updatedClients = new ArrayList<>();
//                updatedClients.add(createClient(getRandomClientName(prefix), "FFT_BACKUP"));
//                newClientsUpdates.put("updatedClients", updatedClients);
//
//                List<Map<String, Object>> deletedClients = new ArrayList<>();
//                deletedClients.add(createClient(getRandomClientName(prefix), "FFT_BACKUP"));
//                newClientsUpdates.put("deletedClients", deletedClients);
//                String json = jsonConvertor.convertToString(newClientsUpdates);
//                System.out.println("sending " + json);
//                template.convertAndSend(replyAddress, new OutputMessage(json));
            }
        }).start();
    }

    private String getRandomClientName(String prefix) {
        int ran = random.nextInt(2);
        return prefix + "_" + ran;
    }

    private void updateState(String allJson) {
        final Map<String, Object> mapData = jsonConvertor.convertToMap(allJson);
        final Map amps = (Map) mapData.get("amps");
        final Map instance = (Map) amps.get("instance");
        final Object name = MessageUtil.getLeafNode(instance, "name");
        final Object group = MessageUtil.getLeafNode(instance, "group");
        final Object timestamp = MessageUtil.getLeafNode(instance, "timestamp");
        final Object clients = MessageUtil.getLeafNode(instance, "clients");
        final Object replication = MessageUtil.getLeafNode(instance, "replication");
        allDataFromServer.put(group + ":" + name, createServer(name.toString(), group.toString(), timestamp.toString(),
                replication, clients));
    }
}