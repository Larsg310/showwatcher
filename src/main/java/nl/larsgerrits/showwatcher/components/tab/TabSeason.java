package nl.larsgerrits.showwatcher.components.tab;

import com.jfoenix.controls.JFXMasonryPane;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.components.poster.PosterEpisode;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class TabSeason extends Tab
{
    private TVSeason season;
    
    private JFXMasonryPane masonryPane;
    private Consumer<TVEpisode> episodeSelectedConsumer;
    
    public TabSeason(TVSeason season, Consumer<TVEpisode> episodeSelectedConsumer)
    {
        this.season = season;
        this.episodeSelectedConsumer = episodeSelectedConsumer;
        season.setEpisodeAdded(this::onEpisodeAdded);
        
        setClosable(true);
        setText("Season " + season.getSeasonNumber());
        
        ScrollPane pane = new ScrollPane();
        pane.setFitToWidth(true);
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        masonryPane = new JFXMasonryPane();
        masonryPane.setHSpacing(10);
        masonryPane.setVSpacing(10);
        masonryPane.setCellWidth(210);
        masonryPane.setCellHeight(160);
        masonryPane.setPrefWidth(1784);
        masonryPane.setPrefHeight(750);
        masonryPane.setPadding(new Insets(16D));
        
        pane.setContent(masonryPane);
        setContent(pane);
    }
    
    private void onEpisodeAdded(TVEpisode episode)
    {
        if (masonryPane.getChildren().size() > 0)
        {
            PosterEpisode poster = new PosterEpisode(episode, episodeSelectedConsumer);
            if (poster.getPrefHeight() > ((PosterEpisode) masonryPane.getChildren().get(0)).getPrefHeight())
            {
                masonryPane.getChildren().forEach(n -> ((PosterEpisode) n).setPrefHeight(poster.getPrefHeight()));
                masonryPane.setCellHeight(poster.getPrefHeight());
            }
            Platform.runLater(() -> masonryPane.getChildren().add(poster));
        }
    }
    
    public void onSelected()
    {
        if (masonryPane.getChildren().isEmpty())
        {
            if (season.getEpisodes().size() > 0)
            {
                Threading.LOADING_THREAD.execute(() -> {
                    List<PosterEpisode> posters = new ArrayList<>();

                    for (TVEpisode episode : season.getEpisodes())
                    {
                        PosterEpisode poster = new PosterEpisode(episode, episodeSelectedConsumer);
                        posters.add(poster);
                    }
                    PosterEpisode biggestPoster = Collections.max(posters, Comparator.comparing(PosterEpisode::getPrefHeight));
                    masonryPane.getChildren().forEach(n -> ((PosterEpisode) n).setPrefHeight(biggestPoster.getPrefHeight()));
                    masonryPane.setCellHeight(biggestPoster.getPrefHeight());
                    Platform.runLater(() -> masonryPane.getChildren().addAll(posters));
                });
            }
        }
    }
    
    public TVSeason getSeason()
    {
        return season;
    }
}
