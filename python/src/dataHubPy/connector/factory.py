from typing import List

from dataHubPy.connector.connector import Connector
from dataHubPy.connector.mysql import MysqlConnector
from dataHubPy.connector.postgresql import PostgresqlConnector
from dataHubPy.connector.s3_file import S3Connector
from dataHubPy.connector.http_file import HttpConnector


class ConnectorFactory:
    def __init__(self, configuration_map: dict) -> None:
        super().__init__()
        self._configuration_map = configuration_map

    @staticmethod
    def wrap(info: dict):
        connector_type = info["type"]
        if "mysql" == connector_type:
            return MysqlConnector().init(info)
        elif "postgresql" == connector_type:
            return PostgresqlConnector().init(info)
        elif "s3" == connector_type:
            return S3Connector().init(info)
        elif "http" == connector_type:
            return HttpConnector().init(info)
        raise Exception("type({}) not support".format(connector_type))

    def input_connector_list(self) -> List[Connector]:
        if "input" not in self._configuration_map:
            return []
        input_connector_map_list = self._configuration_map["input"]
        return [ConnectorFactory.wrap(input_connector_map) for input_connector_map in input_connector_map_list]

    def output_connector_list(self) -> List[Connector]:
        if "output" not in self._configuration_map:
            return []
        output_connector_map_list = self._configuration_map["output"]
        return [ConnectorFactory.wrap(output_connector_map) for output_connector_map in output_connector_map_list]
