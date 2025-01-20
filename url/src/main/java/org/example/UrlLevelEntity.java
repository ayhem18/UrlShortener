package org.example;

import java.util.List;

// credit to JetBrains IDEA automatically converting a read-only class to a Record automatically
public record UrlLevelEntity(String levelName, String pathVariable, List<String> queryParamNames,
                             List<String> queryParamValues) {

    public List<String> get(UrlEntity valueType) {
        return switch (valueType) {
            case UrlEntity.LEVEL_NAME -> List.of(this.levelName());
            case UrlEntity.PATH_VARIABLE -> List.of(this.pathVariable());
            case UrlEntity.QUERY_PARAM_VALUE -> this.queryParamValues();
            default -> this.queryParamNames();
        };
    }
}
