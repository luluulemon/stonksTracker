package com.visa.ssf.stonksTracker.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.visa.ssf.stonksTracker.Service.StonkService;



@RestController
public class PriceRestController {
    
    @Autowired
    StonkService stonkservice;

    @GetMapping(path="prices/{ticker}", produces="application/json")
    public ResponseEntity<String> getDailies(@PathVariable String ticker)
    {
        String respBody = stonkservice.pastMonthDailies(ticker);

        return ResponseEntity.ok(respBody);
    }


    // @GetMapping(path="/movers", produces="application/json")
    // public ResponseEntity<String> getMovers(){
    //     String respBody = stonkservice.getTopMovers();
    //     return ResponseEntity.ok(respBody);
    // }

    
}
