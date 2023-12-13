from urllib import parse

import pandas

from dataHubPy.configuration.data_item import DataItem
from dataHubPy.connector.database import DatabaseConnector


class MysqlConnector(DatabaseConnector):

    def __init__(
            self,
            host: str = None,
            port: int = None,
            username: str = None,
            password: str = None,
            database_name: str = None,
            if_write_table_exists: str = "fail",
            need_index: bool = False
    ) -> None:
        super().__init__(host, port, username, password, "", database_name,
                         if_write_table_exists,
                         need_index)

    def _connection_string(self):
        return "mysql+pymysql://{}:{}@{}:{}/{}".format(
            self.get_username(),
            parse.quote_plus(self.get_password()),
            self.get_host(),
            self.get_port(),
            self.get_database_name(),
        )

    def read_as_data_frame(self, data_item: DataItem) -> pandas.DataFrame:
        with self._connect() as connection:
            return pandas.read_sql("select * from {}".format(data_item.get_name()), connection)
