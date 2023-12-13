### Algorithm Node Template For DataHub

## PIP install
```shell
pip install datahub-algo-base

```

## Source Install
1. go get the source file [Test-Pypi](https://test.pypi.org/project/datahub-algo-base/) [Pypi](https://pypi.org/project/datahub-algo-base/)
2. unzip datahub-algo-base-X.X.X.tar.gz
3. cd source dir **datahub-algo-base-X.X.X**
4. execute
    ```shell
    python ./setup.py build
    python ./setup.py install
    ``` 

## Build
```shell
python .\setup.py sdist bdist_wheel

## upload test.pypi.org
python -m twine upload -u __token__ -p pypi-AgENdGVzdC5weXBpLm9yZwIkZTlkMDNiNGMtZTAzMi00N2M4LTg0MTYtYmJiODNiZmU0NmE0AAIqWzMsImRkODRhMjdiLWQ2MDgtNDMyNi1hZTUyLWI4ZGM1OTgyNWM5NSJdAAAGIDqe93qMQUwOaIcx9VB60pbA9iEx-Wvuu47LTakSxAs0 --repository-url https://test.pypi.org/legacy/ dist/*

## upload pypi.org
python -m twine upload -u __token__ -p pypi-AgEIcHlwaS5vcmcCJGI5MWRjMzVhLWVmZWItNGEzYS05MjdmLWYxODVhZTk4YTczNAACKlszLCJmYTQ4NDcxMi1mNTcyLTQ1ZWQtYWQ5OC1lOWE1OTExYjgxMjIiXQAABiDmSDOT8WB_QK8sPyuD6AdzAB5M3ebEij7nAKZPcW2--g dist/*
```

## Algo Developer

1. create [src]() directory

before you create your algo, you need to install the latest base package

```shell
pip install datahub-algo-base
```

2. add [main.py]() to test and run

export all dependencies
```shell
pip freeze > requirement.txt
```

after you debug your code, you can compress code by following shell:
```shell
tar --exclude='venv' --exclude='dataHubPy*' --exclude='*idea' -cvzf ../xxxx.tar.gz .
```

and then upload package onto data hub page;