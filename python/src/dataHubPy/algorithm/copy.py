from typing import List

from configuration.data_item import DataType
from dataHubPy.algorithm.algorithm import Algorithm
from dataHubPy.connector.connector import Connector


class Copy(Algorithm):
    def __init__(self, configuration: dict) -> None:
        super().__init__(configuration)

    def _accept_input_type(self):
        self._accept_input = [DataType.CSV, DataType.TABLE]

    def _expected_output_type(self):
        self._expected_output = [DataType.TABLE]

    def run(
            self,
            connector_in_list: List[Connector],
            connector_out_list: List[Connector],
            cache_list: List[Connector]
    ) -> dict:
        connector_in = connector_in_list[0]
        connector_out = connector_out_list[0]
        data = connector_in.read_as_data_frame(self._input_data_items[0])
        connector_out.write_data_frame(data, self._output_data_items[0])
        return {"info": "executed Copy Algorithm."}
