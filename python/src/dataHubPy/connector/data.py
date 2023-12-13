class IterableData:
    def __init__(self) -> None:
        super().__init__()
        self._data = None

    def __iter__(self):
        return self

    def __next__(self):
        return self._data
