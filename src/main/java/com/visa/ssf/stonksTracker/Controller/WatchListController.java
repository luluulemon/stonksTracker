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
import org.springframework.web.bind.annotation.PathVariable;

import com.visa.ssf.stonksTracker.Model.Portfolio;
import com.visa.ssf.stonksTracker.Model.Quote;
import com.visa.ssf.stonksTracker.Model.Watchlist;
import com.visa.ssf.stonksTracker.Service.RedisService;
import com.visa.ssf.stonksTracker.Service.StonkService;

@Controller
public class WatchListController {
    private static final Logger logger = LoggerFactory.getLogger(WatchListController.class);

    @Autowired
    RedisService redisService;

    @Autowired
    StonkService stonkService;

    public String currUser = null;          // current active user (Watchlist & Portfolio)
    public List<String> currList = null;     

    
    @GetMapping("/watchlist")
    public String watchlist(Model model, @ModelAttribute Watchlist watchlist){
        
        if(watchlist.getUsername() != null)        // 1. username entered, get user list     
        {   watchlist = redisService.getWatchList(watchlist);
            currUser = watchlist.getUsername();
            currList = watchlist.getWatchList();                    }
        
        else if(currUser != null)           // 2. no username entered, but with active user
        {   logger.info("Inside watchlist control, check active user >>>> " + currUser);
            watchlist.setUsername(currUser);
            watchlist = redisService.getWatchList(watchlist);   // load user watchlist
            currList = watchlist.getWatchList(); 
        }
        if(currList != null)  // if new/empty watchlist, do not get quotes
        {                                                           
            Optional<List<Quote>> quotes = stonkService.getQuotesss(watchlist.getWatchList());
            watchlist.setQuotes(quotes.get());                  
        }
        
        model.addAttribute("Watchlist", watchlist);
        return "watchlist";
    }


    @GetMapping("/addToWatch")
    public String addStock(@ModelAttribute Watchlist watchlist, Model model){

        logger.info("Check for active user >>>> " + currUser);
        
        // check if there is a current active user
        if(currUser == null)
        {   watchlist.setErrorMsg("Pls enter Username");    
            model.addAttribute("Watchlist", watchlist);
            return "watchlist";
        }
        // check if currList alr contains ticker
        watchlist.setUsername(currUser);    // Need this attribute for watchlist header
        String ticker = watchlist.getTicker().toUpperCase();  
        Boolean containsTicker = false;
        if(currList != null)     
        {    if(currList.contains(ticker))
            {   watchlist.setErrorMsg("Ticker alr in watchlist");     
                containsTicker = true;                                  } 
        }
        if(!containsTicker)     // if List alr contain, skip this conditional
        {    // check if ticker exists
            Optional<Quote> opQuote = stonkService.getQuote(ticker);
            if (!opQuote.isPresent())
            {   watchlist.setErrorMsg("Ticker not available");  } 
            else
            {   logger.info("ticker " + ticker + " is available");  
                watchlist = redisService.saveWatchList(ticker, currUser);
                currList = watchlist.getWatchList();    // update currList, with new ticker listing
            }
        }
        if(currList != null){   
            logger.info("Check currList >>>> " + currList);
            logger.info("Check watchlist >>>> " + watchlist.getWatchList());
        Optional<List<Quote>> quotes = stonkService.getQuotesss(currList);
        watchlist.setQuotes(quotes.get());
        }
        model.addAttribute("Watchlist", watchlist);
        return "watchlist";
    }


    @GetMapping("/remove/{symbol}")
    public String removeItem(@PathVariable String symbol, Model model){
        Watchlist watchlist = new Watchlist();
        watchlist.setUsername(currUser);
        boolean removed = currList.remove(symbol);  // currList updated, ticker removed
        logger.info("check remove >>>> " + symbol + "is removed: " + removed);
        watchlist.setWatchList(currList);

        // save to Redis
        redisService.removeSymbol(watchlist);       // saved updated list
        // display the quotesss
        if(currList.size() != 0){                   // make sure list not empty after removing
            Optional<List<Quote>> quotes = stonkService.getQuotesss(watchlist.getWatchList());
            watchlist.setQuotes(quotes.get());          }
        model.addAttribute("Watchlist", watchlist);
        return "watchlist";
    }


    // About Portfolio Display from here 
    public List<Quote> currPortfolio; 
    public Portfolio currPortfolioObj;

    @GetMapping("/portfolio")
    public String portfolio(@ModelAttribute Portfolio portfolio, Model model){
        // Inputs: only getting username, or nothing
        logger.info("In portfolio controller >> Check username" + portfolio.getUsername());
        if(portfolio.getUsername() != null)         // getting portfolio for new user
        {   currUser = portfolio.getUsername();     // set curr active user 
            portfolio = redisService.getPortfolio(portfolio);
            portfolio.setErrorMsg("Loaded " + currUser + " portfolio");
        }       
        else if (currUser != null)       // alr have active user
        {   portfolio.setUsername(currUser);    
            portfolio = redisService.getPortfolio(portfolio);
        }
        currPortfolio = portfolio.getPortfolio();
        currPortfolioObj = portfolio;       // update Portfolio Obj (for transactions display)

        if(portfolio.getPortfolio() != null)
        {   Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolio);
            portfolio.setPortfolio(displayPortfolio.getPortfolio());                       
            portfolio.setPnL(displayPortfolio.getPnL());                        }
        model.addAttribute("Portfolio", portfolio);
        return "portfolio";
    }


 

    @GetMapping("/addToPortfolio")
    public String addPortfolio(@ModelAttribute Portfolio portfolio, Model model){
    // collect ticker, quantity, date, price, comments
        
        portfolio.setUsername(currUser);
        String ticker = portfolio.getTicker().toUpperCase();
        portfolio.setTicker(ticker);    // make sure ticker uppercase in Portfolio obj
        
        // check if there is a current active user
        if(currUser == null)    
        {   portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";
        }
        
        Portfolio returnPortfolio = new Portfolio();        // New Portfolio -> no need to return ticker details
        // check if ticker exists
        Optional<Quote> opQuote = stonkService.getQuote(ticker);
        if (!opQuote.isPresent())
        {   returnPortfolio.setErrorMsg("Ticker not available");    }
            
        else   // add to portfolio, save to redis --> (username, ticker, quantity, edate, eprice, description, comments)        
        {   portfolio.setDescription(opQuote.get().getDescription());
            portfolio = redisService.addToPortfolio(portfolio);     // Save updated portfolio in redis
            //returnPortfolio.setPastTransactions(portfolio.getPastTransactions());              
            currPortfolio = portfolio.getPortfolio();                } // update current portfolio (list of Quotes)
        
        // Call quote service to get lastPrice;
        if(currPortfolio != null){
            Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolio);                   
            returnPortfolio.setPnL(displayPortfolio.getPnL());
            returnPortfolio.setPortfolio(displayPortfolio.getPortfolio());     
            // only display name, errorMsg, portfolio (with PnL), transactions
        }
        returnPortfolio.setUsername(currUser);
        returnPortfolio.setPastTransactions(currPortfolioObj.getPastTransactions());
        returnPortfolio.setPastTradePnL(currPortfolioObj.getPastTradePnL());
        model.addAttribute("Portfolio", returnPortfolio);
           
        return "portfolio";
    }


    @GetMapping("/removePortfolio/{index}")
    public String removePortfolio(@PathVariable int index, Model model){

        Portfolio portfolio = new Portfolio();
        portfolio.setUsername(currUser);
        portfolio.setPastTransactions(currPortfolioObj.getPastTransactions());
        portfolio.setPastTradePnL(currPortfolioObj.getPastTradePnL());
        logger.info("Check removed quote >>> " + currPortfolio.get(index).getSymbol());
        currPortfolio.remove(index);        // updated currPortfolio
        portfolio.setPortfolio(currPortfolio);

        portfolio = redisService.removePortfolioItem(portfolio);    // save updated Portfolio to redis

        // Call quote service to get lastPrice;
        if(currPortfolio.size() != 0){
        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolio);
        portfolio.setPortfolio(displayPortfolio.getPortfolio());                       
        portfolio.setPnL(displayPortfolio.getPnL());                           }

        model.addAttribute("Portfolio", portfolio);      
        return "portfolio";             // pass back username, updated portfolio, transactions
    }

    @GetMapping("/closePortfolio/{index}")
    public String closePortfolio(@PathVariable int index, Model model){
        // TO-DO check for null user
        logger.info("Check close item index >>>> " + index);
        Portfolio portfolio = new Portfolio();
        portfolio.setUsername(currUser);
        portfolio.setPastTransactions(currPortfolioObj.getPastTransactions());
        portfolio.setPastTradePnL(currPortfolioObj.getPastTradePnL());
        currPortfolio.get(index).setToClose(true);  // updated currPortfolio with close Boolean
                                                            // *no update to redis
        // Call quote service to get lastPrice;     
        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolio);
        portfolio.setPortfolio(displayPortfolio.getPortfolio());                       
        portfolio.setPnL(displayPortfolio.getPnL()); 
        logger.info("check close Boolean " + portfolio.getPortfolio().get(index).getToClose());
        model.addAttribute("Portfolio", portfolio);      
        return "portfolio";             // pass back username, updated portfolio, transactions
    }

    @GetMapping("/cancelClosePortfolio/{index}")
    public String cancelClosePortfolio(@PathVariable int index, Model model){
        // TO DO -> check for null currUser/port

        Portfolio portfolio = new Portfolio();
        portfolio.setUsername(currUser);
        portfolio.setPastTransactions(currPortfolioObj.getPastTransactions());
        portfolio.setPastTradePnL(currPortfolioObj.getPastTradePnL());
        currPortfolio.get(index).setToClose(false);       // updated currPortfolio with close Boolean
        portfolio.setPortfolio(currPortfolio);              // update for display                  

        logger.info("check close Boolean " + portfolio.getPortfolio().get(index).getToClose());

        // Call quote service to get lastPrice;
        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolio);
        portfolio.setPortfolio(displayPortfolio.getPortfolio());                       
        portfolio.setPnL(displayPortfolio.getPnL()); 
        logger.info("check close Boolean " + portfolio.getPortfolio().get(index).getToClose());
        model.addAttribute("Portfolio", portfolio);      
        return "portfolio";             // pass back username, updated portfolio, transactions
    }

    @GetMapping("/addToTransactions/{index}")
    public String addToTransactions(@ModelAttribute Portfolio portfolio, Model model, @PathVariable int index){
        // check if there is a current active user
        if(currUser == null)    
        {   portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";
        }
        
        // grab closeDate, closePrice
        portfolio.setUsername(currUser);
        portfolio = redisService.addToTransaction(portfolio, index);    // send username/index(to remove)/closeDate/closePrice    
        currPortfolio = portfolio.getPortfolio();       // update CurrPortfolio
        currPortfolioObj = portfolio;                   // update Portfolio (for transactions)

        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolio);
        portfolio.setPortfolio(displayPortfolio.getPortfolio());                       
        portfolio.setPnL(displayPortfolio.getPnL()); 

        model.addAttribute("Portfolio", portfolio);
        return "portfolio";
    }


    @GetMapping("/removeTransaction/{index}")
    public String removeTransactionItem(@PathVariable int index, Model model){
        currPortfolioObj.getPastTransactions().remove(index);   // update currPortfolioObj with removed transaction
        Float pastTradePnL = 0f;
        for(Quote q:currPortfolioObj.getPastTransactions()){
            pastTradePnL += q.getPnL();
        }
        currPortfolioObj.setPastTradePnL(pastTradePnL);         // update transactions PnL
        currPortfolioObj.getPastTransactions().remove(index);   // update currPortfolioObj with removed transaction
        logger.info("In remove transaction check username " + currPortfolioObj.getUsername());

        redisService.removeTransaction(currPortfolioObj);

        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolio);
        currPortfolioObj.setPortfolio(displayPortfolio.getPortfolio());                       
        currPortfolioObj.setPnL(displayPortfolio.getPnL()); 
        model.addAttribute("Portfolio", currPortfolioObj);
        return "portfolio";
    }


    public Portfolio getDisplayPortfolio2(List<Quote> currPortfolio){       // helper function to get Last Price for display
        List<Quote> displayPortfolio = new LinkedList<>();
        Float totalPnL = 0f;
        for(Quote quote: currPortfolio){
            Optional<Quote> opCurrQuote = stonkService.getQuote(quote.getSymbol());
            quote.setLastPrice(opCurrQuote.get().getLastPrice());
            Float PnL = quote.getQuantity() * (quote.getLastPrice() - quote.getEntryPrice());
            quote.setPnL(PnL);
            displayPortfolio.add(quote);
            totalPnL += PnL;
        }
        Portfolio portfolio = new Portfolio();
        portfolio.setPnL(totalPnL);
        portfolio.setPortfolio(displayPortfolio);
        return portfolio;           // returns portfolio (with Last) and total PnL
    }
}
