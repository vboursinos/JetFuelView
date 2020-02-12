package headfront.services;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import headfront.convertor.JacksonJsonConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Deepak on 30/03/2016.
 */

public abstract class WebServicePoller {

    private static final Logger LOG = LoggerFactory.getLogger(WebServicePoller.class);

    protected Map<String, Object> activeWebPollingRequests = new HashMap<>();
    protected JacksonJsonConvertor jsonParser = new JacksonJsonConvertor();
    @Autowired
    protected UserPreferencesService userPreferencesService;
    @Autowired
    protected SimpMessagingTemplate template;

    public abstract void loadExistingConfig();

    public abstract int getPollInterval();

    public abstract boolean doWebRequests(Object req);

    @PostConstruct
    public void init() {
        loadExistingConfig();
        startScheduler();
    }

    private void startScheduler() {
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> webServicePoller = scheduler.scheduleAtFixedRate(() -> {
            try {
                activeWebPollingRequests.values().forEach(this::doWebRequests);
                Thread.sleep(100);
            } catch (Exception e) {
                LOG.error("Exception thrown during processing request", e);
            }
        }, 0, getPollInterval(), TimeUnit.SECONDS);

        // This to get the exception from the ScheduledExectureService
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                webServicePoller.get();
            } catch (Exception e) {
                LOG.error("Exception thrown during processing webPollingRequests", e);
            }
        });
        LOG.info("Started WebServicePoller");
    }

    @JsonAutoDetect
    protected class WebPollingRequest {
        @JsonProperty
        String name;
        @JsonProperty
        String url;
        @JsonProperty
        String connectionUrl;
        @JsonProperty
        String replyAddress;
        boolean isWebStat = false;

        WebPollingRequest(String name, String url, String replyAddress, boolean webStat) {
            this(name, url, replyAddress, webStat, "");
        }

        WebPollingRequest(String name, String url, String replyAddress, boolean webStat, String connectionUrl) {
            this.name = name;
            this.url = url;
            this.replyAddress = replyAddress;
            isWebStat = webStat;
            this.connectionUrl = connectionUrl;
        }

        @Override
        public String toString() {
            return "WebPollingRequest{" +
                    "name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    ", connectionUrl='" + connectionUrl + '\'' +
                    ", replyAddress='" + replyAddress + '\'' +
                    ", isWebStat=" + isWebStat +
                    '}';
        }
    }
}
