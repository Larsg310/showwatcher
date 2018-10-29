package nl.larsgerrits.showwatcher.show;

import java.util.List;

public class ShowCollection
{
    private String title;
    private List<TVShow> shows;
    
    public ShowCollection(String title, List<TVShow> shows)
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
