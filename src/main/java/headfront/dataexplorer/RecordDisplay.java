package headfront.dataexplorer;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by Deepak on 28/06/2016.
 */
public enum RecordDisplay {

    TEXTAREA("Records in Textarea", "images/icons/txt-icon.png"),
    VERTICAL("Records Vertically", "images/icons/column-icon.png"),
    HORIZONTAL("Records Horizontally", "images/icons/row-icon.png"),
    TREE("Records In Tree", "images/icons/tree.png");

    private String displayName;
    private String imageName;
    private Image image;

    RecordDisplay(String displayName, String imageName) {
        this.displayName = displayName;
        this.imageName = imageName;
        image = new Image(imageName);
    }

    public String getDisplayName() {
        return displayName;
    }


    public ImageView getIcon() {
        ImageView icon = new ImageView(image);
        icon.setFitHeight(15);
        icon.setFitWidth(15);
        return icon;
    }

    public String getImageName() {
        return imageName;
    }
}
