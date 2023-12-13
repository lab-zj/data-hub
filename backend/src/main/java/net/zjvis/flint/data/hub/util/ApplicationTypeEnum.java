package net.zjvis.flint.data.hub.util;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ApplicationTypeEnum {
    JAVA("Java", "https://resource-ops-dev.lab.zjvis.net/charts/cnconti/flask-1.0.0-C358ca08.tgz"),
    PYTHON("Python",
        "https://resource-ops-dev.lab.zjvis.net/charts/cnconti/flask-1.0.0-C358ca08.tgz"),
    ;

    private String name;
    private String helmChart;

    ApplicationTypeEnum(String name, String helmChart) {
        this.name = name;
        this.helmChart = helmChart;
    }
}
