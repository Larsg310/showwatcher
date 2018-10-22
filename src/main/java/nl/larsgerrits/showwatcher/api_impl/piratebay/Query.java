package nl.larsgerrits.showwatcher.api_impl.piratebay;

public class Query {

    public int Page;
    public String Term;
    public boolean Mode48h;
    
    // constructors
    // used for the 48hr and top100 categories
    public Query(String term) {
    	Term = term;
    	Mode48h = true;
    }
    
    public Query(int category) {
    	Term = "";
    	this.Page = 0;
    	Mode48h = false;
    }
    
    public Query(String term, int page) {
    	Term = term;
    	Page = page;
    	Mode48h = false;
    }
    
    public String TranslateToUrl() {
    	String url = new String();
    	
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
