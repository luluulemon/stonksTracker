package com.visa.ssf.stonksTracker.Controller;

import java.util.Collections;
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
import org.springframework.web.bind.annotation.PostMapping;

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

    public static String currUser = null;          // current active user (Watchlist & Portfolio)
    public static List<String> currList = null;     

    
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
        if(currList==null){ watchlist.setErrorMsg("Empty List");    }
        
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
            logger.info("Check watchlist >>>> " + watchlist.getWatchList());
        Optional<List<Quote>> quotes = stonkService.getQuotesss(currList);
        watchlist.setQuotes(quotes.get());
        }
        model.addAttribute("Watchlist", watchlist);
        return "watchlist";
    }

    @GetMapping("/addToWatch/{ticker}")     // TO-DO -> fin up on add to watch on quote
    public String addStock2(@PathVariable String ticker, Model model){
        logger.info("Check for active Ticker >>> " + PriceController.currTicker);
        logger.info("Check for active user >>>> " + currUser);
        Watchlist watchlist = new Watchlist();
        // check if there is a current active user
        if(currUser == null)
        {   watchlist.setErrorMsg("Pls enter Username");    
            model.addAttribute("Watchlist", watchlist);
            return "watchlist";
        }
        // check if currList alr contains ticker
        watchlist.setUsername(currUser);    // Need this attribute for watchlist header
        ticker = ticker.toUpperCase();  
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


// watchlists --> Various Sorting
    String currSort = "";
    @GetMapping("/sortTicker")
    public String sortTickerDown(Model model){
        Watchlist watchlist = new Watchlist();
        watchlist.setUsername(currUser);
        if(currList !=null && currList.size()!=0){
            if(currSort.equals("tickerUp")){
                Collections.sort(currList, Collections.reverseOrder());
                currSort = "tickerDown";
                watchlist.setSorting(currSort);
                redisService.saveSortedWatchlist(currUser, currList);   // save sorted list (send List+user)
                Optional<List<Quote>> quotes = stonkService.getQuotesss(currList);
                watchlist.setQuotes(quotes.get());  
            }
            else{
                logger.info("Check sort " + currSort);
                Collections.sort(currList);     // update currList
                currSort = "tickerUp";
                watchlist.setSorting(currSort);
                redisService.saveSortedWatchlist(currUser, currList);   // save sorted list (send List+user)
                Optional<List<Quote>> quotes = stonkService.getQuotesss(currList);
                watchlist.setQuotes(quotes.get());    
            }
        }
        model.addAttribute("Watchlist", watchlist);
            // if list empty, return username. Else, return username & quotes
        return "watchlist";
    }

    @GetMapping("/sortNetChange")       // sort descending Net Change
    public String sortNetChangeDown(Model model){
            Watchlist watchlist = new Watchlist();
            watchlist.setUsername(currUser);
            if(currList !=null && currList.size()!=0){
                List<Quote> quotes = stonkService.getQuotesss(currList).get();
                if(currSort.equals("netUp"))
                {   currSort = "netDown";       watchlist.setSorting("netDown");
            // TO-DO: check for empty optional    
                    for(int i=0; i<quotes.size(); i++){     // bubble sort NetChange (descending) 
                        for(int j=1; j<quotes.size(); j++){
                            if(quotes.get(j).getNetChange() > quotes.get(j-1).getNetChange()){
                                Quote temp = quotes.get(j);
                                quotes.set(j, quotes.get(j-1));
                                quotes.set(j-1, temp);
                            }
                        }
                    }
                }
                else{
                    currSort =  "netUp";
                    watchlist.setSorting("netUp");
                    for(int i=0; i<quotes.size(); i++){     // bubble sort NetChange (descending) 
                        for(int j=1; j<quotes.size(); j++){
                            if(quotes.get(j).getNetChange() < quotes.get(j-1).getNetChange()){
                                Quote temp = quotes.get(j);
                                quotes.set(j, quotes.get(j-1));
                                quotes.set(j-1, temp);
                            }
                        }
                    }
                }
                watchlist.setQuotes(quotes);    // set sorted quotes
                for(int i=0; i<currList.size(); i++){       // update currList order
                    currList.set(i, quotes.get(i).getSymbol());
                }
                redisService.saveSortedWatchlist(currUser, currList);
            }
        model.addAttribute("Watchlist", watchlist);
        return "watchlist";     // with User + quotes (if currList not null)
    }


    @GetMapping("/sortPercentChange")       // sort descending Net Change
    public String sortPercentChangeDown(Model model){
            Watchlist watchlist = new Watchlist();
            watchlist.setUsername(currUser);
            if(currList !=null && currList.size()!=0){
                List<Quote> quotes = stonkService.getQuotesss(currList).get();
                if(currSort.equals("percentUp"))
                {   currSort = "percentDown";
                    watchlist.setSorting(currSort);
                // TO-DO: check for empty optional    
                    for(int i=0; i<quotes.size(); i++){     // bubble sort NetChange (descending) 
                        for(int j=1; j<quotes.size(); j++){
                            if(quotes.get(j).getNetChange()/quotes.get(j).getLastPrice() > 
                                quotes.get(j-1).getNetChange()/quotes.get(j-1).getLastPrice()){
                                Quote temp = quotes.get(j);
                                quotes.set(j, quotes.get(j-1));
                                quotes.set(j-1, temp);
                            }
                        }
                    }
                }
                else
                {   currSort = "percentUp";
                    watchlist.setSorting(currSort);
                // TO-DO: check for empty optional    
                    for(int i=0; i<quotes.size(); i++){     // bubble sort NetChange (descending) 
                        for(int j=1; j<quotes.size(); j++){
                            if(quotes.get(j).getNetChange()/quotes.get(j).getLastPrice() < 
                                quotes.get(j-1).getNetChange()/quotes.get(j-1).getLastPrice()){
                                Quote temp = quotes.get(j);
                                quotes.set(j, quotes.get(j-1));
                                quotes.set(j-1, temp);
                            }
                        }
                    }
                }
                watchlist.setQuotes(quotes);                // set sorted quotes
                for(int i=0; i<currList.size(); i++){       // update currList order
                    currList.set(i, quotes.get(i).getSymbol());
                }
                redisService.saveSortedWatchlist(currUser, currList);   // save to redis currList order
            }
        model.addAttribute("Watchlist", watchlist);
        return "watchlist";     // with User + quotes (if currList not null)
    }


// About Portfolio Display from here 
    public static Portfolio currPortfolioObj;

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
        currPortfolioObj = portfolio;       // update Portfolio Obj (for transactions display)

        if(portfolio.getPortfolio() != null)
        {   Portfolio displayPortfolio = getDisplayPortfolio2(portfolio.getPortfolio());
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
        {   portfolio.setErrorMsg("Ticker not available");    }
            
        else   // add to portfolio, save to redis --> (username, ticker, quantity, edate, eprice, description, comments)        
        {   portfolio.setDescription(opQuote.get().getDescription());
            currPortfolioObj = redisService.addToPortfolio(portfolio); }    // Save updated portfolio in redis                   
                                     
        returnPortfolio = currPortfolioObj; 
        if( portfolio.getErrorMsg()!=null){ returnPortfolio.setErrorMsg("Ticker not available"); }
        // Call quote service to get lastPrice;
        if(returnPortfolio.getPortfolio() != null){
            Portfolio displayPortfolio = getDisplayPortfolio2(returnPortfolio.getPortfolio());                   
            returnPortfolio.setPnL(displayPortfolio.getPnL());
            returnPortfolio.setPortfolio(displayPortfolio.getPortfolio());     
            // only display name, errorMsg, portfolio (with PnL), transactions
        }
        model.addAttribute("Portfolio", returnPortfolio); 
        return "portfolio";
    }


    @GetMapping("/removePortfolio/{index}")
    public String removePortfolio(@PathVariable int index, Model model){
        if(currUser == null)    
        {   Portfolio portfolio = new Portfolio();
            portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";
        }

        currPortfolioObj.setEditIndex(88888);   // In case in the middle of edit, undo the edit                 
        currPortfolioObj.getPortfolio().remove(index);      // updated currPortfolio
        redisService.removePortfolioItem(currPortfolioObj);    // save updated Portfolio to redis

        // Call quote service to get lastPrice;  Check to see list still have items after removal
        if(currPortfolioObj.getPortfolio().size() != 0){
        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolioObj.getPortfolio());
        currPortfolioObj.setPortfolio(displayPortfolio.getPortfolio());                       
        currPortfolioObj.setPnL(displayPortfolio.getPnL());                           }

        model.addAttribute("Portfolio", currPortfolioObj);      
        return "portfolio";             // pass back username, updated portfolio, transactions
    }


// editing Portfolio
    @GetMapping("/editPortfolio/{index}")
    public String editPortfolioIndex(@PathVariable int index, Model model)
    {   if(currUser == null)    
        {   Portfolio portfolio = new Portfolio();
            portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";                                         }
        
        // In case in the middle of close, reset (undo Boolean and index)
        if(currPortfolioObj.getEditIndex()>=1000000)    
        {   currPortfolioObj.getPortfolio().get(currPortfolioObj.getEditIndex()-1000000).setToClose(false);  }                                            
        // In case in the midst of Transaction edit, reset it (if past transactions is not null)
        if(currPortfolioObj.getPastTransactions()!=null)
        {   for(Quote q:currPortfolioObj.getPastTransactions()){    q.setToClose(false);    }   }

        Portfolio portfolio = new Portfolio();  // new Obj to dump into view, dun want edit values with currObj
        currPortfolioObj.setEditIndex(index);   
        portfolio.setEditIndex(index);     
        portfolio.setUsername(currPortfolioObj.getUsername());
        portfolio.setPortfolio(currPortfolioObj.getPortfolio());
        portfolio.setPastTransactions(currPortfolioObj.getPastTransactions());
        portfolio.setPastTradePnL(currPortfolioObj.getPastTradePnL());      

        // set details of portfolio to edit, throw to view
        portfolio.setTicker(currPortfolioObj.getPortfolio().get(index).getSymbol());
        portfolio.setEntryDate(currPortfolioObj.getPortfolio().get(index).getEntryDate());
        portfolio.setEntryPrice(currPortfolioObj.getPortfolio().get(index).getEntryPrice());
        portfolio.setQuantity(currPortfolioObj.getPortfolio().get(index).getQuantity());
        portfolio.setComments(currPortfolioObj.getPortfolio().get(index).getComments());

        // Call quote service to get lastPrice;
        Portfolio displayPortfolio = getDisplayPortfolio2(portfolio.getPortfolio());
        portfolio.setPortfolio(displayPortfolio.getPortfolio());                       
        portfolio.setPnL(displayPortfolio.getPnL());
    logger.info("In editPortfolioIndex, check Obj >>>> "+ currPortfolioObj.getEntryPrice());
        model.addAttribute("Portfolio", portfolio);
        // no need edit currPort or redis yet
        return "portfolio";
    }

    @GetMapping("/editPortfolio")
    public String editPortfolio(Model model, @ModelAttribute Portfolio portfolio){
        if(currUser == null)    
        {   portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";                                         }
        
        int editIndex = currPortfolioObj.getEditIndex();
        currPortfolioObj.setEditIndex(88888);   // reset to no edit
        //update curr PortfolioObj with edit item
        currPortfolioObj.getPortfolio().get(editIndex).setEntryDate(portfolio.getEntryDate());
        currPortfolioObj.getPortfolio().get(editIndex).setEntryPrice(portfolio.getEntryPrice());
        currPortfolioObj.getPortfolio().get(editIndex).setComments(portfolio.getComments());
        currPortfolioObj.getPortfolio().get(editIndex).setQuantity(portfolio.getQuantity());
        // save to redis
        redisService.editPortfolioItem(currPortfolioObj);
        model.addAttribute("Portfolio", currPortfolioObj);
        return "portfolio";
    }

    @GetMapping("/cancelEditPortfolio")
    public String cancelEdit(Model model){
        if(currUser == null)    
        {   Portfolio portfolio = new Portfolio();
            portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";                                         }

        currPortfolioObj.setEditIndex(88888); // reset to no edit
    // Call quote service to get lastPrice n PnL;
        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolioObj.getPortfolio());
        currPortfolioObj.setPortfolio(displayPortfolio.getPortfolio());                       
        currPortfolioObj.setPnL(displayPortfolio.getPnL());

        model.addAttribute("Portfolio", currPortfolioObj);
        return "portfolio";
    }

// Close Trade (add to Past Transactions)
    @GetMapping("/closePortfolio/{index}")
    public String closePortfolio(@PathVariable int index, Model model){
        if(currUser == null)    
        {   Portfolio portfolio = new Portfolio();
            portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";                                         }
 
        // Reset any edits in progress
        if(currPortfolioObj.getPastTransactions()!=null)
        {   for(Quote q: currPortfolioObj.getPastTransactions()){   q.setToClose(false);    }   }
        currPortfolioObj.setEditIndex(1000000+index);    // Set Index for midst of Close, reset any edit Portfolio 
        currPortfolioObj.getPortfolio().get(index).setToClose(true);  // updated currPortfolio with close Boolean
            
        // Call quote service to get lastPrice;     // *no update to redis
        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolioObj.getPortfolio());
        currPortfolioObj.setPortfolio(displayPortfolio.getPortfolio());                       
        currPortfolioObj.setPnL(displayPortfolio.getPnL()); 
        logger.info("check close Boolean " + currPortfolioObj.getPortfolio().get(index).getToClose());
        model.addAttribute("Portfolio", currPortfolioObj);      
        return "portfolio";             // pass back username, updated portfolio, transactions & PnL
    }

    @GetMapping("/cancelClosePortfolio/{index}")
    public String cancelClosePortfolio(@PathVariable int index, Model model){
        if(currUser == null)    
        {   Portfolio portfolio = new Portfolio();
            portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";                                         }

        // updated currPortfolio with close Boolean
        currPortfolioObj.getPortfolio().get(index).setToClose(false);      
        // Call quote service to get lastPrice;
        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolioObj.getPortfolio());
        currPortfolioObj.setPortfolio(displayPortfolio.getPortfolio());                       
        currPortfolioObj.setPnL(displayPortfolio.getPnL()); 

        model.addAttribute("Portfolio", currPortfolioObj);      
        return "portfolio";             // pass back username, updated portfolio, transactions
    }

    @GetMapping("/addToTransactions/{index}")   // close trade
    public String addToTransactions(@ModelAttribute Portfolio portfolio, Model model, @PathVariable int index){
        if(currUser == null)    
        {   portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";
        }
        // grab closeDate, closePrice
        portfolio.setUsername(currUser);    // send username/index(to remove)/closeDate/closePrice
        currPortfolioObj = redisService.addToTransaction(portfolio, index); // update Portfolio -> boolean reset   

        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolioObj.getPortfolio());
        currPortfolioObj.setPortfolio(displayPortfolio.getPortfolio());                       
        currPortfolioObj.setPnL(displayPortfolio.getPnL()); 

        model.addAttribute("Portfolio", currPortfolioObj);
        return "portfolio";
    }

// to remove Past Transaction
    @GetMapping("/removeTransaction/{index}")
    public String removeTransactionItem(@PathVariable int index, Model model){
        if(currUser == null)    
        {   Portfolio portfolio = new Portfolio();
            portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";                                         }
        
        currPortfolioObj.getPastTransactions().remove(index);   // update currPortfolioObj with removed transaction
        Float pastTradePnL = 0f;        // TO-DO move this to Service code
        for(Quote q:currPortfolioObj.getPastTransactions()){
            pastTradePnL += q.getPnL();
        }
        currPortfolioObj.setPastTradePnL(pastTradePnL);         // update transactions PnL
        logger.info("In remove transaction check username " + currPortfolioObj.getUsername());
        redisService.removeAndEditTransaction(currPortfolioObj);

        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolioObj.getPortfolio());
        currPortfolioObj.setPortfolio(displayPortfolio.getPortfolio());                       
        currPortfolioObj.setPnL(displayPortfolio.getPnL()); 
        model.addAttribute("Portfolio", currPortfolioObj);
        return "portfolio";
    }

// To Edit Past Transaction
    @GetMapping("/editTransaction/{index}")
    public String editTransaction(@PathVariable int index, Model model){
        if(currUser == null)    
        {   Portfolio portfolio = new Portfolio();
            portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";                                         }

        for(Quote q: currPortfolioObj.getPastTransactions())
        {   q.setToClose(false);    }   // undo past edit booleans first, reset other edits
        if(currPortfolioObj.getEditIndex()>=1000000)    // reset any Port closing, and Edit Port
        {   currPortfolioObj.getPortfolio().get(currPortfolioObj.getEditIndex()-1000000).setToClose(false);     }                                              
        currPortfolioObj.setEditIndex(88888);
        currPortfolioObj.getPastTransactions().get(index).setToClose(true);

    // Grab Portfolio & Transactions for display
        Portfolio portfolio = new Portfolio();
        portfolio.setUsername(currPortfolioObj.getUsername());
        portfolio.setPortfolio(currPortfolioObj.getPortfolio());
        portfolio.setPastTransactions(currPortfolioObj.getPastTransactions());
        portfolio.setPastTradePnL(currPortfolioObj.getPastTradePnL()); 
    // grab the various values to edit (all except Ticker and PnL)
        portfolio.seteCloseDate(currPortfolioObj.getPastTransactions().get(index).getCloseDate());
        portfolio.seteClosePrice(currPortfolioObj.getPastTransactions().get(index).getClosePrice());
        portfolio.seteEntryDate(currPortfolioObj.getPastTransactions().get(index).getEntryDate());
        portfolio.seteEntryPrice(currPortfolioObj.getPastTransactions().get(index).getEntryPrice());
        portfolio.seteQuantity(currPortfolioObj.getPastTransactions().get(index).getQuantity());
        portfolio.seteComments(currPortfolioObj.getPastTransactions().get(index).getComments());

        Portfolio displayPortfolio = getDisplayPortfolio2(portfolio.getPortfolio());
        portfolio.setPortfolio(displayPortfolio.getPortfolio());                       
        portfolio.setPnL(displayPortfolio.getPnL());
        model.addAttribute("Portfolio", portfolio);
        return "portfolio";
    }

    @GetMapping("/cancelEditTransaction/{index}")
    public String cancelEditTransaction(@PathVariable int index, Model model){
        if(currUser == null)    
        {   Portfolio portfolio = new Portfolio();
            portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";                                         }
        // reset the edit Boolean
        currPortfolioObj.getPastTransactions().get(index).setToClose(false);
        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolioObj.getPortfolio());
        currPortfolioObj.setPortfolio(displayPortfolio.getPortfolio());                       
        currPortfolioObj.setPnL(displayPortfolio.getPnL()); 

        model.addAttribute("Portfolio", currPortfolioObj);
        return "portfolio";
    }

    @PostMapping("/editTransaction/{index}")
    public String editTransaction(@ModelAttribute Portfolio portfolio, Model model, @PathVariable int index){
        if(currUser == null)    
        {   portfolio.setErrorMsg("Pls enter Username");    
            model.addAttribute("Portfolio", portfolio);
            return "portfolio";                                         }

        // Take in and set 6 values
        currPortfolioObj.getPastTransactions().get(index).setCloseDate(portfolio.geteCloseDate());
        currPortfolioObj.getPastTransactions().get(index).setClosePrice(portfolio.geteClosePrice());
        currPortfolioObj.getPastTransactions().get(index).setQuantity(portfolio.geteQuantity());
        currPortfolioObj.getPastTransactions().get(index).setEntryDate(portfolio.geteEntryDate());
        currPortfolioObj.getPastTransactions().get(index).setEntryPrice(portfolio.geteEntryPrice());
        currPortfolioObj.getPastTransactions().get(index).setComments(portfolio.geteComments());
        Float PnL = (portfolio.geteClosePrice() - portfolio.geteEntryPrice()) * portfolio.geteQuantity();
        currPortfolioObj.getPastTransactions().get(index).setPnL(PnL);
        // reset Boolean        // curr Obj updated
        currPortfolioObj.getPastTransactions().get(index).setToClose(false);
        // save to redis
        Float pastTradePnL = 0f;
        for(Quote q:currPortfolioObj.getPastTransactions()){
            pastTradePnL += q.getPnL();
        }
        currPortfolioObj.setPastTradePnL(pastTradePnL);
        redisService.removeAndEditTransaction(currPortfolioObj);

        // Call display function -> get Last price for Portfolio
        Portfolio displayPortfolio = getDisplayPortfolio2(currPortfolioObj.getPortfolio());
        currPortfolioObj.setPortfolio(displayPortfolio.getPortfolio());                       
        currPortfolioObj.setPnL(displayPortfolio.getPnL()); 

        model.addAttribute("Portfolio", currPortfolioObj);
        return "portfolio";
    }


// helper function
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
