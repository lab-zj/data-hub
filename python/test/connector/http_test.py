import random
import string
import unittest

from dataHubPy.configuration.yaml_configuration import YamlConfiguration
from dataHubPy.connector.factory import ConnectorFactory
from dataHubPy.connector.http_file import HttpConnector


class TestHTTP(unittest.TestCase):
    sample_without_param = HttpConnector(
        url="10.105.20.64",
        request_type="get",
        headers={},
        policy="replace"
    )

    sample_with_param = HttpConnector(
        url="http://test_url:80282",
        request_type="get",
        headers={
            "Content-Type": "application/x-www-form-urlencoded"
        },
        param={
            'a': 'val_a',
            'b': ['b1', 'b2', 'b3'],
            'c': 123
        },
        policy="replace",
        file_type="csv"
    )

    def testSerialize(self):
        configuration_map = YamlConfiguration("./yaml/http.yaml").as_map()
        connector = ConnectorFactory.wrap(configuration_map)
        self.assertTrue(connector == self.sample_with_param)

    def testReadXLSX(self):
        random_name = ''.join(random.choice(string.ascii_letters) for i in range(10))
        xlsx = HttpConnector(
            url="https://eping.wto.org/NotificationExcelFiles/Notification_EN.xlsx",
            request_type="get",
            policy="replace",
            file_type="xlsx"
        ).read_as_data_frame(random_name + ".xlsx")

        print(xlsx.columns.values.tolist())

    def testReadJson(self):
        random_name = ''.join(random.choice(string.ascii_letters) for i in range(10))
        json = HttpConnector(
            url="http://ops-test-08.lab.zjvis.net:18083/gac/task/list?filterRunning=false",
            request_type="get",
            policy="replace",
            file_type="json"
        ).read_as_data_frame(random_name + ".json")

        print(json.head(2))

    def testPostJson(self):
        random_name = ''.join(random.choice(string.ascii_letters) for i in range(10))
        json = HttpConnector(
            url="http://ops-test-08.lab.zjvis.net:18083/gac/task/cancel",
            request_type="post",
            headers={
                "Content-Type": "application/json"
            },
            param={
                "key": "task_1691976031462_GsiTqEtr"
            },
            policy="replace",
            file_type="json"
        ).read_as_data_frame(random_name + ".json")

        print(json)
