package com.visa.ssf.stonksTracker.Model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

public class Portfolio implements Serializable {
    private String ticker;
    private String description;
    private Float lastPrice;
    private int quantity;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date entryDate;
    private Float entryPrice;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date closeDate;
    private Float closePrice;
    private Float PnL;
    private Float pastTradePnL;
    private String comments;
    private String username;
    private String errorMsg;
    private List<Quote> portfolio;
    private List<Quote> pastTransactions;
    private int editIndex = 88888;   // index show edit row in the midst of edit PORTFOLIO
                                    // shows 100000+closeIndex in the midst of close PORTFOLIO

    public int getEditIndex() {
        return editIndex;
    }
    public void setEditIndex(int editIndex) {
        this.editIndex = editIndex;
    }

    public Float getPastTradePnL() {
        return pastTradePnL;
    }
    public void setPastTradePnL(Float pastTradePnL) {
        this.pastTradePnL = pastTradePnL;
    }

    public List<Quote> getPastTransactions() {
        return pastTransactions;
    }
    public void setPastTransactions(List<Quote> pastTransactions) {
        this.pastTransactions = pastTransactions;
    }
    public Float getClosePrice() {
        return closePrice;
    }
    public void setClosePrice(Float closePrice) {
        this.closePrice = closePrice;
    }
    
    public List<Quote> getPortfolio() {
        return portfolio;
    }
    public void setPortfolio(List<Quote> portfolio) {
        this.portfolio = portfolio;
    }
    public List<Quote> getHistory() {
        return history;
    }
    public void setHistory(List<Quote> history) {
        this.history = history;
    }
    private List<Quote> history;

    public String getErrorMsg() {
        return errorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Float getLastPrice() {
        return lastPrice;
    }
    public void setLastPrice(Float lastPrice) {
        this.lastPrice = lastPrice;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public Date getEntryDate() {
        return entryDate;
    }
    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
    }
    public Float getEntryPrice() {
        return entryPrice;
    }
    public void setEntryPrice(Float entryPrice) {
        this.entryPrice = entryPrice;
    }
    public Date getCloseDate() {
        return closeDate;
    }
    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }
    public Float getPnL() {
        return PnL;
    }
    public void setPnL(Float pnL) {
        PnL = pnL;
    }
    public String getComments() {
        return comments;
    }
    public void setComments(String comments) {
        this.comments = comments;
    }

    private int eQuantity;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date eEntryDate;
    private Float eEntryPrice;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date eCloseDate;
    private Float eClosePrice;
    private String eComments;

    public int geteQuantity() {
        return eQuantity;
    }
    public void seteQuantity(int eQuantity) {
        this.eQuantity = eQuantity;
    }
    public Date geteEntryDate() {
        return eEntryDate;
    }
    public void seteEntryDate(Date eEntryDate) {
        this.eEntryDate = eEntryDate;
    }
    public Float geteEntryPrice() {
        return eEntryPrice;
    }
    public void seteEntryPrice(Float eEntryPrice) {
        this.eEntryPrice = eEntryPrice;
    }
    public Date geteCloseDate() {
        return eCloseDate;
    }
    public void seteCloseDate(Date eCloseDate) {
        this.eCloseDate = eCloseDate;
    }
    public Float geteClosePrice() {
        return eClosePrice;
    }
    public void seteClosePrice(Float eClosePrice) {
        this.eClosePrice = eClosePrice;
    }
    public String geteComments() {
        return eComments;
    }
    public void seteComments(String eComments) {
        this.eComments = eComments;
    }
}
