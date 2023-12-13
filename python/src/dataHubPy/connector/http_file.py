import pandas
import requests
from urllib import request

from configuration.data_item import DataItem
from dataHubPy.connector.connector import Connector


class HttpConnector(Connector):

    def __init__(
            self,
            url: str = "",
            request_type: str = "get",
            headers: dict = None,
            param: dict = None,
            policy: str = "replace",
            file_type: str = "csv"
    ) -> None:
        super().__init__()
        self.url = url
        self.request_type = request_type
        self.headers = headers
        self.param = param
        self.persist_policy = policy
        self.persist_file_type = file_type

    def get_url(self):
        return self.url

    def get_request_type(self):
        return self.request_type

    def get_param(self):
        return self.param

    def get_headers(self):
        return self.headers

    def get_persist_file_type(self):
        return self.persist_file_type

    def get_persist_policy(self):
        return self.persist_policy

    def init(self, info: dict):
        self.url = info.get("url", "")
        self.request_type = info.get("request", {}).get("type", "get")
        self.headers = info.get("request", {}).get("headers", {})
        self.param = info.get("request", {}).get("param", {})
        self.persist_policy = info.get("persist", {}).get("policy", "")
        return self

    def __eq__(self, other):
        if not isinstance(other, HttpConnector):
            return False
        return (self.get_url() == other.get_url()
                and self.get_request_type() == other.get_request_type()
                and self.get_param() == other.get_param()
                and self.get_headers() == other.get_headers()
                and self.get_persist_file_type() == other.get_persist_file_type()
                and self.get_persist_policy() == other.get_persist_policy()
                )

    def read_as_data_frame(self, data_item: DataItem) -> pandas.DataFrame:
        if not data_item.get_name() or data_item.get_name() == "":
            raise Exception("please specify a path that could be used to save http response.")
        if self.get_request_type().lower() == 'get':
            if self.get_param() is None:
                request.urlretrieve(self.get_url(), data_item.get_name())
            else:
                response = requests.get(url=self.get_url(),
                                        params=self.get_param(),
                                        headers=self.get_headers())
                print(response)
        else:
            response = requests.post(url=self.get_url(),
                                     data=self.get_param(),
                                     headers=self.get_headers())
            with open(data_item.get_name(), "wb") as f:
                f.write(response.content)

        if self.get_persist_file_type().lower() == "csv":
            return pandas.read_csv(data_item.get_name())
        elif self.get_persist_file_type().lower() == "xlsx":
            return pandas.read_excel(data_item.get_name())
        elif self.get_persist_file_type().lower() == "json":
            return pandas.read_json(data_item.get_name(), typ='series')
        else:
            raise Exception("unacceptable file type.")

    def write_data_frame(self, df: pandas.DataFrame, data_item: DataItem) -> None:
        pass
