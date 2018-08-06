package headfront.services;

import headfront.messages.OutputMessage;
import headfront.utils.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.util.Assert.notNull;

/**
 * Created by Deepak on 03/04/2016.
 */
@Service
public class SystemViewServicePoller extends WebServicePoller {

    private static final Logger LOG = LoggerFactory.getLogger(SystemViewServicePoller.class);
    private int webServicePollInterval = 500;
    private Map<String, Map<String, Object>> systemData = new HashMap();


    @Override
    public void loadExistingConfig() {
        String systemViewPollerConfig = userPreferencesService.getConfig(UserPreferencesService.SYSTEM_VIEW_POLLER);
        if (systemViewPollerConfig.length() > 0) {
            Map<String, Object> config = jsonParser.convertToMap(systemViewPollerConfig);
            config.entrySet().stream().forEach(entry -> {
                LinkedHashMap value = (LinkedHashMap) entry.getValue();
                WebPollingRequest newRequest = new WebPollingRequest(value.get("name").toString(),
                        value.get("url").toString(),
                        value.get("replyAddress").toString(), false, value.get("connectionUrl").toString());
                activeWebPollingRequests.put(entry.getKey(), newRequest);
            });
        }
    }

    @Override
    public int getPollInterval() {
        return webServicePollInterval;
    }

    @Override
    public boolean doWebRequests(Object req) {
        WebPollingRequest request = (WebPollingRequest) req;
        String url = request.url;
        try {
            RestTemplate restTemplate = new RestTemplate();
            String reply = restTemplate.getForObject(url + "/amps/instance/config.xml", String.class);
//            LOG.info("Got " + reply);
            publishMessage(request.replyAddress, request.name, reply);
            if (reply.contains("Config not found")) {
                return false;
            }
        } catch (Exception e) {
            LOG.error("Could not process request " + url, e);
        }
        return true;
    }

    public String getServerStats(String url) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            LOG.error("Could not process request " + url, e);
        }
        return "";
    }

    public void sendRegisteredServer(String replyAddress) {
        activeWebPollingRequests.values().forEach(request -> {
            WebPollingRequest webReq = (WebPollingRequest) request;
            Map<String, Object> map = new HashMap<>();
            map.put("Name", webReq.name);
            map.put("Admin", webReq.url);
            String jsonToSend = jsonParser.convertToString(map);
            template.convertAndSend(replyAddress, new OutputMessage(jsonToSend));
        });

    }

    public String registerUrl(final String env, final String connectionStr, final String adminUrl, final String replyAddress) {
        notNull(env);
        notNull(connectionStr);
        notNull(adminUrl);
        notNull(replyAddress);
        // lets check the admin url is correct
        String urlForName = adminUrl + "/amps/instance/name";
        String ampsInstanceName = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            ampsInstanceName = restTemplate.getForObject(urlForName, String.class);
            ampsInstanceName = MessageUtil.removeHtml(ampsInstanceName);
        } catch (Exception e) {
            LOG.error("Could not get ampsName with url " + urlForName, e);
            return "Invalid admin url.";
        }
//        if (ampsInstanceName == null) {
//            return "Could not get ampsName with url " + urlForName;
//        }

        if (activeWebPollingRequests.containsKey(ampsInstanceName)) {
            return adminUrl + " already registered";
        }
        WebPollingRequest webPollingRequest = new WebPollingRequest(ampsInstanceName, adminUrl, replyAddress, false, connectionStr);
        activeWebPollingRequests.put(ampsInstanceName, webPollingRequest);
        LOG.info("Registered " + webPollingRequest);
        String json = jsonParser.convertToString(activeWebPollingRequests);
        userPreferencesService.saveConfig(UserPreferencesService.SYSTEM_VIEW_POLLER, json);
        return "Successfully registered env " + env + " @ admin url " + adminUrl;

    }

    private void publishMessage(final String replyAddress, final String name, String message) {
        // todo use proper xml to map parser
        String ourPart = message.split("</Group>")[0];
        String[] parts = ourPart.split("</Name>");
        String ampsName = parts[0].split("<Name>")[1];
        String groupName = parts[1].split("<Group>")[1];
        Map<String, Object> objectMap = systemData.get(ampsName);
        if (objectMap == null) {
            objectMap = new HashMap<>();
            systemData.put(ampsName, objectMap);
        }
        objectMap.put("Group", groupName);
        objectMap.put("Name", ampsName);
        String jsonToSend = jsonParser.convertToString(systemData);

        LOG.info("Sending systemData " + jsonToSend);
        template.convertAndSend(replyAddress, new OutputMessage(name + jsonToSend));
    }
}
