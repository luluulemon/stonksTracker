package com.visa.ssf.stonksTracker.Controller;


import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visa.ssf.stonksTracker.Model.Mover;
import com.visa.ssf.stonksTracker.Model.Quote;
import com.visa.ssf.stonksTracker.Service.StonkService;

@Controller
public class PriceController {
    private static final Logger logger = LoggerFactory.getLogger(PriceController.class);

    @Autowired
    StonkService stonkservice;

    @GetMapping(path="/movers")
    public String Movers(Model model){
        
        String SnPGainersArray = stonkservice.getTopMovers("up");
        String SnPLosersArray = stonkservice.getTopMovers("down");

        ObjectMapper mapper = new ObjectMapper();
        try
        {
            List<Mover> GainerList = mapper.readValue(SnPGainersArray, 
                    new TypeReference<List<Mover>>(){});
            model.addAttribute("MoverList", GainerList);

            List<Mover> SnPLosersList = mapper.readValue(SnPLosersArray, 
            new TypeReference<List<Mover>>(){});
            model.addAttribute("SnPLoserList", SnPLosersList);

            logger.info(GainerList.get(0).getDescription());
            logger.info(SnPLosersList.get(0).getDescription());
        }
        catch(JsonMappingException e){  logger.info(e.getMessage());    }
        catch(JsonProcessingException e){  logger.info(e.getMessage());    }

        return "movers";
    }


    @GetMapping("/quote")
    public String Quote(@RequestParam String ticker, Model model){

        logger.info("Check Param ticker" + ticker);
        Optional<String> opQuoteJson = stonkservice.getQuote(ticker.toUpperCase());
        if (!opQuoteJson.isPresent())
        {   Quote quote = new Quote();
            quote.setDescription("Ticker is Unavailable");
            model.addAttribute("Quote", quote);
            return "quote";                                                 }   


        ObjectMapper mapper = new ObjectMapper();
        try{
        Quote quote = mapper.readValue(opQuoteJson.get(), Quote.class);
        model.addAttribute("Quote", quote);

        logger.info("check mapper obj" + quote.getSymbol());
        }
        catch(JsonMappingException e){  logger.info(e.getMessage());    }
        catch(JsonProcessingException e){  logger.info(e.getMessage());    }      

        return "quote";
    }

    @GetMapping("/portfolio")
    public String portfolio(){
        return "portfolio";
    }


    @GetMapping("/watchlist")
    public String watchlist(){
        return "watchlist";
    }

}
