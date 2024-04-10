package eu.sorescu.lseg.connector;

import eu.sorescu.lseg.bo.ExchangeBo;
import eu.sorescu.lseg.bo.StockBo;
import eu.sorescu.lseg.util.LogEvent;
import eu.sorescu.lseg.util.LogLevel;
import eu.sorescu.lseg.util.SyntacticSugar;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LsegArchiveReader {
    private final Consumer<LogEvent> onEvent;
    private final List<String> fileNames;
    private ZipFile input;

    public LsegArchiveReader(Path path) throws IOException {
        this(path, null);
    }

    private final Supplier<Map<ExchangeBo, Map<StockBo, String>>> exchange2stock2entry;

    public LsegArchiveReader(Path path, Consumer<LogEvent> _onEvent) {
        this.onEvent = Optional.ofNullable(_onEvent).orElse(it -> {/*NOOP*/});
        input = SyntacticSugar.tryGet(() -> new ZipFile(path.toFile()));
        this.fileNames = input.stream().map(ZipEntry::getName).toList();
        this.exchange2stock2entry = new Supplier<>() {
            private Map<ExchangeBo, Map<StockBo, String>> CACHE = null;

            @Override
            public Map<ExchangeBo, Map<StockBo, String>> get() {
                if (CACHE == null)
                    CACHE = new HashMap<>();
                input.stream().forEach(entry -> {
                    var segments = entry.getName().split("/");
                    if (segments.length != 2) {
                        onEvent.accept(LogLevel.WARN.on("Path `" + entry.getName() + "` does not have two segments;"));
                    } else if (!segments[1].endsWith(".csv")) {
                        onEvent.accept(LogLevel.WARN.on("Path `" + entry.getName() + "` is not a CSV. Ignoring it..."));
                    } else {
                        onEvent.accept(LogLevel.SUCCESS.on("Fund: " + entry.getName()));
                        var exchange = ExchangeBo.of(segments[0]);
                        var stockName = segments[1].substring(0, segments[1].length() - 4);
                        var stock = StockBo.of(stockName);
                        CACHE.computeIfAbsent(exchange, it ->
                                new HashMap<>()
                        ).computeIfAbsent(stock, it -> entry.getName());
                    }
                });
                return CACHE;
            }
        };
    }

    public Set<ExchangeBo> getExchanges() {
        return exchange2stock2entry.get().keySet();
    }

    public Set<StockBo> getStocksFor(ExchangeBo exchange) {
        return exchange2stock2entry.get().getOrDefault(exchange, Map.of()).keySet();
    }

    public String _read(ExchangeBo exchange, StockBo stock) {
        var entryName = exchange2stock2entry.get().getOrDefault(exchange, Map.of()).getOrDefault(stock, null);
        if (entryName == null)
            onEvent.accept(LogLevel.FAIL.on(exchange + "/" + stock + " not found!"));
        try {
            return new String(input.getInputStream(input.getEntry(entryName)).readAllBytes());
        } catch (Throwable e) {
            onEvent.accept(LogLevel.ERROR.on("Cannot read " + entryName));
        }
        return null;
    }

    public Map<LocalDate, BigDecimal> getPricesFor(ExchangeBo exchange, StockBo stock) {
        var data = _read(exchange, stock);
        if(data==null){
            onEvent.accept(LogLevel.FAIL.on("Prices not found for "+exchange+"/"+stock));
            return Map.of();
        }
        var result = new HashMap<LocalDate, BigDecimal>();
        for (var line : data.split("\\v+")) {
            var cells = line.split(",");
            if (cells.length > 3)
                onEvent.accept(LogLevel.WARN.on("Too many cells: " + exchange + "/" + stock + ": " + line));
            if (cells.length < 3)
                onEvent.accept(LogLevel.ERROR.on("Not enough cells: " + exchange + "/" + stock + ": " + line));
            if (!cells[0].equals(stock.id))
                onEvent.accept(LogLevel.FAIL.on("Stock price in the wrong file: " + exchange + "/" + stock + ": " + line));
            var date = LocalDate.parse(cells[1], DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            var priceText = cells[2];
            if (!priceText.matches("-?\\d+\\.\\d{2}"))
                onEvent.accept(LogLevel.FAIL.on("Price is not [-]#+.##"));
            else if (result.containsKey(date)) {
                onEvent.accept(LogLevel.WARN.on("Duplicate price for " + date + " ignored."));
            } else {
                result.put(date, new BigDecimal(priceText));
            }
        }
        return result;
    }
}