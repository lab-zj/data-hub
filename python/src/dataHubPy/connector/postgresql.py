from urllib import parse

import pandas

from configuration.data_item import DataItem
from dataHubPy.connector.database import DatabaseConnector


class PostgresqlConnector(DatabaseConnector):

    def __init__(
            self,
            host: str = None,
            port: int = None,
            username: str = None,
            password: str = None,
            schema_name: str = None,
            database_name: str = None,
            if_write_table_exists: str = "fail",
            need_index: bool = False
    ) -> None:
        super().__init__(host, port, username, password,
                         schema_name, database_name,
                         if_write_table_exists,
                         need_index)

    def _connection_string(self):
        return "postgresql+psycopg2://{}:{}@{}:{}/{}".format(
            self.get_username(),
            parse.quote_plus(self.get_password()),
            self.get_host(),
            self.get_port(),
            self.get_database_name(),
        )

    def read_as_data_frame(self, data_item: DataItem) -> pandas.DataFrame:
        if not data_item.get_name() or data_item.get_name() == "":
            raise Exception("you need to assign a table name")
        with self._connect() as connection:
            return pandas.read_sql("select * from \"{}\".\"{}\"".format(self.get_schema_name(), data_item.get_name()), connection)

