package nl.larsgerrits.showwatcher.show;

import java.util.List;

public class TVShowCollection
{
    private String title;
    private List<TVShow> shows;
    
    public TVShowCollection(String title, List<TVShow> shows)
    {
        this.title = title;
        this.shows = shows;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public List<TVShow> getShows()
    {
        return shows;
    }
}
