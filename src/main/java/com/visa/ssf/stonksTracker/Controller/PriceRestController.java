package com.visa.ssf.stonksTracker.Controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.visa.ssf.stonksTracker.Model.Portfolio;
import com.visa.ssf.stonksTracker.Model.Quote;
import com.visa.ssf.stonksTracker.Model.Watchlist;
import com.visa.ssf.stonksTracker.Service.RedisService;
import com.visa.ssf.stonksTracker.Service.StonkService;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;



@RestController
public class PriceRestController {
    private static final Logger logger = LoggerFactory.getLogger(PriceRestController.class);

    @Autowired
    StonkService stonkservice;

    @Autowired
    RedisService redisService;

    @GetMapping(path="prices/{ticker}", produces="application/json")
    public ResponseEntity<String> getDailies(@PathVariable String ticker)
    {
        String respBody = stonkservice.pastMonthDailies(ticker);

        return ResponseEntity.ok(respBody);
    }


    @GetMapping(path="/getPortfolio", produces="application/json")
    public ResponseEntity<String> getPortfolio(@RequestParam String user){
        Portfolio p = redisService.checkPortfolio(user);
        if (p==null){
            JsonObject errJson = Json.createObjectBuilder().add("error", "User not available").build();
            return ResponseEntity.status(400).body(errJson.toString()); 
        }

        // Build the Json body
        JsonArrayBuilder portfolioBuilder = Json.createArrayBuilder();
        if(p.getPortfolio() != null){  
            for (Quote q:p.getPortfolio()){
                portfolioBuilder.add( Json.createObjectBuilder().add("symbol", q.getSymbol())
                                            .add("description", q.getDescription())
                                            .add("Entry Date", q.getEntryDate().toString())
                                            .add("Entry Price", q.getEntryPrice())
                                            .add("Quantity", q.getQuantity())
                                            .add("Comments", q.getComments())
                                            .build());
            }
        }

        JsonArrayBuilder transactionsBuilder = Json.createArrayBuilder();
        Float pastTradePnL = 0f;
        if(p.getPastTransactions() != null){
            pastTradePnL = p.getPastTradePnL();
            for (Quote q:p.getPastTransactions()){
                transactionsBuilder.add( Json.createObjectBuilder().add("symbol", q.getSymbol())
                                            .add("description", q.getDescription())
                                            .add("Entry Date", q.getEntryDate().toString())
                                            .add("Entry Price", q.getEntryPrice())
                                            .add("Quantity", q.getQuantity())
                                            .add("Close Date", q.getCloseDate().toString())
                                            .add("Close Price", q.getClosePrice())
                                            .add("PnL", q.getPnL())
                                            .add("Comments", q.getComments())
                                            .build());
            }
        }

        JsonObject portJson = Json.createObjectBuilder().add("username", user)
                                .add("Portfolio", portfolioBuilder.build())
                                .add("PastTransactionsPnL", pastTradePnL)
                                .add("PastTransactions", transactionsBuilder.build())
                                .build();

        return ResponseEntity.ok(portJson.toString());
    }


    @GetMapping(path="/getWatchlist", produces="application/json")
    public ResponseEntity<String> getMovers(@RequestParam String user){
        
        Watchlist w = redisService.checkWatchList(user);
        if (w==null){
            JsonObject errJson = Json.createObjectBuilder().add("error", "User not available").build();
            return ResponseEntity.status(400).body(errJson.toString()); 
        }

        String watchlistString;
        if(w.getWatchList() == null){   watchlistString = "";   }
        else{   watchlistString = w.getWatchList().toString();  }

        JsonObject watchlistJson = Json.createObjectBuilder().add("username", user)
            .add("Watchlist", watchlistString )
            .build();

        return ResponseEntity.ok(watchlistJson.toString());
    }

    
}
