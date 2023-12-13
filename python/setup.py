import setuptools


with open("README.md", "r") as fh:
    long_description = fh.read()


setuptools.setup(
    name="datahub-algo-base",
    version="0.0.20",
    author="Aaron",
    author_email="byang628@zhejianglab.com",
    description="A template for algorithm node",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://gitea-ops.lab.zjvis.net/flint/data-hub",
    package_dir={"": "src"},
    packages=setuptools.find_packages(where="src"),
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    python_requires=">=3.7",
    install_requires=[
        "waitress",
        'minio',
        'numpy',
        'pandas',
        'Flask',
        'requests',
        'psycopg2',
        'PyMySQL',
        'PyYAML',
        'scikit-learn',
        'SQLAlchemy',
        'impyute'
    ]

)
