package nl.larsgerrits.showwatcher.manager;

import nl.larsgerrits.showwatcher.show.ShowCollection;
import nl.larsgerrits.showwatcher.util.FileUtils;

import java.util.List;

public final class ShowCollectionManager
{
    private ShowCollectionManager() {}
    
    public static List<ShowCollection> getShowCollection()
    {
        return FileUtils.loadShowCollectionsFromDisk();
    }
}
