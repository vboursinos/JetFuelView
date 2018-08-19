package headfront.jetfuelview.graph;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static headfront.jetfuelview.graph.Styles.AMPS_SERVER_GOOD;

/**
 * Created by Deepak on 19/08/2018.
 */
public class JetFuelGraphComponent extends mxGraphComponent {

    private static final long serialVersionUID = 1821677322838455152L;

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
                            JMenuItem startJetFuelExplorer = new JMenuItem("Start JetFuel Explorer");
                            startJetFuelExplorer.addActionListener(et -> System.out.println("Start JetFuel Explorer"));
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