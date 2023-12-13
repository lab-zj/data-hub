import string
import unittest
import pandas
import random

from dataHubPy.configuration.yaml_configuration import YamlConfiguration
from dataHubPy.connector.factory import ConnectorFactory
from dataHubPy.connector.s3_file import S3Connector


class TestS3(unittest.TestCase):
    sample = S3Connector(
        endpoint="http://10.11.33.132:9000",
        access_key="minioadmin",
        access_secret="minioadmin",
        bucket="filesystem"
    )

    def testSerialize(self):
        configuration_map = YamlConfiguration("./yaml/s3.yaml").as_map()
        connector = ConnectorFactory.wrap(configuration_map)
        self.assertTrue(connector == self.sample)

    def testIO(self):
        file_dataframe = pandas.read_csv("../data/iris.csv")
        temp_filename = ''.join(random.choice(string.ascii_letters) for i in range(10))

        temp_connector = S3Connector(
            endpoint="http://10.11.33.132:9000",
            access_key="minioadmin",
            access_secret="minioadmin",
            bucket="filesystem"
        )

        temp_connector.write_data_frame(file_dataframe, temp_filename)
        print("generated object name -> ", temp_filename)
        s3_dataframe = temp_connector.read_as_data_frame(temp_filename)

        self.assertTrue(file_dataframe.equals(s3_dataframe))
