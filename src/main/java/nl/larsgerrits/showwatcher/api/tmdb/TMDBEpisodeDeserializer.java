package nl.larsgerrits.showwatcher.api.tmdb;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TMDBEpisodeDeserializer implements JsonDeserializer<List<TMDBEpisode>>
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    @SuppressWarnings("Duplicates")
    @Override
    public List<TMDBEpisode> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        List<TMDBEpisode> seasons = new ArrayList<>();
        
        JsonObject obj = json.getAsJsonObject();
        JsonArray episodeArray = obj.get("episodes").getAsJsonArray();
        
        for (JsonElement e : episodeArray)
        {
            JsonObject episodeObj = e.getAsJsonObject();
            
            String title = episodeObj.get("name").getAsString();
            int episodeNumber = episodeObj.get("episode_number").getAsInt();
            
            Date date = new Date(0);
            
            JsonElement dateElement = episodeObj.get("air_date");
            if (!dateElement.isJsonNull())
            {
                String sDate = dateElement.getAsString();
                try
                {
                    date = DATE_FORMAT.parse(sDate);
                }
                catch (ParseException e1)
                {
                    e1.printStackTrace();
                }
            }
            
            seasons.add(new TMDBEpisode(title, episodeNumber, date));
        }
        return seasons;
    }
}
