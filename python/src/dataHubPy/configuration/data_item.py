from enum import Enum


class DataType(Enum):
    TABLE = "table", 1
    CSV = "csv", 2
    PNG = "png", 3
    BINARY = "binary", 4
    XLSX = "xlsx", 2
    GIF = "gif", 3
    MP4 = "mp4", 4
    PDF = "pdf", 4

    def get_name(self):
        return self.value[0]

    @staticmethod
    def of(key: str):
        for item in DataType:
            if item.get_name() == key:
                return item
        raise Exception("unknown data type! acceptable type could be " + str([item.get_name() for item in DataType]))


class DataItem:
    _name: str
    _type: DataType

    def __init__(self, info: dict):
        if "name" in info.keys() and "type" in info.keys():
            self._name = info.get("name", "")
            if isinstance(info.get("type"), str):
                self._type = DataType.of(info.get("type", ""))
            elif isinstance(info.get("type"), DataType):
                self._type = info.get("type")
        else:
            raise Exception("['name', 'type'] are two required params in 'data_item'. "
                            "acceptable type could be " + str([item.get_name() for item in DataType]))

    @staticmethod
    def wrap(info):
        if isinstance(info, list):
            return [DataItem(obj) for obj in info]
        elif isinstance(info, dict):
            return [DataItem(info)]
        else:
            raise Exception("not acceptable object")

    def get_name(self):
        return self._name
