package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.util.*;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;
import com.crio.warmup.stock.PortfolioManagerApplication;

public class PortfolioManagerImpl implements PortfolioManager {



    StockQuotesService stockQuotesService;
    //private final RestTemplate restTemplate;
    private RestTemplate restTemplate;
    // Caution: Do not delete or modify the constructor, or else your build will
    // break!
    // This is absolutely necessary for backward compatibility
    
    @Deprecated
    protected PortfolioManagerImpl(RestTemplate restTemplate) {
      this.restTemplate = restTemplate;
    }
    
    protected PortfolioManagerImpl(StockQuotesService s){

      stockQuotesService = s;
    }


    // TODO: CRIO_TASK_MODULE_REFACTOR
    // 1. Now we want to convert our code into a module, so we will not call it from
    // main anymore.
    // Copy your code from Module#3
    // PortfolioManagerApplication#calculateAnnualizedReturn
    // into #calculateAnnualizedReturn function here and ensure it follows the
    // method signature.
    // 2. Logic to read Json file and convert them into Objects will not be required
    // further as our
    // clients will take care of it, going forward.

    // Note:
    // Make sure to exercise the tests inside PortfolioManagerTest using command
    // below:
    // ./gradlew test --tests PortfolioManagerTest

    // CHECKSTYLE:OFF

    



	private Comparator<AnnualizedReturn> getComparator() {
      return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
    }

    // CHECKSTYLE:OFF

    // TODO: CRIO_TASK_MODULE_REFACTOR
    // Extract the logic to call Tiingo third-party APIs to a separate function.
    // Remember to fill out the buildUri function and use that.

    public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
        throws JsonProcessingException,StockQuoteServiceException {

      if (from.compareTo(to) >= 0) {
        throw new RuntimeException();
      }
      //String url = buildUri(symbol, from, to);
      // object of Stocksquotesservice
     return stockQuotesService.getStockQuote(symbol, from, to);
     
}

 public  List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate)
    throws JsonProcessingException {

  List<AnnualizedReturn> a = new ArrayList<>();
  try {
  for (PortfolioTrade trade : portfolioTrades) 
  {

      //List<Candle> candles = null;
      List<Candle> candles = new ArrayList<Candle>();
      String moduleToRun = null;
      moduleToRun = "ADDITIONAL_REFACTOR";

        //CHECKSTYLE:OFF
        candles = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
        //CHECKSTYLE:ON

      List<Candle> sortedAndFilteredCandles = candles.stream().filter(candle ->
          trade.getPurchaseDate().atStartOfDay().minus(1, SECONDS)
              .isBefore(candle.getDate().atStartOfDay())
              && endDate.plus(1, DAYS).atStartOfDay().isAfter(candle.getDate().atStartOfDay()))
          .sorted(Comparator.comparing(Candle::getDate))
          .collect(Collectors.toList());

      Double buyPrice = sortedAndFilteredCandles.get(0).getOpen();
      Candle lastCandle = sortedAndFilteredCandles.get(sortedAndFilteredCandles.size() - 1);
      Double sellPrice = lastCandle.getClose();

      double totalReturns = ((sellPrice - buyPrice) / buyPrice);
      double years =
          trade.getPurchaseDate().until(lastCandle.getDate(), ChronoUnit.DAYS) / 365.2425d;
      double annualizedReturns = Math.pow((1 + totalReturns), (1 / years)) - 1;
      AnnualizedReturn a1 = new AnnualizedReturn(trade.getSymbol(), annualizedReturns,totalReturns);
       a.add(a1);
  }
}
     catch (JsonProcessingException e) {
      e.printStackTrace();      
    }
    catch(StockQuoteServiceException e)
    {
      e.getMessage();
    }


  Comparator<AnnualizedReturn> byAnnualizedreturns = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn);
  Collections.sort(a, byAnnualizedreturns);
  Collections.reverse(a);
  return a;

}

protected static String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
      String uriTemplate = "https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?"+"startDate="+startDate.toString()+"&endDate="+endDate.toString()+"&token="+"8fd469602c7c356290b606269650a07e831ffef5";
            
      return uriTemplate;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate, int numThreads) throws InterruptedException, StockQuoteServiceException {
    
      ExecutorService executor = Executors.newFixedThreadPool(numThreads);
      List<Future<AnnualizedReturn>> futures = new ArrayList<>();
      List<AnnualizedReturn> annualizedReturns= new ArrayList<>();
      for (PortfolioTrade trade : portfolioTrades) {

        Callable<AnnualizedReturn> callable = () -> {
          AnnualizedReturn annualizedReturn = getannualizedreturns(trade, endDate);
          annualizedReturns.add(annualizedReturn);
          return annualizedReturn;
        };
        
        Future<AnnualizedReturn> future = executor.submit(callable);
        futures.add(future);
      }
      try{
       for (Future<AnnualizedReturn> f : futures) {
        f.get();

      }
      Comparator<AnnualizedReturn> byAnnualizedreturns = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn);
      Collections.sort(annualizedReturns, byAnnualizedreturns);
      Collections.reverse(annualizedReturns);
    }
      catch(InterruptedException | ExecutionException e)
      {
        throw new StockQuoteServiceException("failed");
      }
      executor.shutdown();
      return annualizedReturns;
        
  }

  public AnnualizedReturn getannualizedreturns(PortfolioTrade trade,LocalDate endDate)
  {
    List<Candle> candles = new ArrayList<Candle>();
    String moduleToRun = null;
    moduleToRun = "ADDITIONAL_REFACTOR";

      //CHECKSTYLE:OFF
      try {
      candles = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
    }catch (StockQuoteServiceException e) {
      
      e.printStackTrace();
    } catch (JsonProcessingException e) {
      
      e.printStackTrace();
    } 
      //CHECKSTYLE:ON

    List<Candle> sortedAndFilteredCandles = candles.stream().filter(candle ->
        trade.getPurchaseDate().atStartOfDay().minus(1, SECONDS)
            .isBefore(candle.getDate().atStartOfDay())
            && endDate.plus(1, DAYS).atStartOfDay().isAfter(candle.getDate().atStartOfDay()))
        .sorted(Comparator.comparing(Candle::getDate))
        .collect(Collectors.toList());

    Double buyPrice = sortedAndFilteredCandles.get(0).getOpen();
    Candle lastCandle = sortedAndFilteredCandles.get(sortedAndFilteredCandles.size() - 1);
    Double sellPrice = lastCandle.getClose();

    double totalReturns = ((sellPrice - buyPrice) / buyPrice);
    double years =
        trade.getPurchaseDate().until(lastCandle.getDate(), ChronoUnit.DAYS) / 365.2425d;
    double annualizedReturns = Math.pow((1 + totalReturns), (1 / years)) - 1;
    AnnualizedReturn a1 = new AnnualizedReturn(trade.getSymbol(), annualizedReturns,totalReturns);
    return a1;
  }

 
}
