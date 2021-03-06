package nl.larsgerrits.showwatcher.api_impl.torrent.kat;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * This class represents a search for torrents. Use the static methods to create
 * new searches.
 */
public class Search extends ArrayList<Torrent>
{
    private static final String BASE_URL = "https://kickasstorrent.cr/";
    private static final String SEARCH_URL = BASE_URL + "/usearch/";
    private static final String ORDER_SIZE_DESC = "/?field=size&sorder=desc";
    private static final String ORDER_SIZE_ASC = "/?field=size&sorder=asc";
    private static final String ORDER_AGE_DESC = "/?field=time_add&sorder=desc"; //newest first
    private static final String ORDER_AGE_ASC = "/?field=time_add&sorder=asc"; //Oldest first
    private static final String ORDER_SEEDS_DESC = "/?field=seeders&sorder=desc";
    private static final String ORDER_SEEDS_ASC = "/?field=seeders&sorder=asc";
    private static final String ORDER_LEECH_DESC = "/?field=leechers&sorder=desc";
    private static final String ORDER_LEECH_ASC = "/?field=leechers&sorder=asc";
    
    private static final String TORRENT_DOWNLOAD = "[title=Download torrent file]";
    private static final String MAGNET_LINK = "[title=Magnet link]";
    
    private static int timeout = 1000;
    private final int pageCount;
    private String searchURL;
    private int currentPage;
    
    private Search()
    {
        super();
        searchURL = "";
        currentPage = 0;
        pageCount = 0;
    }
    
    private Search(String searchURL, int pageCount)
    {
        super();
        this.searchURL = searchURL;
        this.currentPage = 1;
        this.pageCount = pageCount;
    }
    
    /**
     * Gets the next 25 results of the search.
     *
     * @return Empty search if no more results exist
     *
     * @throws IOException
     */
    public Search next() throws IOException
    {
        if (currentPage < pageCount)
        {
            this.currentPage++;
            this.formatPageURL();
            return Search.runSearch(new URL(this.searchURL));
        }
        else
        {
            return this;
        }
    }
    
    private String formatPageURL()
    {
        String[] spl = this.searchURL.split("/");
        if (this.searchURL.contains("?"))
        {
            if (spl.length == 7)
            {
                spl[spl.length - 2] = "" + this.currentPage;
            }
            else if (spl.length == 6)
            {
                spl[spl.length - 1] = this.currentPage + "/" + spl[spl.length - 1];
            }
        }
        else
        {
            if (spl.length == 6)
            {
                spl[spl.length - 1] = "" + this.currentPage;
            }
            else if (spl.length == 5)
            {
                this.searchURL += this.currentPage;
                return this.searchURL;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String s : spl)
        {
            sb.append(s);
            sb.append("/");
        }
        this.searchURL = sb.toString();
        return this.searchURL;
    }
    
    private static Search runSearch(URL url) throws IOException
    {
        Document doc;
        try
        {
            System.out.println(url);
            doc = Jsoup.parse(url, timeout);
        }
        catch (HttpStatusException | SocketTimeoutException ex)
        {
            //404 error means no torrents found in search, so we return an empty list
            return new Search();
        }
        Elements pages = doc.select("a.turnoverButton.siteButton.bigButton");
        int maxPages = 0;
        if (!pages.isEmpty())
        {
            maxPages = 1;/*Integer.parseInt(pages.get(pages.size() - 1).text());*/
        }
        
        Search search = new Search(url.toString(), maxPages);
        
        Elements torrents = doc.select("tr").not(".firstr");
        
        //First one in list repeats, so we skip it
        if (torrents.size() > 1)
        {
            try
            {
                for (Element torrent : torrents.subList(1, torrents.size()))
                {
                    Elements download = torrent.select(TORRENT_DOWNLOAD);
                    if (download != null && !download.isEmpty())
                    {
                        String urlDownload = download.get(0).getAllElements().get(0).attr("href");
                        
                        // Elements links = torrent.select(MAGNET_LINK);
                        // // Elements allElements = links.get(0).getAllElements();
                        // String magnet = links.attr("href");
                        // Pattern p = Pattern.compile("http:.*");
                        // Matcher m = p.matcher(urlDownload);
                        // if (!m.find())
                        // {
                        //     urlDownload = "http:" + urlDownload;
                        // }
                        // URL download = new URL(urlDownload);
                        String html = BASE_URL + "/" + urlDownload.split("/")[2].replaceAll(".html", "");
                        
                        Document document = Jsoup.parse(new URL(html), timeout);
                        // document.select(MAGNET_LINK);
                        // Elements a = document.select("a");
                        String magnet = document.select(MAGNET_LINK).get(0).attr("href");
                        
                        String title = torrent.select("a.cellMainLink").get(0).text();
                        
                        //TODO: Check if verified
                        //Select info table cells
                        Elements info = torrent.select("td.center");
                        long size = Torrent.parseSize(info.get(0).text());
                        long age = Torrent.parseAge(info.get(2).text());
                        int seeds = Integer.parseInt(info.get(3).text());
                        int leech = Integer.parseInt(info.get(4).text());
                        
                        // String category = torrent.select("span[id]").get(0).text();
                        
                        search.add(new Torrent(null, title, "", age, seeds, leech, size, magnet));
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return search;
    }
    
    /**
     * Gets a page from the results
     *
     * @param pageNumber The wanted result page
     *
     * @return up to 25 results if page exists, empty otherwise
     *
     * @throws IOException
     */
    public Search page(int pageNumber) throws IOException
    {
        if (currentPage < pageCount && pageNumber > 0)
        {
            this.currentPage = pageNumber;
            this.formatPageURL();
            return Search.runSearch(new URL(this.searchURL));
        }
        else
        {
            return this;
        }
    }
    
    public String getSearchURL()
    {
        return searchURL;
    }
    
    /**
     * Creates a new search
     *
     * @param query String to search
     *
     * @return Up to 25 torrents, or empty if search yields no results
     *
     * @throws IOException
     */
    public static Search newSearch(String query) throws IOException
    {
        String urlStr = SEARCH_URL + URLEncoder.encode(query, "UTF-8");
        URL url = new URL(urlStr);
        
        return Search.runSearch(url);
    }
    
    /**
     * Creates a new search
     *
     * @param query String to search
     * @param sort  {@link SortOption} used to sort the results
     *
     * @return Up to 25 torrents, or empty if search yields no results
     *
     * @throws IOException
     */
    public static Search newSearch(String query, SortOption sort) throws IOException
    {
        String urlStr = SEARCH_URL + URLEncoder.
                                                   encode(query + getSortQuery(sort), "UTF-8");
        URL url = new URL(urlStr);
        return Search.runSearch(url);
    }
    
    private static String getSortQuery(SortOption sort)
    {
        String sortQuery = "";
        switch (sort)
        {
            case SMALLEST_FIRST:
                sortQuery = ORDER_SIZE_ASC;
                break;
            case LARGEST_FIRST:
                sortQuery = ORDER_SIZE_DESC;
                break;
            case OLDEST_FIRST:
                sortQuery = ORDER_AGE_ASC;
                break;
            case YOUNGEST_FIRST:
                sortQuery = ORDER_AGE_DESC;
                break;
            case MOST_SEEDERS:
                sortQuery = ORDER_SEEDS_DESC;
                break;
            case LEAST_SEEDERS:
                sortQuery = ORDER_SEEDS_ASC;
                break;
            case MOST_LEECHES:
                sortQuery = ORDER_LEECH_DESC;
                break;
            case LEAST_LEECHES:
                sortQuery = ORDER_LEECH_ASC;
                break;
        }
        return sortQuery;
    }
    
    /**
     * Sets the timeout used by the parsing methods. Default value is 1000.
     *
     * @param t Time in milliseconds
     */
    public static void setTimeout(int t)
    {
        timeout = t;
    }
    
    /**
     * This documents available sorting methods for a search.
     */
    public enum SortOption
    {
        
        SMALLEST_FIRST,
        LARGEST_FIRST,
        OLDEST_FIRST,
        YOUNGEST_FIRST,
        MOST_SEEDERS,
        LEAST_SEEDERS,
        MOST_LEECHES,
        LEAST_LEECHES
    }
    
}
