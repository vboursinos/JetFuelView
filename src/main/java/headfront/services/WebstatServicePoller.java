package headfront.services;

import headfront.messages.OutputMessage;
import headfront.utils.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.util.Assert.notNull;

/**
 * Created by Deepak on 03/04/2016.
 */
@Service
public class WebstatServicePoller extends WebServicePoller {

    private static final Logger LOG = LoggerFactory.getLogger(WebstatServicePoller.class);
    private int webServicePollInterval = 5;

    @Override
    public void loadExistingConfig() {
        String webServicePollerConfig = userPreferencesService.getConfig(UserPreferencesService.WEB_SERVICE_POLLER);
        if (webServicePollerConfig.length() > 0) {
            Map<String, Object> config = jsonParser.convertToMap(webServicePollerConfig);
            config.entrySet().stream().forEach(entry -> {
                LinkedHashMap value = (LinkedHashMap) entry.getValue();
                WebPollingRequest newRequest = new WebPollingRequest(value.get("name").toString(),
                        value.get("url").toString(),
                        value.get("replyAddress").toString(), true);
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
        try {
            RestTemplate restTemplate = new RestTemplate();
            String reply = restTemplate.getForObject(request.url + ".json", String.class);
            String[] split = request.url.split("/");
            String fieldName = split[split.length - 1];
            publishStatMessage(request.replyAddress, request.name, reply, fieldName);
        } catch (Exception e) {
            LOG.error("Could not process request " + request.name + " for url " + request.url, e);
            return false;
        }
        return true;
    }

    public String registerUrl(final String name, final String url, final String replyAddress) {
        notNull(name);
        notNull(url);
        notNull(replyAddress);
        if (activeWebPollingRequests.containsKey(name)) {
            return name + " already registered";
        }
        WebPollingRequest webPollingRequest = new WebPollingRequest(name, url, replyAddress, true);
        boolean success = doWebRequests(webPollingRequest);
        if (success) {
            activeWebPollingRequests.put(name, webPollingRequest);
            LOG.info("Registered " + webPollingRequest);
            String json = jsonParser.convertToString(activeWebPollingRequests);
            userPreferencesService.saveConfig(UserPreferencesService.WEB_SERVICE_POLLER, json);
            return "Successfully registered " + name;
        }
        return "Invalid Url. Please check";
    }

    private void publishStatMessage(final String replyAddress, final String name, String message, String fieldName) {
        Map<String, Object> data = jsonParser.convertToMap(message);
        try {
            Object leafNode = MessageUtil.getLeafNode(data, fieldName);
            Map<String, Object> dataToPublish = new HashMap<>();
            dataToPublish.put(fieldName, leafNode);
            dataToPublish.put("Updated", new Date().toString());
            template.convertAndSend(replyAddress, new OutputMessage(name + jsonParser.convertToString(dataToPublish)));
        } catch (Exception e) {
            LOG.error("Cant process " + name + " with data " + message, e);
        }
    }
}
