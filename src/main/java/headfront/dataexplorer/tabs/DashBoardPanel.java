package headfront.dataexplorer.tabs;

import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Deepak on 16/07/2017.
 */
public class DashBoardPanel extends Tab {

    private static final Logger LOG = LoggerFactory.getLogger(DashBoardPanel.class);

    public final static Image jetfuelDashBoardImage = new Image("images/icons/JetFuel-DashBoard.png");

    public DashBoardPanel() {
        super("DashBoard");
        setContent(new ImageView(jetfuelDashBoardImage));
    }
}
