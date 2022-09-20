package com.visa.ssf.stonksTracker.Model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Watchlist implements Serializable {
    private String username;
    private String ticker;
    private String ErrorMsg;

    private String listName;
    private List<String> watchList;
    private List<Quote> quotes;
    private String sorting;

    private Map<String, List<String>> watchlists;

    public String getSorting() {
        return sorting;
    }
    public void setSorting(String sorting) {
        this.sorting = sorting;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public Map<String, List<String>> getWatchlists() {
        return watchlists;
    }
    public void setWatchlists(Map<String, List<String>> watchlists) {
        this.watchlists = watchlists;
    }
    public String getListName() {
        return listName;
    }
    public void setListName(String listName) {
        this.listName = listName;
    }
    public List<String> getWatchList() {
        return watchList;
    }
    public void setWatchList(List<String> watchList) {
        this.watchList = watchList;
    }
    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
    public String getErrorMsg() {
        return ErrorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        ErrorMsg = errorMsg;
    }
    public List<Quote> getQuotes() {
        return quotes;
    }
    public void setQuotes(List<Quote> quotes) {
        this.quotes = quotes;
    }
}
