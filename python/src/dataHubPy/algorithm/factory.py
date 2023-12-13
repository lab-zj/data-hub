from dataHubPy.algorithm.algorithm import Algorithm


class AlgorithmFactory:
    def __init__(self, configuration_map: dict) -> None:
        super().__init__()
        self._configuration_map = configuration_map

    def algorithm(self) -> Algorithm:
        if self._configuration_map is None or self._configuration_map.get("type", "") == "":
            raise Exception("algorithm is None")
        return self._extract_algorithm()

    def _extract_algorithm(self):
        all_available_algos = {clazz.__name__: clazz for clazz in Algorithm.__subclasses__()}
        print("available algorithms : {}".format(all_available_algos))
        if self._configuration_map['type'] in all_available_algos.keys():
            selected_algo = all_available_algos[self._configuration_map['type']]
            return selected_algo(self._configuration_map)
        else:
            raise Exception("algorithm type({}) not support".format(self._configuration_map['type']))

