import yaml


class YamlConfiguration:
    def __init__(self, path: str) -> None:
        with open(path, "r") as stream:
            self._configuration_map = yaml.safe_load(stream)

    def as_map(self):
        return self._configuration_map
