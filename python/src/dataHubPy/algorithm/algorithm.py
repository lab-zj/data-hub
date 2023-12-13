from typing import List

from dataHubPy.configuration.data_item import DataItem, DataType
from dataHubPy.connector.connector import Connector


class Algorithm:
    _registry = {}

    def __init__(self, configuration: dict) -> None:
        super().__init__()
        self._params = configuration.get("params", {})
        if len(self._params) > 0:
            self._input_data_items = DataItem.wrap(self._params.get("input", []))
            self._output_data_items = DataItem.wrap(self._params.get("output", []))
        else:
            raise Exception("configuration yaml need have a property 'params' under 'algorithm'.")

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        cls._registry[cls.__name__] = cls

    def run(
            self,
            connector_in_list: List[Connector],
            connector_out_list: List[Connector],
            connector_cache_list: List[Connector]
    ) -> dict:
        raise Exception("not implement")

    def list_registry(self):
        return self._registry

    def check_validation(self, input_list, output_list):

        if (len(self._input_data_items) != len(input_list)
                or len(self._output_data_items) != len(output_list)):
            raise Exception("The quantities of 'data_item' and 'connector' do not match")

    def _accept_input_type(self):
        self._accept_input = []

    def _expected_output_type(self):
        self._expected_output = []