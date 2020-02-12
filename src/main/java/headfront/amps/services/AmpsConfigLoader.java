package headfront.amps.services;

import headfront.convertor.JacksonJsonConvertor;
import headfront.guiwidgets.PopUpDialog;
import headfront.utils.StringUtils;
import headfront.utils.WebServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by Deepak on 18/09/2016.
 */
public class AmpsConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(AmpsConfigLoader.class);
    private String connectionsStr = "";
    private String adminPortStr = "";
    private TopicService topicService;
    private boolean useSecureHttp;
    private JacksonJsonConvertor jsonConvertor = new JacksonJsonConvertor();
    private AmpsLoader ampsPoller = null;
    private String instanceName = "";
    private volatile boolean inited = false;

    public AmpsConfigLoader(String connectionsStr, String adminPortStr, TopicService topicService, boolean useSecureHttp) {
        this.connectionsStr = connectionsStr;
        this.adminPortStr = adminPortStr;
        this.topicService = topicService;
        this.useSecureHttp = useSecureHttp;
    }

    public void loadMetaData() {
        loadConfig();
        loadStats();
        if (ampsPoller != null) {
            ampsPoller.setKeepRunning(false);
            ampsPoller.interrupt();
        }
        ampsPoller = new AmpsLoader();
        ampsPoller.start();
    }

    private void loadConfig() {
        String configUrl = StringUtils.getConfigUrl(connectionsStr, adminPortStr, useSecureHttp);
        String xml = WebServiceRequest.doWebRequests(configUrl);
        if (xml != null) {
            topicService.setTopicDataFromConfig(xml);
        } else {
            String message = "Could not get config from amps with url " + StringUtils.removePassword(configUrl) + " config data will be missing";
            LOG.error(message);
            PopUpDialog.showErrorPopup("Config not found", message);
        }
//        if (!inited) {
//            inited = true;
//
//            new Thread(() -> {
//                // load large topic dont worry about failures
//                try {
//                    Thread.sleep(5100);
//                    topicService.getAllTopicsNamesOnly().forEach(t -> {
//                        if (topicService.isLargeSow(t)) {
//                            topicService.getTopicMetaData(t, null);
//                        }
//                    });
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        }
    }

    public String getInstanceName() {
        return instanceName;
    }

    private void loadStats() {
        String metaDatUrl = StringUtils.getAdminUrl(connectionsStr, adminPortStr, useSecureHttp) + ".json";
        String json = WebServiceRequest.doWebRequests(metaDatUrl);
        if (json != null) {
            Map<String, Object> stringObjectMap = jsonConvertor.convertToMap(json);
            topicService.setAmpsStatsMetaData(stringObjectMap);
            topicService.setFullAmpsJsonFile(stringObjectMap);
            final Map amps = (Map) stringObjectMap.get("amps");
            if (amps != null) {
                final Map instance = (Map) amps.get("instance");
                if (instance != null) {
                    final Object name = instance.get("name");
                    final Object group = instance.get("group");
                    if (name != null && group != null) {
                        instanceName = " [Instance - " + group + "." + name + "]";
                    } else if (name != null) {
                        instanceName = " [Instance -" + name + "]";
                    }
                }
            }
        } else {
            String message = "Could not load amps metadata with url " + StringUtils.removePassword(metaDatUrl) + " . " +
                    "Stats and TimeSeries will not work correctly";
            LOG.error(message);
            PopUpDialog.showErrorPopup("Could not load stat meta data.", message);
        }
    }


    class AmpsLoader extends Thread {
        private boolean keepRunning = true;

        public void run() {
            while (keepRunning) {
                String metaDatUrl = StringUtils.getAdminUrl(connectionsStr, adminPortStr,useSecureHttp ) + ".json";
                String json = WebServiceRequest.doWebRequests(metaDatUrl);
                if (json != null) {
                    Map<String, Object> stringObjectMap = jsonConvertor.convertToMap(json);
                    topicService.setFullAmpsJsonFile(stringObjectMap);
                }
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }

        public void setKeepRunning(boolean keepRunning) {
            this.keepRunning = keepRunning;
        }
    }
}
