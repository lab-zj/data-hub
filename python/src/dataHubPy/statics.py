import pathlib

root_path = pathlib.Path(__file__).parent.resolve()

if __name__ == '__main__':
    print(pathlib.Path().parent.parent.resolve())