package nl.larsgerrits.showwatcher.data;

import java.util.Date;
import java.util.List;

public class SeasonData
{
    private String title = "";
    private String imdbId = "";
    private int season;
    private int totalEpisodes;
    private long releaseDate;
    private List<EpisodeData> episodes;
    
    public SeasonData()
    {
    }
    
    public SeasonData(String title, String imdbId, int season, int totalEpisodes, long releaseDate, List<EpisodeData> episodes)
    {
        this.title = title;
        this.imdbId = imdbId;
        this.season = season;
        this.totalEpisodes = totalEpisodes;
        this.releaseDate = releaseDate;
        this.episodes = episodes;
    }
    
    public List<EpisodeData> getEpisodeData()
    {
        return episodes;
    }
    
    public String getImdbId()
    {
        return imdbId;
    }
    
    public Date getReleaseDate()
    {
        return new Date(releaseDate);
    }
    
    public int getSeason()
    {
        return season;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public int getTotalEpisodes()
    {
        return totalEpisodes;
    }
    
    @Override
    public String toString()
    {
        return "SeasonData{" + "title='" + title + '\'' + ", imdbId='" + imdbId + '\'' + ", season=" + season + ", totalEpisodes=" + totalEpisodes + ", releaseDate=" + releaseDate + ", episodes=" + episodes + '}';
    }
}
