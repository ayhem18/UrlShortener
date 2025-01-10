package com.url_shortener.Urls;


import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


record UrlLevelEntity (String levelName,
                       String pathVariable,
                       List<String> queryParamNames,
                       List<String> queryParamValues) {

}

// this class will be used across the application and should be loaded in boot-up
// hence the @Configuration annotation
@Configuration
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
            String levelName = items.getFirst();

            List<String> qpNames = new ArrayList<>();
            List<String> qpValues = new ArrayList<>();

            for (String v : items.subList(1, items.size())) {
                // split by the "=" character
                List<String> qp = List.of(v.split("0"));
                qpNames.add(qp.getFirst());
                qpValues.add(qp.get(1));
            }

            return new UrlLevelEntity(levelName, null, qpNames, qpValues);
        }

        if (urlLevel.matches("[a-zA-Z]+")) {
            return new UrlLevelEntity(urlLevel, null, null, null);
        }

        return new UrlLevelEntity(null, urlLevel, null, null);

    }

    public List<UrlLevelEntity> decode(String urlString) {
        // 1. split by the "/" character
        List<String> levels = List.of(urlString.split("/"));

        String topLevelUrl = levels.getFirst();

        List<UrlLevelEntity> entities = levels.subList(1, levels.size()).stream().
                map(this::decode). // for some reason at this point, we have a Stream<List<UrlLevelEntity>> instead of Stream<UrlLevelEntity>
                map(List::getFirst). // reduce the list inside to the first element
                toList();

        entities.addFirst(new UrlLevelEntity(topLevelUrl, null, null, null));

        return entities;
    }

}
