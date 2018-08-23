package headfront.jetfuelview.graph;

import headfront.convertor.JacksonJsonConvertor;
import headfront.jetfuelview.panel.JetFuelViewStatusBar;
import headfront.utils.MessageUtil;
import headfront.utils.StringUtils;
import headfront.utils.WebServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.swing.*;
import java.io.FileReader;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static headfront.jetfuelview.graph.Styles.*;

/**
 * This is a horrible class. This was written a while ago in a different project. This needs a full refactor
 */
public class JetFuelGraphModel {

    private static final Logger LOG = LoggerFactory.getLogger(JetFuelGraphModel.class);


    private Map<String, Map<String, Object>> allDataFromServer = new ConcurrentHashMap<>();
    private Set<String> unknownServers = new HashSet<>();
    private Map<String, String> mappedServers = new ConcurrentHashMap<>();
    private static final String SEPARATOR = "=";
    private JacksonJsonConvertor jsonConvertor = new JacksonJsonConvertor();

    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
    private SimpMessagingTemplate template;
    private Random random = new Random();
    private JetFuelGraph graph;
    private String propertiesFile;
    private String username;
    private String credentials;
    private String environment;
    private JetFuelViewStatusBar jetFuelViewStatusBar;

    public JetFuelGraphModel(JetFuelGraph graph, String propertiesFile, String username, String credentials, String environment,
                             JetFuelViewStatusBar jetFuelViewStatusBar) {
        this.graph = graph;
        this.propertiesFile = propertiesFile;
        this.username = username;
        this.credentials = credentials;
        this.environment = environment;
        this.jetFuelViewStatusBar = jetFuelViewStatusBar;
    }

    public void updateFromServer(boolean redraw) {
        new Thread(() -> {
            try {
                String fileToLoad = "config/" + propertiesFile;
                LOG.info("Loading " + fileToLoad);
                Properties properties = new Properties();
                properties.load(new FileReader(fileToLoad));
                final String servers = properties.getProperty("servers");
                final String adminPorts = properties.getProperty("adminports");
                final String environment = properties.getProperty("environment");
                final String[] allServers = servers.split(",");
                final String[] allAdminPorts = adminPorts.split(",");
                allDataFromServer.clear();
                unknownServers.clear();
                mappedServers.clear();
                jetFuelViewStatusBar.clearCount();
                for (int i = 0; i < allServers.length; i++) {
                    String serverToLoad = allServers[i];
                    String adminPortToLoad = allAdminPorts[i];
                    String metaDatUrl = StringUtils.getAdminUrl(serverToLoad, adminPortToLoad) + ".json";
                    final String serverStats = getServerConfig(metaDatUrl);
                    if (serverStats != null && serverStats.trim().length() > 1) {
                        updateState(serverStats, serverToLoad);
                    } else {
                        String s = StringUtils.getShortServerAndPortFromUrl(serverToLoad);
                        final String registeredServer = mappedServers.get(serverToLoad);
                        if (registeredServer != null) {
                            final String[] split = registeredServer.split(SEPARATOR);
                            allDataFromServer.put(registeredServer, createServer(split[1], split[0], "",
                                    null, null, null));
                        } else {
                            unknownServers.add(s);
                        }
                    }
                }
                if (redraw) {
                    draw();
                }
            } catch (Exception e) {
                LOG.error("Unable to load graph model", e);
            }
        }).start();
    }

    private Map<String, Object> createServer(String name, String group, String date, Object reps, Object clients, String serverHost) {
        Map<String, Object> server = new HashMap<>();
        server.put("name", name);
        server.put("group", group);
        server.put("connectedTime", date);
        server.put("messagesIn", random.nextInt(1500000));
        server.put("messagesOut", random.nextInt(100000));
        server.put("serverHost", serverHost);
        server.put("environment", environment);
        List<Map<String, Object>> createdRep = new ArrayList<>();
        if (reps != null) {
            List<Map<String, Object>> repList = (List) reps;
            for (Map aRep : repList) {
                createdRep.add(createRep(aRep));
            }
        }
        server.put("replication", createdRep);

        List<Map<String, Object>> createdClients = new ArrayList<>();
        if (clients != null) {
            List<Map<String, Object>> clientList = (List) clients;
            for (Map aClient : clientList) {
                final String client_name = aClient.get("client_name").toString();
                if (!client_name.contains("amps-replication")) {
                    createdClients.add(createClient(aClient, name));
                }
            }
        }
        server.put("clients", createdClients);

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
        final Object name = rep.get("name");
        final Object group = rep.get("destination_group_name");
        replication.put("name", name);
        replication.put("type", rep.get("replication_type"));
        replication.put("secondsBehind", rep.get("seconds_behind"));
        final String fullServerName = getFullServerName("" + group, "" + name);
        if (!allDataFromServer.containsKey(fullServerName)) {
            allDataFromServer.put(fullServerName, createServer(name.toString(), group.toString(), "",
                    null, null, null));
        }
        return replication;
    }

    private String getFullServerName(String group, String name) {
        return group + SEPARATOR + name;
    }

    public Map<String, Object> getAmpsServer(String id) {
        return allDataFromServer.get(id);
    }

    private String getRandomClientName(String prefix) {
        int ran = random.nextInt(2);
        return prefix + "_" + ran;
    }

    private void updateState(String allJson, String url) {
        final Map<String, Object> mapData = jsonConvertor.convertToMap(allJson);
        final Map amps = (Map) mapData.get("amps");
        final Map instance = (Map) amps.get("instance");
        final Object name = MessageUtil.getLeafNode(instance, "name");
        final Object group = MessageUtil.getLeafNode(instance, "group");
        final Object timestamp = MessageUtil.getLeafNode(instance, "timestamp");
        final Object clients = MessageUtil.getLeafNode(instance, "clients");
        final Object replication = MessageUtil.getLeafNode(instance, "replication");
        final String fullServerName = getFullServerName("" + group, "" + name);
        final String serverHost = StringUtils.getShortServerAndPortFromUrl(url);
        allDataFromServer.put(fullServerName, createServer(name.toString(), group.toString(), timestamp.toString(),
                replication, null, serverHost));
        mappedServers.put(url, fullServerName);
    }

    public String getServerConfig(String url) {
        try {
            return WebServiceRequest.doWebRequests(url);
        } catch (Exception e) {
            //LOG.error("Could not process request " + url, e);
        }
        return "";
    }

    public void draw() {
        SwingUtilities.invokeLater(() -> {
            graph.getModel().beginUpdate();
            graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));

            int ampsServerX = 20;
            int ampsServerY = 30;
            int ampsServerWidth = 200;
            int ampsServerHeight = 40;
            int ampsServerPaddingX = 50;
            int ampsServerPaddingY = 50;
            int noOfServersPerGroup = 4;
            int initialAmpsGroupX = 100;
            int ampsGroupX = 100;
            int ampsGroupY = 150;
            Object defaultParent = graph.getDefaultParent();
            Object user = graph.insertVertex(defaultParent, null, "TraderMark", 20, 20, 50, 50, USER_CONNECTED);
            Object server = graph.insertVertex(defaultParent, null, "pricePublisher", 130, 20, 50, 50, AMPS_COMPONENT_GOOD);
            Object jetFuel = graph.insertVertex(defaultParent, null, "DeepakJetFuel", 240, 20, 50, 50, JETFUEL_CONNECTED);
            final Collection<Map<String, Object>> values = allDataFromServer.values();
            Map<String, List<String>> replications = new HashMap<>();
            Map<String, List<String>> groupToAmpsServers = new TreeMap<>();
            values.forEach(map -> {
                final String newGroup = map.get("group").toString();
                final String newamps = map.get("name").toString();
                List<String> servers = groupToAmpsServers.get(newGroup);
                if (servers == null) {
                    servers = new ArrayList<>();
                    groupToAmpsServers.putIfAbsent(newGroup, servers);
                }
                servers.add(newamps);

                List<Map<String, Object>> reps = (List<Map<String, Object>>) map.get("replication");
                final List<String> repNames = reps.stream().map(m -> m.get("name").toString()).collect(Collectors.toList());
                replications.put(map.get("name").toString(), repNames);
            });
            int groupCount = 0;
            Map<String, Object> createdGroups = new HashMap<>();
            for (String groupName : groupToAmpsServers.keySet()) {
                final int groupWith = (ampsServerWidth + ampsServerPaddingY) * 2;
                final List<String> strings = groupToAmpsServers.get(groupName);
                noOfServersPerGroup = strings.size();
                if (noOfServersPerGroup % 2 != 0) {
                    noOfServersPerGroup++;
                }
                final int groupHeight = ((noOfServersPerGroup / 2) * (ampsServerHeight + ampsServerPaddingX)) + (ampsServerY);
                Object group = graph.insertVertex(defaultParent, null, groupName, ampsGroupX, ampsGroupY,
                        groupWith, groupHeight, AMPS_GROUP);
                groupCount++;
                if (groupCount == 2) {
                    groupCount = 0;
                    ampsGroupX = initialAmpsGroupX;
                    ampsGroupY = ampsGroupY + groupHeight + 100;
                } else {
                    ampsGroupX = ampsGroupX + groupWith + 50;
                }
                createdGroups.put(groupName, group);
            }
            int badAmpsServerX = 500;
            for (String unknowsAmpsServer : unknownServers) {
                unknowsAmpsServer = "Unreachable " + StringUtils.getServerAndPortFromUrl(unknowsAmpsServer);
                graph.insertVertex(defaultParent, null, unknowsAmpsServer, badAmpsServerX, 20, ampsServerWidth, ampsServerHeight, AMPS_SERVER_BAD);
                badAmpsServerX = badAmpsServerX + ampsServerWidth + ampsServerPaddingX;
                jetFuelViewStatusBar.incrementInactiveCount();
            }

            Map<String, Object> createdServers = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : groupToAmpsServers.entrySet()) {
                int ampsCount = 0;
                ampsServerX = 20;
                ampsServerY = 35;
                String groupKey = entry.getKey();
                Object ampsGroupObj = createdGroups.get(groupKey);
                List<String> allServers = entry.getValue();
                for (String ampServer : allServers) {
                    String fullServerName = getFullServerName(groupKey, ampServer);
                    Map<String, Object> fullServerDetails = allDataFromServer.get(fullServerName);
                    if (fullServerDetails != null) {
                        String connectedTime = (String) fullServerDetails.get("connectedTime");
                        String style = AMPS_SERVER_GOOD;
                        if (connectedTime != null && connectedTime.length() == 0) {
                            style = AMPS_SERVER_BAD;
                        }
                        String ampsNameToUse = ampServer;
                        final Object serverHost = fullServerDetails.get("serverHost");
                        if (serverHost != null) {
                            ampsNameToUse = ampsNameToUse + "\n[" + serverHost + "]";
                        }
                        final Object ampsSever = graph.insertVertex(ampsGroupObj,
                                fullServerName, ampsNameToUse, ampsServerX, ampsServerY, ampsServerWidth, ampsServerHeight, style);
                        ampsCount++;
                        if (ampsCount == 2) {
                            ampsCount = 0;
                            ampsServerX = 20;
                            ampsServerY = ampsServerY + ampsServerHeight + ampsServerPaddingY;
                        } else {
                            ampsServerX = ampsServerX + ampsServerWidth + ampsServerPaddingX;
                        }
                        createdServers.put(ampServer, ampsSever);
                        jetFuelViewStatusBar.incrementActiveCount();
                    }
                }
            }

//        graph.insertEdge(defaultParent, "1", "", user, createdServers.get(allAmpsServer[0]), AMPS_LINK_GOOD);
//        graph.insertEdge(defaultParent, "2", "", user, jetFuel, AMPS_LINK_GOOD);
//        graph.insertEdge(defaultParent, "3", "", jetFuel, user, AMPS_LINK_GOOD);
            for (Map.Entry repEntry : replications.entrySet()) {
                final String from = repEntry.getKey().toString();
                final List<String> reps = (List<String>) repEntry.getValue();
                reps.forEach(arep -> {
                    final Object fromServer = createdServers.get(from);
                    final Object toServer = createdServers.get(arep);

                    graph.insertEdge(createdGroups.get(fromServer), null, "", fromServer, toServer, AMPS_LINK_GOOD);
                });
            }
            graph.getModel().endUpdate();
        });
    }
}
