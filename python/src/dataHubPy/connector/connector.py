import pandas

from dataHubPy.configuration.data_item import DataItem


class Connector:

    def __init__(self):
        super().__init__()

    def init(self, info: dict):
        self.__dict__.update(info)
        return self

    def read_as_data_frame(self, data_item: DataItem) -> pandas.DataFrame:
        pass

    def write_data_frame(self, df: pandas.DataFrame, data_item: DataItem) -> None:
        pass

    def cached_file(self):
        raise Exception("Not Implement yet.")
