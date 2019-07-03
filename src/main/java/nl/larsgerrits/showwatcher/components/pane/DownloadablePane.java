package nl.larsgerrits.showwatcher.components.pane;

import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVShow;

import java.util.ArrayList;
import java.util.List;

public class DownloadablePane extends AnchorPane
{
    private final TVShow show;
    private final List<TVEpisode> downloadableEpisodes = new ArrayList<>();
    
    public DownloadablePane(TVShow show)
    {
        this.show = show;
    
        getStyleClass().addAll("anchor-pane");
    
        Text title = new Text(show.getTitle());
        title.setFont(new Font((24)));
        
        setTopAnchor(title, 10D);
        setLeftAnchor(title, 10D);
        
        getChildren().addAll(title);
    }
    
    public void addEpisode(TVEpisode episode)
    {
        downloadableEpisodes.add(episode);
        
    }
}
