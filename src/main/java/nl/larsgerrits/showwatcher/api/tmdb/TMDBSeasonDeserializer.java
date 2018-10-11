package nl.larsgerrits.showwatcher.api.tmdb;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TMDBSeasonDeserializer implements JsonDeserializer<List<TMDBSeason>>
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    @SuppressWarnings("Duplicates")
    @Override
    public List<TMDBSeason> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        List<TMDBSeason> seasons = new ArrayList<>();
        
        JsonObject obj = json.getAsJsonObject();
        JsonArray seasonArray = obj.get("seasons").getAsJsonArray();
        // System.out.println(seasonArray);
        
        for (JsonElement e : seasonArray)
        {
            JsonObject seasonObj = e.getAsJsonObject();
            
            int seasonNumber = seasonObj.get("season_number").getAsInt();
            int episodeCount = seasonObj.get("episode_count").getAsInt();
            
            Date date = new Date(0);
            
            JsonElement dateElement = seasonObj.get("air_date");
            if (!dateElement.isJsonNull())
            {
                String sDate = dateElement.getAsString();
                try
                {
                    date = DATE_FORMAT.parse(sDate);
                }
                catch (ParseException | NumberFormatException ignored) { }
            }
            
            seasons.add(new TMDBSeason(seasonNumber, episodeCount, date));
        }
        
        return seasons;
    }
}
