package com.visa.ssf.stonksTracker.Controller;


import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import com.visa.ssf.stonksTracker.Model.Mover;
import com.visa.ssf.stonksTracker.Model.Profile;
import com.visa.ssf.stonksTracker.Model.Quote;
import com.visa.ssf.stonksTracker.Service.RedisService;
import com.visa.ssf.stonksTracker.Service.StonkService;

@Controller
public class PriceController {
    private static final Logger logger = LoggerFactory.getLogger(PriceController.class);

    @Autowired
    StonkService stonkService;

    @Autowired
    RedisService redisService;

    @GetMapping(path="/movers")
    public String Movers(Model model, @ModelAttribute Mover indices){
        logger.info("In movers Controller, check indices dropdown >>>>> " + indices.getIndices());
        if (indices.getIndices() == null){   indices.setIndices("$SPX.X"); }

        Optional<List<Mover>> gainersList = stonkService.getTopMovers("up", indices.getIndices());
            if(gainersList.isPresent())
            {   model.addAttribute("GainersList", gainersList.get());   }
            else{
                List<Mover> gainers = new LinkedList<>(); 
                model.addAttribute("GainersList", gainers); } 
        Optional<List<Mover>> losersList = stonkService.getTopMovers("down", indices.getIndices());
            if(losersList.isPresent())
            {   model.addAttribute("LosersList", losersList.get());   }
            else{
                List<Mover> losers = new LinkedList<>();
                model.addAttribute("LosersList", losers);   }
          
        model.addAttribute("Indices", indices);
        return "movers";
    }


    public static String currTicker;

    @GetMapping(value={"/quote"})
    public String Quote(@RequestParam String ticker, Model model){

        logger.info("Check Param ticker >>> " + ticker);
        Optional<Quote> opQuote = stonkService.getQuote(ticker.toUpperCase());
        if (!opQuote.isPresent())
        {   Quote quote = new Quote();
            quote.setDescription("Ticker is Unavailable");
            model.addAttribute("Quote", quote);
            model.addAttribute("Profile", new Profile());
            return "quote";                                                 }   

        model.addAttribute("Quote", opQuote.get());
        
        // adding other API service
        Optional<Profile> opProfile = stonkService.getProfile(ticker);
        if (!opProfile.isPresent())
        {   model.addAttribute("Profile", new Profile());
           logger.info("returned empty Profile");                       }

        model.addAttribute("Profile", opProfile.get());
        currTicker = ticker.toUpperCase();

        logger.info("check currTicker" + currTicker);    

        return "quote";
    }

}
