package org.url;


import java.util.ArrayList;
import java.util.List;


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

            String queryParamValuePairsString = items.get(1);

            List<String> queryParamValuePairsList = List.of(queryParamValuePairsString.split("&"));

            List<String> qpNames = new ArrayList<>();
            List<String> qpValues = new ArrayList<>();

            for (String v : queryParamValuePairsList) {
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
            // make sure to extract the substring after the protocol part of the url
            strToWorkWith = urlString.substring("https://".length());
        }
        else {
            strToWorkWith = urlString.substring("http://".length());
        }

        // 1. split by the "/" character (which can be only done after removing the http(s) delimiter
        List<String> levels = List.of(strToWorkWith.split("/"));

        String topLevelUrl = levels.getFirst();

        List<UrlLevelEntity> entities = levels.subList(1, levels.size()).stream().
                map(this::inspectLevel).
                toList();

        // make sure the 'entities' variable is mutable
        ArrayList<UrlLevelEntity> m_entities = new ArrayList<>(entities);

        m_entities.addFirst(new UrlLevelEntity(topLevelUrl, null, null, null));

        return m_entities;
    }

}
