package headfront.graph;

import java.util.HashMap;
import java.util.Map;

import static headfront.graph.Styles.*;

/**
 * Created by Deepak on 19/08/2018.
 */
public class JetFuelGraphRenderer {

    private JetFuelGraph graph;

    public JetFuelGraphRenderer(JetFuelGraph graph) {
        this.graph = graph;
    }

    public void draw() {
        graph.getModel().beginUpdate();
        Map<String, Object> createdServers = new HashMap<>();
        int startx = 20;
        int starty = 35;
        int width = 200;
        int height = 40;
        int xPadding = 50;
        int yPadding = 50;
        Object defaultParent = graph.getDefaultParent();
        String[] allAmpsServer = {"PRIMARYLDN", "BACKUPLDN", "PRIMARYFFT", "BACKUPFFT"};
        Object user = graph.insertVertex(defaultParent, null, "ProcessFFT", 20, 20, 50, 50, USER_CONNECTED);
        Object jetFuel = graph.insertVertex(defaultParent, null, "DeepakJetFuel", 150, 20, 50, 50, JETFUEL_CONNECTED);
        Object group = graph.insertVertex(defaultParent, null, "Main", 240, 150,
                (width + yPadding) * 2,
                (((allAmpsServer.length + 1) / 2) * (height + xPadding)) + (starty), AMPS_GROUP);
        int count = 0;
        for (int i = 0; i < allAmpsServer.length; i++) {
            final Object ampsSever = graph.insertVertex(group, allAmpsServer[i], allAmpsServer[i], startx, starty, width, height, AMPS_SERVER_GOOD);
            count++;
            if (count == 2) {
                count = 0;
                startx = 20;
                starty = starty + height + yPadding;
            } else {
                startx = startx + width + xPadding;
            }
            createdServers.put(allAmpsServer[i], ampsSever);
        }

        graph.insertEdge(defaultParent, "1", "", user, createdServers.get(allAmpsServer[0]), AMPS_LINK_GOOD);
        graph.insertEdge(defaultParent, "2", "", user, jetFuel, AMPS_LINK_GOOD);
        graph.insertEdge(defaultParent, "3", "", jetFuel, user, AMPS_LINK_GOOD);
        graph.insertEdge(group, "4", "", createdServers.get(allAmpsServer[0]), createdServers.get(allAmpsServer[1]), AMPS_LINK_GOOD);
        graph.insertEdge(group, "5", "", createdServers.get(allAmpsServer[1]), createdServers.get(allAmpsServer[0]), AMPS_LINK_GOOD);

        graph.getModel().endUpdate();
    }
}
