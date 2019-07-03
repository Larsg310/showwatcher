package nl.larsgerrits.showwatcher.api_impl.torrent.piratebay;

import nl.larsgerrits.showwatcher.download.Torrent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import static nl.larsgerrits.showwatcher.api_impl.torrent.ettv.ETTVApi.TRACKERS;

public class PirateBayScraper
{
    
    public static ArrayList<Torrent> search(Query query, int season, int episode)
    {
        ArrayList<Torrent> result = new ArrayList<>();
        
        try
        {
            Jsoup.connect(Constants.PIRATE_BAY_URL);
            
            Document doc = Jsoup.connect(query.TranslateToUrl()).userAgent(Constants.USER_AGENT).timeout(5000).get();
            
            // get all table rows
            Elements tableRows = doc.getElementsByTag("tr");
            
            for (Element element : tableRows)
            {
                if (!element.hasClass("header"))
                {
                    Element td2 = element.children().select("td").get(1);
                    Element torrentName = td2.children().select("a").first();
                    
                    String name = torrentName.text();
                    
                    Element torrentMagnet = td2.children().select("a").get(1);
                    String magnetUrl = torrentMagnet.attr("href")  + "&tr=" + TRACKERS;
                    
                    Element details = td2.select("font").first();
                    String torrentInfo = details.text();
                    String[] splitInfo = torrentInfo.split(",");
                    //                    String size = splitInfo[1].substring(6); (e.g. 2.16GiB)
                    
                    Element td3 = element.children().select("td").get(2);
                    int seeds = Integer.parseInt(td3.text());
                    
                    Element td4 = element.children().select("td").get(3);
                    int peers = Integer.parseInt(td4.text());
                    
                    result.add(new Torrent(name, magnetUrl, season, episode, seeds, peers, 0));
                }
            }
        }
        catch (IOException ignored) { /*can't connect, ignore*/}
        
        return result;
    }
}
