from typing import List
from sklearn.cluster import DBSCAN
from dataHubPy.algorithm.algorithm import Algorithm
from dataHubPy.connector.connector import Connector


class DBscan(Algorithm):
    def __init__(self, configuration: dict) -> None:
        super().__init__(configuration)
        self._cols = configuration["cols"]
        self._eps: float = float(configuration["eps"])
        self._min_samples: int = int(configuration["min_samples"])
        self._metric = configuration["metric"]
        self._output_column_name = configuration.get("output_column_name", "_cluster_id_")

    def run(
            self,
            connector_in_list: List[Connector],
            connector_out_list: List[Connector],
            cache_list: List[Connector],
    ) -> dict:
        connector_in = connector_in_list[0]
        connector_out = connector_out_list[0]
        data = connector_in.read_as_data_frame(self._input_data_item)
        data = data.dropna()
        ml_model = DBSCAN(
            eps=self._eps,
            min_samples=self._min_samples,
            metric=self._metric
        ).fit(data[self._cols].values)
        pred = ml_model.labels_
        data[self._output_column_name] = pred.astype("int")
        connector_out.write_data_frame(data, self._output_data_item)
        return {"success": True}
