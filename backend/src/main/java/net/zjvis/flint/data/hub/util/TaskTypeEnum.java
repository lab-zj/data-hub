package net.zjvis.flint.data.hub.util;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum TaskTypeEnum {

    Algorithm("algorithm"),
    ETL("etl"),
    SQL("sql"),
    Data("data"),
    NoteBook("notebook"),
    Clean("clean")

    ;

    private String name;

    TaskTypeEnum(String name) {
        this.name = name;
    }

    public static TaskTypeEnum of(String name) {
        return Stream.of(TaskTypeEnum.values())
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
