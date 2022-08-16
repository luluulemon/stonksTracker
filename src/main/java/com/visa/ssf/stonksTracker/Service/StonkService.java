package com.visa.ssf.stonksTracker.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.apptastic.tickersymbol.TickerSymbolSearch;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;



@Service
public class StonkService {
    private static final Logger logger = LoggerFactory.getLogger(StonkService.class);

    private String endPoint= "https://api.tdameritrade.com/v1/marketdata/%s/pricehistory";
    private String ApiKey = System.getenv("TD_API_KEY");


    public String pastMonthDailies(String ticker){

        String endPoint1 = String.format(endPoint,ticker.toUpperCase());    // ticker need to be uppercase
        String url = UriComponentsBuilder.fromUriString(endPoint1)
                        .queryParam("periodType", "month")
                        .queryParam("frequencyType", "daily")
                        .queryParam("period",1)
                        .queryParam("apikey", ApiKey).toUriString(); 

        logger.info("Check url built >>>>> " + url);
            // default period is one month - one month of dailies
        
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.getForEntity(url, String.class);

        return resp.getBody();
        
    }

    // https://github.com/w3stling/tickersymbol
    // library for ticker symbol search

    public String getTopMovers(String direction){
        String endPoint1="https://api.tdameritrade.com/v1/marketdata/$SPX.X/movers";

        String url = UriComponentsBuilder.fromUriString(endPoint1)
            .queryParam("direction", direction)
            .queryParam("change", "percent")
            .queryParam("apikey", ApiKey).toUriString(); 

        logger.info("Check url built >>>>> " + url);
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.getForEntity(url, String.class);
    
        return resp.getBody();
    }


    public Optional<String> getQuote(String ticker){
        String endPoint1 = "https://api.tdameritrade.com/v1/marketdata/quotes";
        String url = UriComponentsBuilder.fromUriString(endPoint1)
        .queryParam("symbol", ticker)
        .queryParam("apikey", ApiKey).toUriString(); 

        logger.info("Check url built >>>>> " + url);
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.getForEntity(url, String.class);
        
        try (InputStream is = new ByteArrayInputStream(resp.getBody().getBytes())) {
            JsonReader reader = Json.createReader(is);
            JsonObject data = reader.readObject();
            JsonObject quotedata = data.getJsonObject(ticker);
            if(quotedata == null)
                {   return Optional.empty();    }
            logger.info("Check data " + quotedata.toString());
            if(quotedata!= null)
            {    return Optional.of(quotedata.toString());  }
            }
        catch(IOException e)
        {   logger.info("Error in service getQuote >>>>>"  + e.getMessage());       }           

        return Optional.empty();
    }


    public Optional<String> getQuote2(String ticker){
        String endPoint1 = "https://api.tdameritrade.com/v1/marketdata/quotes";
        String url = UriComponentsBuilder.fromUriString(endPoint1)
        .queryParam("symbol", ticker)
        .toUriString(); 

        String access_token = "Bearer " + createAccessToken().replaceAll("^\"|\"$", "");
        logger.info("Check url built >>>>> " + access_token);

        RequestEntity req = RequestEntity.get(url)
                    .header("Authorization", access_token).build();
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.exchange(req, String.class);
        
        try (InputStream is = new ByteArrayInputStream(resp.getBody().getBytes())) {
            JsonReader reader = Json.createReader(is);
            JsonObject data = reader.readObject();
            JsonObject quotedata = data.getJsonObject(ticker);
            logger.info("Check data " + quotedata.toString());
            return Optional.of(quotedata.toString());
            }
        catch(IOException e)
        {   logger.info("Error in service getQuote >>>>>"  + e.getMessage());       }           

        return null;
    }


    private String refresh_token= System.getenv("TD_REFRESH_TOKEN");

    public String createAccessToken(){
        String endPoint = "https://api.tdameritrade.com/v1/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refresh_token);
        form.add("client_id", ApiKey);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.exchange(endPoint, 
                                HttpMethod.POST,
                                entity, String.class);
        logger.info("Check respbody >>>>> " + resp.getBody());

        try (InputStream is = new ByteArrayInputStream(resp.getBody().getBytes())) {
            JsonReader reader = Json.createReader(is);
            JsonObject data = reader.readObject();
            String access_token = data.get("access_token").toString();
            logger.info("Check token " + access_token);
            return access_token;
            }
        catch(IOException e)
        {   logger.info("Error in service getQuote >>>>>"  + e.getMessage());       }
            
        return resp.getBody();
    }
}
