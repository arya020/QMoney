
package com.crio.warmup.stock.portfolio;


import java.time.LocalDate;
import java.util.List;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.quotes.StockQuoteServiceFactory;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory {


  

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement the method to return new instance of PortfolioManager.
  //  Steps:
  //    1. Create appropriate instance of StoockQuoteService using StockQuoteServiceFactory and then
  //       use the same instance of StockQuoteService to create the instance of PortfolioManager.
  //    2. Mark the earlier constructor of PortfolioManager as @Deprecated.
  //    3. Make sure all of the tests pass by using the gradle command below:
  //       ./gradlew test --tests PortfolioManagerFactory
  
  
  
  
  public static PortfolioManager getPortfolioManager(StockQuotesService s) {

    PortfolioManager p = new PortfolioManagerImpl(s);
     return p;
   }

   public static PortfolioManager getPortfolioManager(String provider,
     RestTemplate restTemplate) {

    StockQuotesService s = StockQuoteServiceFactory.getService(provider,restTemplate);
    PortfolioManager p = new PortfolioManagerImpl(s);
    return p; 
   }

public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {

  StockQuotesService s = StockQuoteServiceFactory.getService("Alphavantage",restTemplate);
  PortfolioManager p = new PortfolioManagerImpl(s);
  return p; 
}

public static PortfolioManager getPortfolioManager() {
  
  PortfolioManager p = new PortfolioManagerImpl(new RestTemplate());
  return p;
}

}
