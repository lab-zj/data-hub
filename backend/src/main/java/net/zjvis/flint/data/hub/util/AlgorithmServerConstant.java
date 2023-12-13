package net.zjvis.flint.data.hub.util;

public class AlgorithmServerConstant {

    public static final String KEY_JOB_CALLBACK_TYPE = "callbackType";
    public static final String KEY_JOB_CALLBACK_ALGORITHM_ID = "algorithmId";
    public static final String KEY_JOB_CALLBACK_STATUS = "status";

    public static final String KEY_VALUES_YAML_IMAGE = "image";
    public static final String KEY_VALUES_YAML_REPOSITORY = "repository";
    public static final String KEY_VALUES_YAML_TAG = "tag";
    public static final String KEY_VALUES_YAML_INGRESS = "ingress";
    public static final String KEY_VALUES_YAML_HOSTS = "hosts";

    public static final String KEY_VALUES_YAML_HOST = "host";
    public static final String KEY_VALUES_YAML_PATHS = "paths";
    public static final String KEY_VALUES_YAML_PATH = "path";

    public static final String KEY_VALUES_YAML_NODE_SELECTOR = "nodeSelector";
    public static final String KEY_VALUES_YAML_TOLERATIONS = "tolerations";
    public static final String KEY_VALUES_YAML_TLS = "tls";
    public static final String KEY_VALUES_YAML_TLS_SECRET_KEY = "secretName";

    public static final String KEY_REGISTRY = "registry";
    public static final String KEY_IMAGE_NAME = "imageName";
    public static final String KEY_IMAGE_TAG = "imageTag";

    public static final String VALUES_YAML_INGRESS_PATH_PARAM = "demo_algo_1212";


    public static final String JOB_CALLBACK_TYPE_BUILD = "build";
    public static final String JOB_CALLBACK_TYPE_DEPLOY = "deploy";
    public static final String DEFAULT_HTTP_SCHEMA = "http://";
    public static final String HTTPS_SCHEMA = "https://";
    public static final String DEFAULT_FLASK_SERVER_SUFFIX = "-flask:8080";

    public static final String ALGO_IMAGE_NAME = "ALGO_NAME";
    public static final String ALGO_VERSION = "ALGO_VERSION";
    public static final String ALGO_APP_NAME = "ALGO_NAME";
    public static final String ALGO_NAMESPACE = "ALGO_NAMESPACE";
    public static final String DOCKER_REGISTRY = "DOCKER_REGISTRY";
    public static final String BASE_PYTHON_IMAGE = "BASE_PYTHON_IMAGE";
    public static final String BASE_PYTHON_TAG = "BASE_PYTHON_TAG";


    public static final String ALGO_SRC_PATH = "ALGO_SRC_PATH";
    public static final String ALGO_SRC_PATH_SUFFIX = "code/src";
    public static final String ALGO_REQUIREMENTS_PATH = "ALGO_REQUIREMENTS_PATH";
    public static final String ALGO_REQUIREMENTS_PATH_SUFFIX = "code/requirements.txt";

    public static final String ALGO_JOB_ENV_CALLBACK_TOKEN = "ALGO_CALLBACK_TOKEN";
    public static final String ALGO_JOB_ENV_CALLBACK_ADDRESS = "ALGO_CALLBACK_ADDRESS";

    public static final String ALGO_VALUES = "ALGO_VALUES";
    public static final String ALGO_VALUES_SUFFIX = "demo.values.yaml";

    public static final String ALGO_FILE_PATH_VALUES = "algorithm/demo.values.yaml";
    public static final String ALGO_FILE_PATH_JOB_DOCKER = "algorithm/algorithms.docker.job.yaml";
    public static final String ALGO_FILE_PATH_JOB_HELM = "algorithm/algorithms.helm.job.yaml";
    public static final String ALGO_VOLUME_MOUNT = "/app/algorithms";
    public static final String DYNAMIC_SERVER_OUTER_PATH = "/algorithms";
    public static final String DYNAMIC_SERVER_CALLBACK_PATH = "/server/algorithm/v1/callback";

    public static final String CLUSTER_K8S_TOKEN_FILE_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";
    public static final String CLUSTER_K8S_MASTER_URL = "https://kubernetes.default.svc";

    public static final String NODE_SELECTOR_GPU_KEY = "beta.conti.com/gpu.name";
    public static final String NODE_SELECTOR_GPU_VALUE = "conti";
    public static final String TOLERATIONS_EFFECT_NOSCHEDULE = "NoSchedule";
    public static final String TOLERATIONS_OPERATOR_EQUAL = "Equal";

    public static final String ENV_SERVICE_ACCOUNT_NAME = "SERVICE_ACCOUNT_NAME";
    public static final String ENV_ALGORITHMS_CODE_PVC = "ALGORITHMS_CODE_PVC";
    public static final String ENV_INGRESS_TLS_ENABLE = "INGRESS_TLS_ENABLE";

    public static final String ALGO_FILE_TAR = "algorithm.tar.gz";
    public static final String ALGO_FILE_VALUES = "values.yaml";


}
