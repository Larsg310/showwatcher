package nl.larsgerrits.showwatcher.components.poster;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class PosterDownloadList extends AnchorPane
{
    private Text textTitle = new Text();
    private VBox boxSeasons = new VBox();
    
    public PosterDownloadList()
    {
        setPrefHeight(155);
    }
    
    // private void setImage(Image image)
    // {
    //     imageView.setImage(image);
    //     double imageHeight = 188 * (image.getHeight() / image.getWidth());
    //     // textEpisode.setLayoutX(26 + image.getWidth());
    //
    //     setPrefHeight(20 + imageHeight);
    // }
}
