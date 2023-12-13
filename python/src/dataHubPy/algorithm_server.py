import base64
import tempfile

from flask import Flask, request

from dataHubPy.algorithm_executor import AlgorithmExecutor

flask_app = Flask(__name__)


@flask_app.route("/algorithms/invoke", methods=['POST'])
def invoke():
    request_json_data = request.json
    base64_yaml = request_json_data["base64_yaml"]
    with tempfile.NamedTemporaryFile(delete=False) as temp_yaml_file:
        temp_yaml_file.write(base64.b64decode(base64_yaml))
        temp_yaml_file.flush()
        algorithm_executor = AlgorithmExecutor(temp_yaml_file.name)
        try:
            info = algorithm_executor.execute()
            return {"success": True, "result": info, "message": ""}
        except Exception as e:
            return {"success": False, "result": "", "message": str(e)}


@flask_app.route("/health/liveness", methods=['GET'])
def liveness():
    return "ok"


@flask_app.route("/health/readiness", methods=['GET'])
def readiness():
    return "ok"
