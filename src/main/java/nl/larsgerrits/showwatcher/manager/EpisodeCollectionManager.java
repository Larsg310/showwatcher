package nl.larsgerrits.showwatcher.manager;

import nl.larsgerrits.showwatcher.show.TVEpisodeCollection;
import nl.larsgerrits.showwatcher.util.FileUtils;

import java.util.List;

public final class EpisodeCollectionManager
{
    private EpisodeCollectionManager() {}
    
    public static List<TVEpisodeCollection> getShowCollection()
    {
        return FileUtils.loadShowCollectionsFromDisk();
    }
}
