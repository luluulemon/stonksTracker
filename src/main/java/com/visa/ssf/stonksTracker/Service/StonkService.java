package com.visa.ssf.stonksTracker.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visa.ssf.stonksTracker.Model.Mover;
import com.visa.ssf.stonksTracker.Model.Profile;
import com.visa.ssf.stonksTracker.Model.Quote;

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

    public Optional<List<Mover>> getTopMovers(String direction, String indices){
        logger.info("in service check indices " + indices);
        String endPoint1="https://api.tdameritrade.com/v1/marketdata/" + indices + "/movers";

        String url = UriComponentsBuilder.fromUriString(endPoint1)
            .queryParam("direction", direction)
            .queryParam("change", "percent")
            .queryParam("apikey", ApiKey).toUriString(); 

        logger.info("Check url built >>>>> " + url);
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.getForEntity(url, String.class);
        
        // check for null return
        ObjectMapper mapper = new ObjectMapper();
        try
        {   List<Mover> MoversList = mapper.readValue(resp.getBody(), 
                    new TypeReference<List<Mover>>(){});
            return Optional.of(MoversList);
        }
        catch(JsonMappingException e){  logger.info(e.getMessage());    }
        catch(JsonProcessingException e){  logger.info(e.getMessage());    }    

        return Optional.empty();
    }


    public Optional<Quote> getQuote(String ticker){
        String endPoint1 = "https://api.tdameritrade.com/v1/marketdata/quotes";
        String url = UriComponentsBuilder.fromUriString(endPoint1)
        .queryParam("symbol", ticker)
        .queryParam("apikey", ApiKey).toUriString(); 

        logger.info("Check url built >>>>> " + url);
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.getForEntity(url, String.class);
        
        try (InputStream is = new ByteArrayInputStream(resp.getBody().getBytes())) 
        {
            JsonReader reader = Json.createReader(is);
            JsonObject data = reader.readObject();
            JsonObject quotedata = data.getJsonObject(ticker);
            if(quotedata == null)
                {   return Optional.empty();    }
            //logger.info("Check data " + quotedata.toString());
            if(quotedata!= null)
            {   ObjectMapper mapper = new ObjectMapper();
                Quote quote = mapper.readValue(quotedata.toString(), Quote.class);
                return Optional.of(quote);  }
        }
        catch(IOException e)
        {   logger.info("IOError in service getQuote >>>>>"  + e.getMessage());       }           

        return Optional.empty();
    }


    public Optional<List<Quote>> getQuotesss(List<String> tickers){     // for multiple tickers
        String endPoint1 = "https://api.tdameritrade.com/v1/marketdata/quotes";
        
        // Need to string tickers together
        StringBuilder sb = new StringBuilder();
        for(String ticker: tickers){
            sb.append(ticker);
            sb.append(",");
        }
        String allTickers = sb.toString();

        String url = UriComponentsBuilder.fromUriString(endPoint1)
        .queryParam("symbol", allTickers.substring(0, allTickers.length()-1))
        .queryParam("apikey", ApiKey).toUriString(); 

        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.getForEntity(url, String.class);
        
        List<Quote> quotes = new LinkedList<>();    // used to fill up with Quote objs
        try (InputStream is = new ByteArrayInputStream(resp.getBody().getBytes())) 
        {
            JsonReader reader = Json.createReader(is);
            JsonObject data = reader.readObject();
            for(String ticker:tickers){
                JsonObject quotedata = data.getJsonObject(ticker);
                    if(quotedata == null)   // if endpoint does not work
                    {   return Optional.empty();    }

                ObjectMapper mapper = new ObjectMapper();
                Quote quote = mapper.readValue(quotedata.toString(), Quote.class);
                quotes.add(quote);
            }   
            return Optional.of(quotes);
        }
        catch(IOException e)
        {   logger.info("IOError in service getQuote >>>>>"  + e.getMessage());       } 
          
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
            return Optional.of(quotedata.toString());
            }
        catch(IOException e)
        {   logger.info("Error in service getQuote >>>>>"  + e.getMessage());       }           

        return null;
    }
    
    String finnhubApiKey = System.getenv("FINNHUB_API_KEY");
        // https://api.tdameritrade.com/v1/instruments

    public Optional<Profile> getProfile(String ticker){
        String endPoint1 = "https://finnhub.io/api/v1/stock/profile2";

        String url = UriComponentsBuilder.fromUriString(endPoint1)
        .queryParam("symbol", ticker)
        .queryParam("token", finnhubApiKey)
        .toUriString(); 

        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.getForEntity(url, String.class);

        try (InputStream is = new ByteArrayInputStream(resp.getBody().getBytes())) {
            JsonReader reader = Json.createReader(is);
            JsonObject data = reader.readObject();
            ObjectMapper mapper = new ObjectMapper();
            Profile profile = mapper.readValue(data.toString(), Profile.class);

            //logger.info("Check data " + quotedata.toString());
            return Optional.of(profile);
            }
        catch(IOException e)
        {   logger.info("Error in service getQuote >>>>>"  + e.getMessage());       } 

        return Optional.empty();
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
