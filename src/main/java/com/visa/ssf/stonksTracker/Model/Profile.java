package com.visa.ssf.stonksTracker.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile {
    @JsonProperty("finnhubIndustry")
    private String industry;
    @JsonProperty("ipo")
    private String ipoDate;
    private Float marketCapitalization; // million
    private String logo;
    private String weburl;
    private Float shareOutstanding;


    public Float getShareOutstanding() {
        return shareOutstanding;
    }
    public void setShareOutstanding(Float shareOutstanding) {
        this.shareOutstanding = shareOutstanding;
    }
    public String getIndustry() {
        return industry;
    }
    public void setIndustry(String industry) {
        this.industry = industry;
    }
    public String getIpoDate() {
        return ipoDate;
    }
    public void setIpoDate(String ipoDate) {
        this.ipoDate = ipoDate;
    }
    public Float getMarketCapitalization() {
        return marketCapitalization;
    }
    public void setMarketCapitalization(Float marketCapitalization) {
        this.marketCapitalization = marketCapitalization;
    }
    public String getLogo() {
        return logo;
    }
    public void setLogo(String logo) {
        this.logo = logo;
    }
    public String getWeburl() {
        return weburl;
    }
    public void setWeburl(String weburl) {
        this.weburl = weburl;
    }

}



/*
{
    "country": "US",
    "currency": "USD",
    "exchange": "NASDAQ NMS - GLOBAL MARKET",
    "finnhubIndustry": "Technology",
    "ipo": "1980-12-12",
    "logo": "https://static.finnhub.io/logo/87cb30d8-80df-11ea-8951-00000000092a.png",
    "marketCapitalization": 2692975.971865912,
    "name": "Apple Inc",
    "phone": "14089961010.0",
    "shareOutstanding": 16070.8,
    "ticker": "AAPL",
    "weburl": "https://www.apple.com/"
}
 */