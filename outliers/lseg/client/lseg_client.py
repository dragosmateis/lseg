import pathlib

import lseg.bo.input
from lseg import StockExchange


class LsegClient:
    def __init__(self, root: pathlib.Path):
        assert root.exists(), root
        assert root.is_dir(), root
        self._input = lseg.InputRoot(root)

    def get_exchanges(self) -> list[StockExchange]:
        return self._input.get_exchanges()
