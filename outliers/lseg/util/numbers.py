import re


class CurrencyAmount:
    CURRENCY_VALUE_PATTERN = re.compile(r"-?\d{1,10}\.\d{2}")

    def __init__(self, value: str):
        assert CurrencyAmount.CURRENCY_VALUE_PATTERN.match(value), value
        self._value = int(value.replace('.', ''))

    def as_float(self):
        return self._value / 100

    def __str__(self):
        text = str(self._value)
        return f"{text[:-2]}.{text[-2:]}"


if __name__ == '__main__':
    testValue = CurrencyAmount('-123.45')
    print(testValue)
    CurrencyAmount("123.4")
