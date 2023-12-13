import json
import unittest

from configuration.data_item import DataItem, DataType
from dataHubPy.algorithm_executor import AlgorithmExecutor

from dataHubPy.algorithm.copy import Copy


class TestCopy(unittest.TestCase):

    def testS32S3(self):
        algorithm_executor = AlgorithmExecutor("copy_conf.yaml")
        result = algorithm_executor.execute()
        input_data = algorithm_executor.input_connector_list()[0].read_as_data_frame(
            DataItem({"name": "2/db2.csv", "type": DataType.CSV}))
        output_data = algorithm_executor.output_connector_list()[0].read_as_data_frame(
            DataItem({"name": "2/db998.csv", "type": DataType.CSV}))
        self.assertTrue(input_data.equals(output_data))

    def testPG2S3(self):
        algorithm_executor = AlgorithmExecutor("copy_conf2.yaml")
        result = algorithm_executor.execute()
        input_data = algorithm_executor.input_connector_list()[0].read_as_data_frame(
            DataItem({"name": "china_export_code_ts", "type": DataType.TABLE}))
        output_data = algorithm_executor.output_connector_list()[0].read_as_data_frame(
            DataItem({"name": "2/china_export5.csv", "type": DataType.CSV}))
        # TODO dtype will be different, especially when pg using some abnormal type
        self.assertFalse(input_data.equals(output_data))
