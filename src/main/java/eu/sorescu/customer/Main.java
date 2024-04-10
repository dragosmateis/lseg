package eu.sorescu.customer;

import eu.sorescu.customer.math.AbstractExtrapolator;
import eu.sorescu.customer.math.LastExtrapolator;
import eu.sorescu.customer.math.LinearExtrapolator;
import eu.sorescu.lseg.LsegClient;
import eu.sorescu.lseg.util.LogEvent;
import eu.sorescu.lseg.util.LogLevel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Main {
    static final List<Supplier<AbstractExtrapolator>> STRATEGIES=List.of(LinearExtrapolator::new,
            LastExtrapolator::new);
    public static void main(String...args) {
        Consumer<LogEvent> myErrorHandler=event->{
            if(event.level.level<= LogLevel.SUCCESS.level) System.out.println(event);
            else System.err.println(event);
        };

        var lsegClient=new LsegClient(Config.getInstance().getInputFile().toPath(),myErrorHandler);
        var strategy=STRATEGIES.get(0);

        for(var exchange: lsegClient.getAllExchanges()){
            lsegClient.getAllStocksFor(exchange).stream().limit(Config.getInstance().getFilesLimit()).forEach(stock->{
                System.out.println(exchange+"/"+stock);
                var inputs=lsegClient.getTenDataPoints(exchange,stock);
                inputs.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(it->
                        System.out.println(it.getKey()+": "+it.getValue()));
                var outputs=lsegClient.extrapolateNextThreeDays(strategy,inputs);
                outputs.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(it->
                        System.out.println(it.getKey()+": "+it.getValue()));

                var outputFile=new File(Config.getInstance().getOutputFolder(),exchange.id+"_"+stock.id+".out.csv");
                var outputText=outputs.entrySet().stream().sorted(Map.Entry.comparingByKey())
                        .map(it->List.of(stock.id,it.getKey(),it.getValue()))
                        .map(it->it.stream().map(cell->cell.toString()).collect(Collectors.joining(",")))
                        .collect(Collectors.joining("\r\n"));
                try {
                    Files.writeString(outputFile.toPath(),outputText, StandardCharsets.US_ASCII,StandardOpenOption.CREATE_NEW);
                } catch (IOException e) {
                    System.err.println("Could not write to "+outputFile);
                    var errorFile=new File(Config.getInstance().getOutputFolder(),"errors.log");
                    try {
                        Files.writeString(errorFile.toPath(), Util.throwable2string(e),StandardOpenOption.CREATE);
                    }catch(Throwable t){}
                }
            });
        }
    }
}
