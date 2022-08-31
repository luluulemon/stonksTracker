package com.visa.ssf.stonksTracker.Model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote implements Serializable{
    
    private String userName;
    public String getUserName() {   return userName;    }
    public void setUserName(String userName) {  this.userName = userName;   }

    private String ticker;
    private String description;

    public String getSymbol() { return ticker;  }
    public void setSymbol(String ticker) {  this.ticker = ticker;   }

    private Float lastPrice;
    private Float openPrice;
    private Float highPrice;
    private Float lowPrice;
    private Float netChange;
    private Float totalVolume;
    public Float peRatio;

    @JsonProperty("52WkHigh")
    public Float yearHigh;

    @JsonProperty("52WkLow")
    public Float yearLow;


    public Float getLastPrice() {
        return lastPrice;
    }
    public void setLastPrice(Float lastPrice) {
        this.lastPrice = lastPrice;
    }
    public Float getOpenPrice() {
        return openPrice;
    }
    public void setOpenPrice(Float openPrice) {
        this.openPrice = openPrice;
    }
    public Float getHighPrice() {
        return highPrice;
    }
    public void setHighPrice(Float highPrice) {
        this.highPrice = highPrice;
    }
    public Float getLowPrice() {
        return lowPrice;
    }
    public void setLowPrice(Float lowPrice) {
        this.lowPrice = lowPrice;
    }
    public Float getClosePrice() {
        return closePrice;
    }
    public void setClosePrice(Float closePrice) {
        this.closePrice = closePrice;
    }
    public Float getNetChange() {
        return netChange;
    }
    public void setNetChange(Float netChange) {
        this.netChange = netChange;
    }
    public Float getTotalVolume() {
        return totalVolume;
    }
    public void setTotalVolume(Float totalVolume) {
        this.totalVolume = totalVolume;
    }
    public Float getYearHigh() {
        return yearHigh;
    }
    public void setYearHigh(Float yearHigh) {
        this.yearHigh = yearHigh;
    }
    public Float getYearLow() {
        return yearLow;
    }
    public void setYearLow(Float yearLow) {
        this.yearLow = yearLow;
    }
    public Float getPeRatio() {
        return peRatio;
    }
    public void setPeRatio(Float peRatio) {
        this.peRatio = peRatio;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    // some properties for portfolio
    private int quantity;       
    private Date entryDate;
    private Float entryPrice;
    private Date closeDate;
    private Float closePrice;
    private Float PnL;
    private String comments;
    private Boolean toClose=false;   

    public Boolean getToClose() {   return toClose; }
    public void setToClose(Boolean toClose) {   this.toClose = toClose; }

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


}


/*
{
  "AAPL": {
    "assetType": "EQUITY",
    "assetMainType": "EQUITY",
    "cusip": "037833100",
    "assetSubType": "",
    "symbol": "AAPL",
    "description": "Apple Inc. - Common Stock",
    "bidPrice": 173.16,
    "bidSize": 1800,
    "bidId": "P",
    "askPrice": 173.18,
    "askSize": 100,
    "askId": "Q",
    "lastPrice": 173.16,
    "lastSize": 0,
    "lastId": "D",
    "openPrice": 171.52,
    "highPrice": 173.39,
    "lowPrice": 171.345,
    "bidTick": " ",
    "closePrice": 172.1,
    "netChange": 1.06,
    "totalVolume": 54091694,
    "quoteTimeInLong": 1660607995868,
    "tradeTimeInLong": 1660607999836,
    "mark": 173.19,
    "exchange": "q",
    "exchangeName": "NASD",
    "marginable": true,
    "shortable": true,
    "volatility": 0.0156,
    "digits": 4,
    "52WkHigh": 182.94,
    "52WkLow": 129.04,
    "nAV": 0,
    "peRatio": 28.4371,
    "divAmount": 0.92,
    "divYield": 0.53,
    "divDate": "2022-08-05 00:00:00.000",
    "securityStatus": "Normal",
    "regularMarketLastPrice": 173.19,
    "regularMarketLastSize": 40654,
    "regularMarketNetChange": 1.09,
    "regularMarketTradeTimeInLong": 1660593600260,
    "netPercentChangeInDouble": 0.6159,
    "markChangeInDouble": 1.09,
    "markPercentChangeInDouble": 0.6333,
    "regularMarketPercentChangeInDouble": 0.6334,
    "delayed": true,
    "realtimeEntitled": false
  },
  "BABA": {
    "assetType": "EQUITY",
    "assetMainType": "EQUITY",
    "cusip": "01609W102",
    "assetSubType": "ADR",
    "symbol": "BABA",
    "description": "Alibaba Group Holding Limited American Depositary Shares each representing eight",
    "bidPrice": 94.15,
    "bidSize": 1100,
    "bidId": "P",
    "askPrice": 94.45,
    "askSize": 500,
    "askId": "P",
    "lastPrice": 94.2,
    "lastSize": 100,
    "lastId": "N",
    "openPrice": 93.2,
    "highPrice": 94.63,
    "lowPrice": 92.38,
    "bidTick": " ",
    "closePrice": 94.77,
    "netChange": -0.57,
    "totalVolume": 14425111,
    "quoteTimeInLong": 1660608000105,
    "tradeTimeInLong": 1660607984081,
    "mark": 94.2,
    "exchange": "n",
    "exchangeName": "NYSE",
    "marginable": true,
    "shortable": true,
    "volatility": 0.0283,
    "digits": 2,
    "52WkHigh": 192.98,
    "52WkLow": 73.28,
    "nAV": 0,
    "peRatio": 43.6259,
    "divAmount": 0,
    "divYield": 0,
    "divDate": "",
    "securityStatus": "Normal",
    "regularMarketLastPrice": 94.2,
    "regularMarketLastSize": 4109,
    "regularMarketNetChange": -0.57,
    "regularMarketTradeTimeInLong": 1660604400003,
    "netPercentChangeInDouble": -0.6015,
    "markChangeInDouble": -0.57,
    "markPercentChangeInDouble": -0.6015,
    "regularMarketPercentChangeInDouble": -0.6015,
    "delayed": true,
    "realtimeEntitled": false
  }
}
 */