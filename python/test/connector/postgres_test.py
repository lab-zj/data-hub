import string
import unittest
import pandas
import random

from dataHubPy.configuration.yaml_configuration import YamlConfiguration
from dataHubPy.connector.factory import ConnectorFactory
from dataHubPy.connector.postgresql import PostgresqlConnector


class TestPostgres(unittest.TestCase):
    sample = PostgresqlConnector(
        host="10.105.20.64",
        port=32432,
        username="postgres",
        password="QcThDsduYf",
        database_name="tbt_crawler"
    )

    def testSerialize(self):
        configuration_map = YamlConfiguration("./yaml/postgres.yaml").as_map()
        # todo, not a proper way to do this
        connector = ConnectorFactory.wrap(configuration_map)
        self.assertTrue(connector == self.sample)

    def testIO(self):
        file_dataframe = pandas.read_csv("../data/iris.csv")
        random_name = ''.join(random.choice(string.ascii_letters) for i in range(10))

        temp_connector = PostgresqlConnector(
            host="10.105.20.64",
            port=32432,
            username="postgres",
            password="QcThDsduYf",
            schema_name="public",
            database_name="tbt_crawler",
            if_write_table_exists="replace",
            need_index=False
        )

        temp_connector.write_data_frame(file_dataframe, random_name)
        print("generated table -> ", random_name)
        s3_dataframe = temp_connector.read_as_data_frame(random_name)

        self.assertTrue(file_dataframe.equals(s3_dataframe))
