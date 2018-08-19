package headfront.jetfuelview.graph;

import com.mxgraph.view.mxGraph;

/**
 * Created by Deepak on 18/08/2018.
 */
public class JetFuelGraph extends mxGraph {
    public JetFuelGraph() {
        setResetEdgesOnMove(true);
        setCellsResizable(false);
        setAutoSizeCells(true);
        setCellsEditable(false);
        setEdgeLabelsMovable(false);
        setDisconnectOnMove(false);
        setDropEnabled(false);
        setKeepEdgesInForeground(true);
        getSelectionModel().setSingleSelection(false);
        setAllowDanglingEdges(false);
        setEnabled(false);
        setConstrainChildren(true);
        setExtendParents(true);
        setExtendParentsOnAdd(true);
        setDefaultOverlap(0);
    }

    @Override
    public String getToolTipForCell(Object cell) {
        return super.getToolTipForCell(cell);
    }
}
