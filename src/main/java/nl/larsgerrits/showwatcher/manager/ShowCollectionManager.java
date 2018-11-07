package nl.larsgerrits.showwatcher.manager;

import nl.larsgerrits.showwatcher.show.TVShowCollection;
import nl.larsgerrits.showwatcher.util.FileUtils;

import java.util.List;

public final class ShowCollectionManager
{
    private ShowCollectionManager() {}
    
    public static List<TVShowCollection> getShowCollection()
    {
        return FileUtils.loadShowCollectionsFromDisk();
    }
}
