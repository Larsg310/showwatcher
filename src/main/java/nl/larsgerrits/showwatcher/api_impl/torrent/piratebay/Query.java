package nl.larsgerrits.showwatcher.api_impl.torrent.piratebay;

public class Query {

    public int Page;
    public String Term;
    public boolean Mode48h;
    public Query(String term, int page) {
    	Term = term;
    	Page = page;
    	Mode48h = false;
    }
    
    public String TranslateToUrl() {
    	String url;
    	
    	if (!Mode48h) {
    		url = Constants.PIRATE_BAY_URL +
    			"/search/" +
    			Term + "/" +
    			Integer.toString(Page) + "/" +
    			Integer.toString(99) + "/" +
    			Integer.toString(0);
    	}
    	else {
    		url = Constants.PIRATE_BAY_URL +
    				"/top/" +
    				Term;
    	}
    	
    	return url;
    }
}
