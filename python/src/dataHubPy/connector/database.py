import pandas

import sqlalchemy

from dataHubPy.configuration.data_item import DataItem
from dataHubPy.connector.connector import Connector


class DatabaseConnector(Connector):

    def __init__(
            self,
            host: str,
            port: int,
            username: str,
            password: str,
            schema_name: str,
            database_name: str,
            if_write_table_exists: str = "fail",
            need_index: bool = False
    ) -> None:
        super().__init__()
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        self.schema_name = schema_name
        self.database_name = database_name
        self.if_write_table_exists = if_write_table_exists
        self.need_index = need_index

    def read_as_data_frame(self, data_item: DataItem ) -> pandas.DataFrame:
        with self._connect() as connection:
            return pandas.read_sql("select * from {}.{}".format(self.get_schema_name(), data_item.get_name()), connection)

    def write_data_frame(self, data: pandas.DataFrame, data_item: DataItem) -> None:
        if not data_item.get_name() or data_item.get_name() == "":
            raise Exception("you need to assign a output data item name")
        with self._connect() as connection:
            data.to_sql(name=data_item.get_name(), con=connection,
                        if_exists=self.get_if_write_table_exists(),
                        chunksize=10000,
                        index=self.get_need_index()
                        )

    def _connect(self):
        sql_engine = sqlalchemy.create_engine(self._connection_string())
        return sql_engine.connect()

    def _connection_string(self):
        raise Exception("not implement")

    def get_host(self):
        return self.host

    def get_port(self):
        return self.port

    def get_username(self):
        return self.username

    def get_password(self):
        return self.password

    def get_schema_name(self):
        return self.schema_name

    def get_database_name(self):
        return self.database_name

    def get_if_write_table_exists(self):
        return self.if_write_table_exists

    def get_need_index(self):
        return self.need_index

    def __eq__(self, other):
        if not isinstance(other, DatabaseConnector):
            return False
        return (self.get_host() == other.get_host()
                and self.get_port() == other.get_port()
                and self.get_username() == other.get_username()
                and self.get_password() == other.get_password()
                and self.get_schema_name() == other.get_schema_name()
                and self.get_database_name() == other.get_database_name()
                and self.get_if_write_table_exists() == other.get_if_write_table_exists()
                and self.get_need_index() == other.get_need_index()
                )
