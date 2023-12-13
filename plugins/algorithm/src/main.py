from dataHubPy.algorithm_server import flask_app

# TODO import your own algo
from clustering.dbscan import DBscan
from clustering.k_means import KMeans


def create_app():
    return flask_app

## waitress-serve --call main:create_app


if __name__ == '__main__':
    flask_app.run()

## or just use this