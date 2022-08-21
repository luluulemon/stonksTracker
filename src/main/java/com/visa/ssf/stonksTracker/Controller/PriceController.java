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
    public String Movers(Model model){
        
        String SnPGainersArray = stonkService.getTopMovers("up");
        String SnPLosersArray = stonkService.getTopMovers("down");

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

        logger.info("Check Param ticker >>> " + ticker);
        Optional<Quote> opQuote = stonkService.getQuote(ticker.toUpperCase());
        if (!opQuote.isPresent())
        {   Quote quote = new Quote();
            quote.setDescription("Ticker is Unavailable");
            model.addAttribute("Quote", quote);
            return "quote";                                                 }   

        model.addAttribute("Quote", opQuote.get());

        logger.info("check mapper obj" + opQuote.get().getSymbol());    

        return "quote";
    }

}
