import string
import unittest
import pandas
import random

from dataHubPy.configuration.yaml_configuration import YamlConfiguration
from dataHubPy.connector.factory import ConnectorFactory
from dataHubPy.connector.mysql import MysqlConnector


class TestMysql(unittest.TestCase):
    sample = MysqlConnector(
        host="10.105.20.64",
        port=32307,
        username="root",
        password="rPlHyxKEVp",
        database_name="nebula",
    )

    def testSerialize(self):
        configuration_map = YamlConfiguration("./yaml/mysql.yaml").as_map()
        connector = ConnectorFactory.wrap(configuration_map)
        self.assertTrue(connector == self.sample)

    def testIO(self):
        file_dataframe = pandas.read_csv("../data/iris.csv")
        random_name = ''.join(random.choice(string.ascii_letters) for i in range(10))

        temp_connector = MysqlConnector(
            host="10.105.20.64",
            port=32307,
            username="root",
            password="rPlHyxKEVp",
            database_name="nebula",
            if_write_table_exists="replace",
            need_index=False
        )

        temp_connector.write_data_frame(file_dataframe, random_name)
        print("generated table -> ", random_name)
        s3_dataframe = temp_connector.read_as_data_frame(random_name)

        self.assertTrue(file_dataframe.equals(s3_dataframe))
