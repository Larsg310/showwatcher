package nl.larsgerrits.showwatcher.data;

public class EpisodeData
{
    private int episode;
    private String title;
    private String file_name;
    private long releaseDate;
    private boolean watched;
    
    public EpisodeData() {}
    
    public EpisodeData(int episodeNumber, String title, String file_name, long releaseDate, boolean watched)
    {
        this.episode = episodeNumber;
        this.title = title;
        this.file_name = file_name;
        this.releaseDate = releaseDate;
        this.watched = watched;
    }
    
    public void setFileName(String file_name)
    {
        this.file_name = file_name;
    }
    
    public int getEpisode()
    {
        return episode;
    }
    
    public String getFileName()
    {
        return file_name;
    }
    
    public long getReleaseDate()
    {
        return releaseDate;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public boolean isWatched()
    {
        return watched;
    }
}
