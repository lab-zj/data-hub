import pandas
import re

from minio import Minio
from io import BytesIO

from configuration.data_item import DataItem
from dataHubPy.connector.connector import Connector


class S3Connector(Connector):

    def __init__(
            self,
            endpoint: str = "",
            access_key: str = None,
            access_secret: str = None,
            bucket: str = None
    ) -> None:
        super().__init__()
        self.endpoint = endpoint
        self.access_key = access_key
        self.access_secret = access_secret
        self.bucket = bucket

    def get_endpoint(self):
        return self.endpoint

    def get_access_key(self):
        return self.access_key

    def get_access_secret(self):
        return self.access_secret

    def get_bucket(self):
        return self.bucket

    def init(self, info: dict):
        self.__dict__.update(info)
        return self

    def __eq__(self, other):
        if not isinstance(other, S3Connector):
            return False
        return (self.get_endpoint() == other.get_endpoint()
                and self.get_access_key() == other.get_access_key()
                and self.get_access_secret() == other.get_access_secret()
                and self.get_bucket() == other.get_bucket()
                )

    def read_as_data_frame(self, data_item:DataItem) -> pandas.DataFrame:
        if not data_item.get_name() or data_item.get_name() == "":
            raise Exception("file name is None")
        client = self._connect()
        if client.bucket_exists(self.get_bucket()):
            file_object = client.get_object(self.get_bucket(), data_item.get_name())
            return pandas.read_csv(file_object)
        else:
            raise Exception("bucket is not exist.")

    def write_data_frame(self, df: pandas.DataFrame, data_item: DataItem) -> None:
        if not data_item.get_name() or data_item.get_name() == "":
            raise Exception("you need to assign a output data item name")
        client = self._connect()
        if client.bucket_exists(self.get_bucket()):
            temp_csv = df.to_csv(index=False).encode("utf-8")
            client.put_object(
                self.get_bucket(),
                data_item.get_name(),
                BytesIO(temp_csv),
                length=len(temp_csv),
                content_type="application/csv"
            )
        else:
            raise Exception("bucket is not exist.")

    def _connect(self):
        secure = False
        if self.get_endpoint().startswith("https"):
            secure = True
        prefix = re.compile(r"https?://")
        return Minio(
            endpoint=prefix.sub('', self.get_endpoint()).strip().strip('/'),
            access_key=self.get_access_key(),
            secret_key=self.get_access_secret(),
            secure=secure
        )
