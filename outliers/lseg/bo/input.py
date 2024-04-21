import datetime
import pathlib
import re
import os
from pathlib import Path

import lseg.util.numbers


class DataPoint:
    def __init__(self, ticker: str, date: datetime.date, price: lseg.CurrencyAmount):
        self.ticker = ticker
        self.date = date
        self.price = price

    def __str__(self):
        return f"{self.ticker}@{self.date}:{self.price}"

    def __repr__(self):
        return f"{self.__class__.__qualname__}({self.ticker},{self.date},{self.price})"

    @classmethod
    def parse(cls, line):
        values = line.split(",")
        ticker = values[0]
        y = int(values[1].split("-")[2])
        m = int(values[1].split("-")[1])
        d = int(values[1].split("-")[0])
        date = datetime.date(y, m, d)
        price = lseg.CurrencyAmount(values[2])
        return DataPoint(ticker, date, price)


exchange_re = re.compile(r"[A-Z]+")
ticker_re = re.compile(r"([A-Z]+)\.csv")


class Ticker:
    def __init__(self, ticker_file: pathlib.Path):
        assert ticker_file.is_file(), ticker_file
        assert ticker_re.match(ticker_file.name), ticker_file
        self._file = ticker_file
        self.name = ticker_re.match(self._file.name)[1]

    def __str__(self):
        return f"{self.__class__.__name__}({self.name})"

    def get_data_points(self) -> list[DataPoint]:
        with open(self._file) as handler:
            result = [DataPoint.parse(it) for it in handler.readlines()]
            assert not any([it for it in result if it.ticker != self.name]), f"Ticker mismatch in {self._file}"
            return result


class StockExchange:
    def __init__(self, exchange_folder: pathlib.Path):
        assert exchange_folder.is_dir(), exchange_folder
        assert exchange_re.match(exchange_folder.name), exchange_folder
        self._folder = exchange_folder
        self.name = self._folder.name

    def __str__(self):
        return f"{self.__class__.__name__}({self.name})"

    def _get_ticker_file_names(self) -> list[str]:
        return [it for it in os.listdir(self._folder) if self._folder.joinpath(it).is_file() and ticker_re.match(it)]

    def get_tickers(self) -> list[Ticker]:
        return [Ticker(self._folder.joinpath(it)) for it in self._get_ticker_file_names()]


class InputRoot:
    def __init__(self, root: pathlib.Path):
        assert root.is_dir(), root
        self._root = root

    def _get_exchange_files(self) -> list[Path]:
        return [file for file
                in [self._root.joinpath(name) for name in os.listdir(self._root) if exchange_re.match(name)]
                if file.is_dir()]

    def get_exchanges(self) -> list[StockExchange]:
        return [StockExchange(it) for it in self._get_exchange_files()]
