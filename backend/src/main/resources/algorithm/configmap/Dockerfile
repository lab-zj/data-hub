ARG BASE_PYTHON_REGISTRY=docker.io
ARG BASE_PYTHON_IMAGE=python
ARG BASE_PYTHON_TAG=3.9.14-bullseye
FROM ${BASE_PYTHON_REGISTRY}/${BASE_PYTHON_IMAGE}:${BASE_PYTHON_TAG}

ENV TZ=Asia/Shanghai

RUN useradd flask --create-home

COPY --chown=flask entry-point.sh /app/bin/
COPY --chown=flask requirements.txt /tmp/requirements.txt
COPY --chown=flask src/ /app/

RUN pip install -r /tmp/requirements.txt -i https://devpi-ops.lab.zjvis.net:32443/root/pypi \
    && chmod u+x /app/bin/entry-point.sh

WORKDIR /app
USER flask
ENV FLASK_APP=app
CMD ["/app/bin/entry-point.sh"]
