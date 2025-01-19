package org.example;


//import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

// credit to JetBrains IDEA automatically converting a read-only class to a Record automatically
record UrlLevelEntity(String levelName, String pathVariable, List<String> queryParamNames,
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


// this class will be used across the application and should be loaded in boot-up
// hence the @Configuration annotation
//@Configuration
public class UrlDecoder {
    private UrlLevelEntity inspectLevel(String urlLevel) {
        // this method assume teh levelUrl is not the top level url (nothing such as www.youtube.com...)

        // the algorithm is simple: a basic set of rules

        // 1. check for the ? character in the level
        //      1.a if yes: split by the ? character, the first one item is a urlLevelName
        //          1.b the rest of each item is split by the = character, this split should give 2 elements
        //              the first being the queryParameterName, the second being the query parameter value
        //      1.c if no: we know the level does not contain any queryParamNames / Values, set both fields to Null
        //          and move to step 2
        // 2. if there are non-alphabetical characters then, the level is assumed to be a pathVariable
        // 3. otherwise, it is a levelName variable

        if (urlLevel.contains("?")) {
            List<String> items = List.of(urlLevel.split("\\?"));

            List<String> qpNames = new ArrayList<>();
            List<String> qpValues = new ArrayList<>();

            for (String v : items.subList(1, items.size())) {
                // split by the "=" character
                List<String> qp = List.of(v.split("="));
                qpNames.add(qp.getFirst());
                qpValues.add(qp.get(1));
            }

            // the string right before the "?" delimiter might be either path variable or a levelName
            String levelName;
            String pathVariable;

            if (items.getFirst().matches("[a-zA-Z]+")) {
                levelName = items.getFirst();
                pathVariable = null;
            }
            else {
                pathVariable = items.getFirst();
                levelName = null;
            }

            return new UrlLevelEntity(levelName, pathVariable, qpNames, qpValues);
        }

        if (urlLevel.matches("[a-zA-Z]+")) {
            return new UrlLevelEntity(urlLevel, null, null, null);
        }

        return new UrlLevelEntity(null, urlLevel, null, null);

    }

    public List<UrlLevelEntity> breakdown(String urlString) {
        //
        String strToWorkWith;

        if (urlString.startsWith("https://")) {
            strToWorkWith = urlString.substring(0, "https://".length());
        }
        else {
            strToWorkWith = urlString.substring(0, "http://".length());
        }

        // 1. split by the "/" character (which can be only done after removing the http(s) delimiter
        List<String> levels = List.of(strToWorkWith.split("/"));

        String topLevelUrl = levels.getFirst();

        List<UrlLevelEntity> entities = levels.subList(1, levels.size()).stream().
                map(this::inspectLevel).
                toList();

        entities.addFirst(new UrlLevelEntity(topLevelUrl, null, null, null));

        return entities;
    }

}
