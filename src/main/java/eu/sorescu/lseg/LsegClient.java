package eu.sorescu.lseg;

import eu.sorescu.customer.math.AbstractExtrapolator;
import eu.sorescu.lseg.bo.ExchangeBo;
import eu.sorescu.lseg.bo.StockBo;
import eu.sorescu.lseg.connector.LsegArchiveReader;
import eu.sorescu.lseg.util.LogEvent;
import eu.sorescu.lseg.util.LogLevel;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LsegClient {
    private final Path inputPath;
    private final Consumer<LogEvent> onEvent;
    private LsegArchiveReader lar=null;

    public LsegClient(Path inputPath, Consumer<LogEvent> onEvent){
        this.inputPath=inputPath;
        this.onEvent=(onEvent==null)?it->{/*NOOP*/}:onEvent;
        if(!inputPath.toFile().exists())
            this.onEvent.accept(LogLevel.WARN.on("File "+inputPath+" not found. It will fail later, when fetching."));
    }
    private LsegArchiveReader getArchiveReader(){
        if(this.lar==null)
            this.lar=new LsegArchiveReader(inputPath,onEvent);
        return this.lar;
    }
    public Set<ExchangeBo> getAllExchanges(){
        return getArchiveReader().getExchanges();
    }
    public Set<StockBo> getAllStocksFor(ExchangeBo exchange){
        return getArchiveReader().getStocksFor(exchange);
    }
    private Map<LocalDate, BigDecimal> getAllPricesFor(ExchangeBo exchange, StockBo stock){
        return getArchiveReader().getPricesFor(exchange,stock);
    }
    private static Optional<LocalDate> getRandomDate(Collection<LocalDate>localDates){
        if(localDates.isEmpty())
            return Optional.empty();
        else {
            // it would be faster to return localDatesList.get(trunc(Math.IntRandom(0,length));
            var localDatesList = new ArrayList<>(localDates);// we need to ensure the list is mutable
            Collections.shuffle(localDatesList);// shuffle in place
            return Optional.of(localDatesList.getFirst());
        }
    }
    private Stream<Map.Entry<LocalDate, BigDecimal>> getDPSortedByDates(ExchangeBo exchange, StockBo stock, int limit){
        // be lazy and return a late-evaluated stream
        var dataSet= getAllPricesFor(exchange,stock);
        onEvent.accept(LogLevel.DEBUG.on("getDates: "+exchange+"/"+stock+" found:"+dataSet.keySet()));
        var randomDate=getRandomDate(dataSet.keySet());
        onEvent.accept(LogLevel.DEBUG.on("getDates: "+exchange+"/"+stock+" randomly picked:"+randomDate));
        if(randomDate.isEmpty())
            onEvent.accept(LogLevel.WARN.on("No data points found for: "+exchange+"/"+stock));
        if(randomDate.isPresent())
            return dataSet.entrySet().stream()
                    .filter(it->!it.getKey().isBefore(randomDate.get()))
                    .sorted(Map.Entry.comparingByKey())
                    .limit(limit);
        else return Stream.of();
    }
    public Map<LocalDate, BigDecimal> getTenDataPoints(ExchangeBo exchange, StockBo stock){
        // Yes, I know, this method should also return a lazy stream, instead of .toList()
        var data= getDPSortedByDates(exchange,stock,10).toList();
        onEvent.accept(LogLevel.DEBUG.on("Ten data points: "+data));
        return data.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private AbstractExtrapolator buildExtrapolator(Supplier<AbstractExtrapolator> strategy, Map<LocalDate, BigDecimal>inputs){
        var extrapolator=strategy.get();
        for(var input: inputs.entrySet())
            extrapolator=extrapolator.with(input.getKey().toEpochDay(),input.getValue().doubleValue());
        return extrapolator;
    }
    private Map<LocalDate,Number> extrapolate(Supplier<AbstractExtrapolator>strategy,Map<LocalDate, BigDecimal>inputs,List<LocalDate>points){
        var minDay=inputs.keySet().stream().mapToLong(LocalDate::toEpochDay).min();
        var extrapolator=buildExtrapolator(strategy,inputs);
        return points.stream().collect(Collectors.toMap(it->it,
                it-> extrapolator.extrapolateAt(it.toEpochDay())));
    }
    public Map<LocalDate,Number>extrapolateNextThreeDays(Supplier<AbstractExtrapolator>strategy,Map<LocalDate, BigDecimal>inputs){
      var lastDay=inputs.keySet().stream().max(LocalDate::compareTo).get();
      var nextThreeDays= IntStream.range(1,4).mapToObj(lastDay::plusDays).toList();
      onEvent.accept(LogLevel.DEBUG.on("NEXT Three days: "+nextThreeDays));
        var extrapolation=extrapolate(strategy,inputs,nextThreeDays);
      onEvent.accept(LogLevel.DEBUG.on("Extrapolation: "+extrapolation));
      // DO NOT return BigDecimal, let the caller convert - if needed
      return extrapolation;
    };
}