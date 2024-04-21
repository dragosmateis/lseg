import functools
import math
import pathlib
import random

import lseg

input_folder = pathlib.Path(__file__).joinpath('../data/stock_price_data_files').resolve().absolute()
output_folder = pathlib.Path(__file__).joinpath('../data/output').resolve().absolute()
client = lseg.LsegClient(input_folder)


def get_30_consecutive_datapoints(exchange_id: str, ticker_id: str) -> list[lseg.DataPoint]:
    '''The first requested function'''
    exchange = [*[it for it in client.get_exchanges() if it.name == exchange_id], None][0]
    assert exchange is not None, f"Exchange {exchange_id} not found."
    ticker = [*[it for it in exchange.get_tickers() if it.name == ticker_id], None][0]
    assert ticker is not None, f"{exchange} ticker {ticker_id} not found."

    data_points = sorted(ticker.get_data_points(), key=lambda it: it.date, reverse=False)

    items_to_drop = len(data_points) - 30
    assert items_to_drop >= 0, f"Data set for {exchange_id}:{ticker_id} must have at least 30 items."

    start = random.Random().randint(0, items_to_drop)
    return data_points[start:(start + 30)]


class Statistics:
    def __init__(self, dp: list[lseg.DataPoint]):
        self._dp = dp
        self.count = len(dp)
        self._float_values = [it.price.as_float() for it in self._dp]
        self.mean = sum(self._float_values) / self.count
        delta = [it - self.mean for it in self._float_values]
        self.std = math.sqrt(sum([it * it for it in delta]) / self.count)

    @functools.lru_cache(30)
    def x_sigma(self, dp: lseg.DataPoint):
        return math.fabs(dp.price.as_float() - self.mean) / self.std


def get_outliers(dp: list[lseg.DataPoint]) -> tuple[list[lseg.DataPoint], Statistics]:
    '''The first requested function.
    It returns also the stats in one call.'''
    _stats = Statistics(dp)
    _outliers = [it for it in dp if _stats.x_sigma(it) >= 2]
    return _outliers, _stats


def outlier_to_output_values(outlier: lseg.DataPoint, stats: Statistics) -> list[str]:
    ticker = outlier.ticker
    date=outlier.date.strftime("%d-%m-%Y")
    price = str(outlier.price)
    mean = stats.mean
    x = stats.x_sigma(outlier)
    proc = math.erf(x / math.sqrt(2))
    threshold_p = math.erf(2 / math.sqrt(2))
    # TODO: is the deviation over and above threshold calculated correctly? To confirm with Data Science team.
    deviation_over_and_above_threshold = proc - threshold_p
    return [str(it) for it in [ticker, date, price, mean, deviation_over_and_above_threshold]]


def outliers_to_csv_lines(outliers: list[lseg.DataPoint], stats: Statistics) -> list[str]:
    return [','.join(outlier_to_output_values(outlier, stats)) for outlier in outliers]


if __name__ == '__main__':
    if not output_folder.exists():
        output_folder.mkdir(parents=True)
    for exchange in client.get_exchanges():
        print(exchange)
        for ticker in exchange.get_tickers():
            print(ticker)
            data_points = get_30_consecutive_datapoints(exchange.name, ticker.name)
            outliers, stats = get_outliers(data_points)
            lines = outliers_to_csv_lines(outliers,stats)
            with open(pathlib.Path(output_folder).joinpath(f"{exchange.name}_{ticker.name}.csv"), 'wt') as handler:
                for line in lines:
                    handler.write(line)
                    handler.write("\r\n") # RFC4180: CSV new line is CRLF
