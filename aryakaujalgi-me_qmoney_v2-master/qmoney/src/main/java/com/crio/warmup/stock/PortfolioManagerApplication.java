
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sound.sampled.Port;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication {

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Task:
  // - Read the json file provided in the argument[0], The file is available in
  // the classpath.
  // - Go through all of the trades in the given file,
  // - Prepare the list of all symbols a portfolio has.
  // - if "trades.json" has trades like
  // [{ "symbol": "MSFT"}, { "symbol": "AAPL"}, { "symbol": "GOOGL"}]
  // Then you should return ["MSFT", "AAPL", "GOOGL"]
  // Hints:
  // 1. Go through two functions provided - #resolveFileFromResources() and
  // #getObjectMapper
  // Check if they are of any help to you.
  // 2. Return the list of all symbols in the same order as provided in json.

  // Note:
  // 1. There can be few unused imports, you will need to fix them to make the
  // build pass.
  // 2. You can use "./gradlew build" to check if your code builds successfully.

  private static Object portfolioTrades;

  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {

    List<PortfolioTrade> list = new ArrayList<PortfolioTrade>();
    File f = resolveFileFromResources(filename);
    PortfolioTrade[] t = getObjectMapper().readValue(f, PortfolioTrade[].class);

    for (PortfolioTrade tr : t) {
      list.add(tr);
    }

    // return Collections.emptyList();
    return list;
  }
  
  public static List<String> mainReadFile(String args[]) throws IOException, URISyntaxException {
    String filename = args[0];

    List<String> list = new ArrayList<String>();
    File f = resolveFileFromResources(filename);
    PortfolioTrade[] t = getObjectMapper().readValue(f, PortfolioTrade[].class);

    for (PortfolioTrade tr : t) {
      list.add(tr.getSymbol());
    }

    // return Collections.emptyList();
    return list;
  }

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  // and deserialize the results in List<Candle>

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {

    File f = resolveFileFromResources(args[0]);
    PortfolioTrade[] t = getObjectMapper().readValue(f, PortfolioTrade[].class);
    LocalDate d = LocalDate.parse(args[1]);
    RestTemplate rs = new RestTemplate();
    HashMap<String, Double> stocks = new HashMap<>();
    for (PortfolioTrade tr : t) {

      String url = prepareUrl(tr, d, "8fd469602c7c356290b606269650a07e831ffef5");
      TiingoCandle c[] = new RestTemplate().getForObject(url, TiingoCandle[].class);
      stocks.put(tr.getSymbol(), c[c.length - 1].getClose());
      if (tr.getPurchaseDate().isAfter(c[c.length - 1].getDate())) {
        RuntimeException e = new RuntimeException();
        throw e;
      }

    }
    List<Double> pList = new ArrayList<>(stocks.values());
    Collections.sort(pList);
    List<String> stock = new ArrayList<>();
    for (Double i : pList) {

      Set<String> keys = stocks.keySet();
      // iterate all keys
      for (String key : keys) {

        // if maps value for the current key matches, return the key
        if (stocks.get(key).equals(i)) {
          stock.add(key);
        }
      }
    }

    return stock;
    // return Collections.emptyList();
  }

  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {

    String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?" + "startDate="
        + trade.getPurchaseDate().toString() + "&endDate=" + endDate.toString() + "&token=" + token;
    return url;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper objectmapper = new ObjectMapper();
    logger.info(objectmapper.writeValueAsString(object));

  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Follow the instructions provided in the task documentation and fill up the
  // correct values for
  // the variables provided. First value is provided for your reference.
  // A. Put a breakpoint on the first line inside mainReadFile() which says
  // return Collections.emptyList();
  // B. Then Debug the test #mainReadFile provided in
  // PortfoliomanagerApplicationTest.java
  // following the instructions to run the test.
  // Once you are able to run the test, perform following tasks and record the
  // output as a
  // String in the function below.
  // Use this link to see how to evaluate expressions -
  // https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  // 1. evaluate the value of "args[0]" and set the value
  // to the variable named valueOfArgument0 (This is implemented for your
  // reference.)
  // 2. In the same window, evaluate the value of expression below and set it
  // to resultOfResolveFilePathArgs
  // expression ==> resolveFileFromResources(args[0])
  // 3. In the same window, evaluate the value of expression below and set it
  // to toStringOfObjectMapper.
  // You might see some garbage numbers in the output. Dont worry, its expected.
  // expression ==> getObjectMapper().toString()
  // 4. Now Go to the debug window and open stack trace. Put the name of the
  // function you see at
  // second place from top to variable functionNameFromTestFileInStackTrace
  // 5.In the same window,you will see the line number of the function in the
  // stack trace window.
  // assign the same to lineNumberFromTestFileInStackTrace
  // Once you are done with above, just run the corresponding test and
  // make sure its working as expected. use below command to do the same.
  // ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/aryakaujalgi-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    // String toStringOfObjectMapper = "byte[52]@74";
    String toStringOfObjectMapper = getObjectMapper().toString();
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "29";

    return Arrays.asList(new String[] { valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
        functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace });
  }

  // Note:
  // Remember to confirm that you are getting same results for annualized returns
  // as in Module 3.
  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Now that you have the list of PortfolioTrade and their data, calculate
  // annualized returns
  // for the stocks provided in the Json.
  // Use the function you just wrote #calculateAnnualizedReturns.
  // Return the list of AnnualizedReturns sorted by annualizedReturns in
  // descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.
  public static Double getClosingPriceOnEndDate(List<Candle> candles) {

    return candles.get(candles.size() - 1).getClose();
  }

  public static Double getOpeningPriceOnStartDate(List<Candle> candles) {

    return candles.get(0).getOpen();
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    List<Candle> candles = new ArrayList<>();
    String url = prepareUrl(trade,endDate, token);
    System.out.println(url);
    TiingoCandle c[] = new RestTemplate().getForObject(url, TiingoCandle[].class);
    candles = Arrays.asList(c);
    return candles;
  }

 
  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
    String file = args[0];
    final LocalDate endDate = LocalDate.parse(args[1]);
    ObjectMapper objectMapper = getObjectMapper();
   PortfolioTrade[] portfolioTrades = objectMapper.readValue(resolveFileFromResources(file), PortfolioTrade[].class);
    final String token = "d7ee5290251fd4882f10fde8ada179ccc1450745";
    String uri = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
  
      List<AnnualizedReturn> a = new ArrayList<>();
      for(PortfolioTrade trade:portfolioTrades)
      {
       
      String url = uri.replace("$APIKEY", token).replace("$SYMBOL", trade.getSymbol())
          .replace("$STARTDATE", trade.getPurchaseDate().toString())
          .replace("$ENDDATE", endDate.toString());
      TiingoCandle[] tiingoCandles = new RestTemplate().getForObject(url, TiingoCandle[].class);
      Double buyPrice = 0.0;
      Double sellPrice = 0.0;
      for (TiingoCandle candle : tiingoCandles) {
        if (candle.getDate().equals(trade.getPurchaseDate())) {
          buyPrice = candle.getOpen();
        }
        if (candle.getDate().equals(endDate)) {
          sellPrice = candle.getClose();
        }
      }
      AnnualizedReturn ar = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
    
        System.out.println(ar.getAnnualizedReturn());
        a.add(ar);
      
    }
    Comparator<AnnualizedReturn> byAnnualizedreturns = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn);
    Collections.sort(a, byAnnualizedreturns);
    Collections.reverse(a);
    
    return a; 
  }
  // TODO:
  // Ensure all tests are passing using below command
  // ./gradlew test --tests ModuleThreeRefactorTest

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Return the populated list of AnnualizedReturn for all stocks.
  // Annualized returns should be calculated in two steps:
  // 1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  // 1.1 Store the same as totalReturns
  // 2. Calculate extrapolated annualized returns by scaling the same in years
  // span.
  // The formula is:
  // annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  // 2.1 Store the same as annualized_returns
  // Test the same using below specified command. The build should be successful.
  // ./gradlew test --tests
  // PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args) throws Exception {
    String file = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);
    String contents = readFileAsString(file);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);

   // PortfolioManager p = PortfolioManagerFactory.getPortfolioManager();
    return PortfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
   //return p.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
    
  }
   

  private static String readFileAsString(String file) {
    
    String s = " ";
    try {
       s = Files.readString(Path.of(file));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return s;
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
   // printJsonObject(mainCalculateSingleReturn(args));

  }

  public static String getToken() {
	return "8fd469602c7c356290b606269650a07e831ffef5";
  }

public static AnnualizedReturn calculateAnnualizedReturns(LocalDate parse, PortfolioTrade trade, double buyPrice, double sellPrice) {
	      
        Double totalReturns = (sellPrice - buyPrice) / buyPrice;
        Double total_num_years = (double) ChronoUnit.DAYS.between(trade.getPurchaseDate(), parse) / 365.24;
        Double annualized_returns = Math.pow((1 + totalReturns), (1 / total_num_years)) - 1;
        AnnualizedReturn ar = new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturns);
        return ar;
}
}


 



