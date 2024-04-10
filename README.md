# Simple XCHG price prognosis tool
The code is written on Java 22 (hence, you might face issues with previous Java versions).
No additional library is used (neither Spring, or any AoP decorator library).

In the next days I'll try to redo it also in other languages (Groovy, Python, Octave, etc.).
Most probably not as guarded against errors as this code.

## Code parts
Code is split int two parts:
* Package `eu.sorescu.lseg` contains the `LSEG` libary.
* Package `eu.sorescu.customer` contains a proposed implementation solution.

`LSEG` library contains `LsegClient` class with two methods: `getTenDataPoints` and `extrapolateNextThreeDays`.
A subpackage (`eu.sorescu.lseg.bo`) contains business object classes (just two classes, one for representing
the exchange, another for representing the stock).
A stock does not have a reference back to exchange, given that a stock
(from customer's perspective) may be traded on more exchanges.

For the sake exposing various mechanisms, the code did not follow a clear structure or approach,
but it rather is eclectic.

Much code is using the collection streams, written on the `Write-Once-Read-Never` philosophy (of course,
lots of unit tests would be written to ensure quality).

### LSEG code base
`LsegClient` is using inversion of control for logging the events,
allowing the customer code whether they want to abort or not at given failures.
The log event is just a simple log-level and text message record.

`LsegClient` is relying on `LsegArchiveReader` that is reading the file ZIP
and can respond with (cached) list of exchanges, stocks, and prices.

Each data point consists of a pair (`LocalDate` and `BigDecimal` value).
Various validations are performed:
- two prices must not exist for the same date;
- the price must be a number with maximum 2 decimals;
- the stock id from CSV must match the file name (except `.csv` extension);
- etc;
  In general, all these events are sent back through IoC and ignored (unless the customer throws exception in callback).

### Customer code base
Customer is initiating an `LsegClient` by supplying it the path to the input archive,
and an events' handler.

The file (if it does not exist) is chosen by human intervention (using the AWT package).

For each of the exchanges that is reported by `LsegClient`,
the customer code is fetching a limited number of stocks (calculated and cached in the Configuration utility)
and generating the trend values (for the next three days) and exports them to CSV.

## Notes and limitations
Multiton pattern for `ExchangeBo` and `StockBo` was chosen so that the heap does not get polluted with
duplicate logically-equal objects, and to avoid (as of now) the hash/equals mechanism.

I did not clean up or autoformat the code.

I did not add the extrapolator strategy logic in `LsegClient`, so that customer can use the same client
with different strategies. I did not elaborate a more complex client-strategy tuple.

CSV parsing/writing assumes there's no escaping issue.

**Customer** code is not guarded against errors as much as **LSEG** is.

## How to start the application
No maven/gradle is required, simple java compilation or import should work fine.

`Main` class is triggering the `eu.sorescu.customer.Main` class (no module or package configuration is added to expose it).

The script is reading the input file and, for each exchange, and for a maximum of stocks,
a `<xchg>_<stock>.out.csv` file is generated with the next three days.
For the sake of simplification, the output files are saved in the same folder (`input-files`).

The extrapolation algorithm is using the linear interpolation (closest line to all datapoints).
The algorithm takes in consideration the local dates with their meanings (it allows for skipped days in range).

The extrapolated next three days are the three successor `LocalDate`s of the latest date in input dataset.
