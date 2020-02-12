package headfront.amps.services;

import headfront.utils.StringUtils;
import headfront.utils.WebServiceRequest;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Created by Deepak on 18/09/2016.
 */
public class AmpsStatsLoader {

    private static final Logger LOG = LoggerFactory.getLogger(AmpsStatsLoader.class);
    private volatile boolean keepRunning = true;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    public AmpsStatsLoader(String connectionsStr, String adminPortStr, String stat,
                           Consumer<String> messageListener, int refreshTimeInSec, boolean useSecureHttp) {
        final String url = StringUtils.getAdminUrl(connectionsStr, adminPortStr, useSecureHttp) + "/instance/" + stat + ".json";
        if (refreshTimeInSec > 0) {
            new Thread(() -> {
                final long sleepTime = refreshTimeInSec * 1000;
                while (keepRunning) {
                    doRequest(messageListener, url, sleepTime);
                }
            }).start();
        } else {
            doRequest(messageListener, url, 0);
        }
    }

    public AmpsStatsLoader(String connectionsStr, String adminPortStr,
                           Consumer<String> messageListener, int refreshTimeInSec,
                           TreeItem<String> treeItem,
                           LocalDateTime startDate, LocalDateTime stopDate, boolean useSecureHttp) {
        String data = StringUtils.getFullTreePath(treeItem);
        final String url = StringUtils.getAmpsUrl(connectionsStr, adminPortStr,useSecureHttp) + "/" + data + ".json?t0=" + formatter.format(startDate) +
                "&t1=" + formatter.format(stopDate);
        if (refreshTimeInSec > 0) {
            new Thread(() -> {
                final long sleepTime = refreshTimeInSec * 1000;
                while (keepRunning) {
                    doRequest(messageListener, url, sleepTime);
                }
            }).start();
        } else {
            doRequest(messageListener, url, 0);
        }
    }

    private void doRequest(Consumer<String> messageListener, String url, long sleepTime) {
        try {
            String jsonToProcess = WebServiceRequest.doWebRequests(url);
            messageListener.accept(jsonToProcess);
            Thread.sleep(sleepTime);
        } catch (Exception e) {
            LOG.error("Exception thrown while processing stat request " + url, e);
        }
    }

    public void stopRunning() {
        keepRunning = false;
    }

}
