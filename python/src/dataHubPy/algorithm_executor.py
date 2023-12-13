import json
from typing import List

from dataHubPy.algorithm.factory import AlgorithmFactory
from dataHubPy.configuration.yaml_configuration import YamlConfiguration
from dataHubPy.connector.connector import Connector
from dataHubPy.connector.factory import ConnectorFactory
from dataHubPy.utils.format_util import NpEncoder


class AlgorithmExecutor:
    def __init__(self, config_path: str) -> None:
        super().__init__()
        configuration_map = YamlConfiguration(config_path).as_map()
        connector_factory = ConnectorFactory(configuration_map["connector"])
        self._input_connector_list = connector_factory.input_connector_list()
        self._output_connector_list = connector_factory.output_connector_list()
        algorithm_factory = AlgorithmFactory(configuration_map["algorithm"])
        self._algorithm = algorithm_factory.algorithm()

    def input_connector_list(self) -> List[Connector]:
        return self._input_connector_list

    def output_connector_list(self) -> List[Connector]:
        return self._output_connector_list

    def execute(self) -> str:
        self._algorithm.check_validation(self._input_connector_list, self._output_connector_list)
        result = self._algorithm.run(self._input_connector_list, self._output_connector_list, [])
        return json.dumps(result, ensure_ascii=False, cls=NpEncoder)
