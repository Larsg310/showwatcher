package nl.larsgerrits.showwatcher.show;

import java.util.List;

public class TVEpisodeCollection
{
    private String title;
    private List<TVEpisode> episodes;
    
    public TVEpisodeCollection(String title, List<TVEpisode> episodes)
    {
        this.title = title;
        this.episodes = episodes;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public List<TVEpisode> getEpisodes()
    {
        return episodes;
    }
}
