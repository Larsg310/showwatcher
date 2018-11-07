package nl.larsgerrits.showwatcher.components.poster;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import nl.larsgerrits.showwatcher.manager.ImageManager;
import nl.larsgerrits.showwatcher.show.TVShow;

public class PosterShow extends AnchorPane
{
    private TVShow show;
    
    @FXML
    private ImageView imageView;
    
    @FXML
    private Text text;
    
    public PosterShow(TVShow show)
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/show_poster.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        
        this.show = show;
        
        try
        {
            loader.load();
        }
        catch (Exception e) {e.printStackTrace();}
    }
    
    @FXML
    public void initialize()
    {
        text.setText(show.getTitle());
        ImageManager.getPosterURLForTVShow(show, imageView::setImage);
    }
}
