package headfront.jetfuelview.graph;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import headfront.jetfuelview.JetFuelView;
import headfront.jetfuelview.util.ProcessLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static headfront.jetfuelview.graph.Styles.AMPS_SERVER_GOOD;

/**
 * Created by Deepak on 19/08/2018.
 */
public class JetFuelGraphComponent extends mxGraphComponent {

    private static final long serialVersionUID = 1821677322838455152L;

    private static final Logger LOG = LoggerFactory.getLogger(ProcessLauncher.class);

    public JetFuelGraphComponent(mxGraph model) {
        super(model);
        configureGraphComponent(model);
    }

    @Override
    public mxGraphHandler createGraphHandler() {
        return new mxGraphHandler(this) {
            @Override
            protected boolean shouldRemoveCellFromParent(Object parent, Object[] cells, MouseEvent e) {
                return false;
            }
        };
    }

    private void configureGraphComponent(mxGraph model) {
        getViewport().setOpaque(true);
        getViewport().setBackground(Color.WHITE);
        setConnectable(false);
        setToolTips(true);
        ToolTipManager.sharedInstance().registerComponent(this);
        getGraphControl().addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                mxCell cell = (mxCell) getCellAt(e.getX(), e.getY());

                if (cell != null) {
                    if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu menu = new JPopupMenu();
                        if (cell.getStyle().equals(AMPS_SERVER_GOOD)) {
                            System.out.println("cell=" + model.getLabel(cell));
                            Map<String, Object> server = new HashMap<>();
                            server.put("name", "Sarah");
                             JMenuItem startJetFuelExplorer = new JMenuItem("Start JetFuel Explorer");
                            startJetFuelExplorer.addActionListener(et -> {
                                try {
                                    ProcessLauncher.exec(server);
                                } catch (Exception e1) {
                                    LOG.error("Unable to launch JetFuelExplorer for " + model.getLabel(cell));
                                }
                            });
                            menu.add(startJetFuelExplorer);
                        }
                        JMenuItem properties = new JMenuItem("Properties");
                        properties.addActionListener(et -> System.out.println("Properties"));
                        menu.add(properties);
                        menu.show(JetFuelGraphComponent.this, e.getX(), e.getY());
                    }
                }
            }
        });
    }
}