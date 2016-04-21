package net.wtako.thoughts.data;

public class SearchQuery {
    String query;
    int page;

    public SearchQuery(String query, int page) {
        this.query = query;
        this.page = page;
    }
}
