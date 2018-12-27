package nl.larsgerrits.showwatcher.components.collections;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import nl.larsgerrits.showwatcher.Main;
import nl.larsgerrits.showwatcher.components.poster.PosterEpisode;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVEpisodeCollection;

import java.util.List;
import java.util.stream.Collectors;

public class PaneCollection extends AnchorPane
{
    public PaneCollection(TVEpisodeCollection collection)
    {
        setMaxWidth(Main.WIDTH);
        
        Text text = new Text(collection.getTitle());
        text.setFont(new Font(20));
        text.setId("text");
        setLeftAnchor(text, 10D);
        setTopAnchor(text, 10D);
        
        HBox box = new HBox();
        {
            List<TVEpisode> unwatched = collection.getEpisodes().stream().filter(e -> !e.getWatched().get()).collect(Collectors.toList());
            int count = 0;
            for (TVEpisode episode : unwatched)
            {
                box.getChildren().add(new PosterEpisode(episode, null));
                if(++count > 7) break;
            }
        }
        box.setMaxWidth(Main.WIDTH);
        setLeftAnchor(box, 10D);
        setTopAnchor(box, 40D);
        
        getChildren().addAll(text,box);
    }
}
