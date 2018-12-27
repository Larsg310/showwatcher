package nl.larsgerrits.showwatcher.api_impl.info.trakt;

public class TraktShow
{
    private String overview;
    
    public TraktShow(String overview)
    {
        this.overview = overview;
    }
    
    public String getOverview()
    {
        return overview;
    }
}
