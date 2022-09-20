package com.visa.ssf.stonksTracker.Service;


import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.visa.ssf.stonksTracker.Model.Portfolio;
import com.visa.ssf.stonksTracker.Model.Quote;
import com.visa.ssf.stonksTracker.Model.Watchlist;

@Service
public class RedisService {
    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    RedisTemplate<String,Object> redistemplate;

    public Watchlist saveWatchList(String ticker, String userName){

        String user = userName + "miniProjectWatchlist";
   
        Watchlist watchlist = (Watchlist) redistemplate.opsForValue().get(user);
        List<String> tickerList = watchlist.getWatchList();   // grab current list (List of String Tickers);

        if (tickerList == null)       // if watchlist is null, create new list, add ticker to list
        {   List<String> watchlistStocks = new LinkedList<>();   
            watchlistStocks.add(ticker);
            watchlist.setWatchList(watchlistStocks);
        }
        else
        {
            tickerList.add(ticker);
            logger.info("Check tickerSet to save >>>>> " + tickerList);
            watchlist.setUsername(userName);
            watchlist.setWatchList(tickerList);
        }
        redistemplate.opsForValue().set(watchlist.getUsername()+"miniProjectWatchlist", watchlist);
        // watchlist is returned with 1. username, 2. (List)updated watchlist
        return watchlist;       
    }

    public void saveSortedWatchlist(String userName, List<String> currList){
        String user = userName + "miniProjectWatchlist";
        Watchlist watchlist = new Watchlist();
        watchlist.setUsername(userName);
        watchlist.setWatchList(currList);
        redistemplate.opsForValue().set(user, watchlist);   
    }       // save to redis with Username, and List of Ticker(String)



    public void removeSymbol(Watchlist watchlist){
        String user = watchlist.getUsername() + "miniProjectWatchlist";
        redistemplate.opsForValue().set(user, watchlist);
    }

    public Watchlist checkWatchList(String username){
        String user = username + "miniProjectWatchlist";
        if(!redistemplate.hasKey(user))
            return null;
        Watchlist watchlist = (Watchlist) redistemplate.opsForValue().get(user);
        return watchlist;
    }

    public Watchlist getWatchList(Watchlist watchlist){
        String user = watchlist.getUsername() + "miniProjectWatchlist";
        boolean userExists = redistemplate.hasKey(user);
        logger.info("In service check if user exists >>>" + userExists);
        if(!userExists)
        {   // create new record for new user
            redistemplate.opsForValue().set(watchlist.getUsername()+"miniProjectWatchlist", watchlist);
            watchlist.setErrorMsg("New Watchlist created"); 
            return watchlist;
        }

        watchlist = (Watchlist) redistemplate.opsForValue().get(user);
        return watchlist;
    }


    public Portfolio checkPortfolio(String username){
        String user = username + "miniProjectPortfolio";
        if(!redistemplate.hasKey(user))
            return null;
        Portfolio portfolio = (Portfolio) redistemplate.opsForValue().get(user);
        return portfolio;
    }

    public Portfolio getPortfolio(Portfolio portfolio){     // getting username
        String user = portfolio.getUsername() + "miniProjectPortfolio";
        boolean userExists = redistemplate.hasKey(user);
        logger.info("In service check if user exists >>>" + userExists);
        if(!userExists)
        {   // create new portfolio record for new user -> empty portfolio with username
            redistemplate.opsForValue().set(portfolio.getUsername()+"miniProjectPortfolio", portfolio);
            portfolio.setErrorMsg("No current portfolio"); 
            return portfolio;   // portfolio with username & errorMsg(not saved in Redis)
        }

        portfolio = (Portfolio) redistemplate.opsForValue().get(user);
        if(portfolio.getPortfolio() == null)
        {   portfolio.setErrorMsg("No current portfolio");  }
        return portfolio;
    }


    public Portfolio addToPortfolio(Portfolio portfolio){
        // passed ticker (validated), quantity, entryDate, entryPrice
        String user = portfolio.getUsername() + "miniProjectPortfolio";
        Portfolio port = (Portfolio) redistemplate.opsForValue().get(user);
        Quote quote = new Quote();
        quote.setSymbol(portfolio.getTicker());     // transfer features over to Quote to be saved
        quote.setQuantity(portfolio.getQuantity()); // quantity/entryDate/entryPrice/description/comments       
        quote.setEntryDate(portfolio.getEntryDate());
        quote.setEntryPrice(portfolio.getEntryPrice());
        quote.setDescription(portfolio.getDescription());
        quote.setComments(portfolio.getComments());

        if(port.getPortfolio()== null) // no list created yet
        {   List<Quote> tempPortfolio = new LinkedList<>();
            tempPortfolio.add(quote);
            port.setPortfolio(tempPortfolio);
        }
        else
        {   port.getPortfolio().add(quote);    }     // else, add to exsiting list
        
        redistemplate.opsForValue().set(user, port);   // save portfolio to redis with list
        return port;
    }

    public void removePortfolioItem(Portfolio portfolio){
        String user = portfolio.getUsername() + "miniProjectPortfolio";
        Portfolio redisPortfolio = (Portfolio) redistemplate.opsForValue().get(user);
        redisPortfolio.setPortfolio(portfolio.getPortfolio());
        redistemplate.opsForValue().set(user, redisPortfolio);

    }

    public void editPortfolioItem(Portfolio portfolio){     // pass in updated portfolio
        String user = portfolio.getUsername() + "miniProjectPortfolio";
        redistemplate.opsForValue().set(user, portfolio);   
    }

    public Portfolio addToTransaction(Portfolio portfolio, int index){
        // passed in username, and closeDate, closePrice, index
        String user = portfolio.getUsername() + "miniProjectPortfolio";
        Portfolio redisPortfolio = (Portfolio) redistemplate.opsForValue().get(user);

        // set closeDate, closePrice, PnL for transaction
        Quote newTransaction = redisPortfolio.getPortfolio().get(index); 
        newTransaction.setCloseDate(portfolio.getCloseDate());  
        newTransaction.setClosePrice(portfolio.getClosePrice());
        Float PnL = (newTransaction.getClosePrice() - newTransaction.getEntryPrice())
                    * newTransaction.getQuantity();
        newTransaction.setPnL(PnL);     
        redisPortfolio.getPortfolio().remove(index);    // update closed trade, remove from portfolio

        if (redisPortfolio.getPastTransactions() == null){
            List<Quote> transactions = new LinkedList<>();  // add new Transaction to new empty List
            transactions.add(newTransaction);
            redisPortfolio.setPastTransactions(transactions);
            redisPortfolio.setPastTradePnL(newTransaction.getPnL());    // set total PnL
        }
        else
        {    //redisPortfolio.getPastTransactions().add(newTransaction);
            List<Quote> transactions = redisPortfolio.getPastTransactions();
            transactions.add(newTransaction);
            Float pastTradePnL = 0f;
            for(Quote trade:transactions)
            {   pastTradePnL += trade.getPnL();     }
            redisPortfolio.setPastTradePnL(pastTradePnL);       // set total PnL
            redisPortfolio.setPastTransactions(transactions);   // set updated transactions
        }
        redistemplate.opsForValue().set(user, redisPortfolio);      // update redis with transaction & closed trade

        return redisPortfolio;
    }


    public void removeAndEditTransaction(Portfolio portfolio){
        // grab redisPortfolio -> update Transactions List n PnL
        String user = portfolio.getUsername() + "miniProjectPortfolio";
        Portfolio redisPortfolio = (Portfolio) redistemplate.opsForValue().get(user); 
        redisPortfolio.setPastTransactions(portfolio.getPastTransactions());
        redisPortfolio.setPastTradePnL(portfolio.getPastTradePnL());

        redistemplate.opsForValue().set(user, redisPortfolio);
    }
    
}
