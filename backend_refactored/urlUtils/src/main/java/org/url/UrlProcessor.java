package org.url;


import org.utils.CustomGenerator;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class UrlProcessor {

    private final CustomGenerator customGenerator;

    public UrlProcessor(CustomGenerator customGenerator) {
        this.customGenerator = customGenerator;
    }

    private UrlLevelEntity inspectPathSegment(String urlLevel) {
        // this method assume teh levelUrl is not the top level url (nothing such as www.youtube.com...)

        // the algorithm is simple: a basic set of rules

        // 1. check for the ? character in the level
        //      1.a if yes: split by the ? character, the first one item is a urlLevelName
        //          1.b the rest of each item is split by the = character, this split should give 2 elements
        //              the first being the queryParameterName, the second being the query parameter value
        //      1.c if no: we know the level does not contain any queryParamNames / Values, set both fields to Null
        //          and move to step 2
        // 2. if there are non-alphabetical (and not _ or -) characters then, the level is assumed to be a pathVariable

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

            if (items.getFirst().matches("[a-zA-Z_-]+")) {
                levelName = items.getFirst();
                pathVariable = null;
            }
            else {
                pathVariable = items.getFirst();
                levelName = null;
            }

            return new UrlLevelEntity(levelName, pathVariable, qpNames, qpValues);
        }

        if (urlLevel.matches("[a-zA-Z_-]+")) {
            return new UrlLevelEntity(urlLevel, null, null, null);
        }

        return new UrlLevelEntity(null, urlLevel, null, null);

    }

    public List<UrlLevelEntity> breakdown(String urlString) {
        // step1: extract the protocol schema
        // step2: extract the top level Domain 
        // split the rest of the url by the "/" character 
        // for each item in the list call the inspectLevel method
        // return protocol (as urlEntity) + topLevelDomain (as urlEntity) + list of UrlLevelEntity 

        int protocolEndIndex = urlString.indexOf("://"); 

        if (protocolEndIndex == -1) {
            throw new RuntimeException("The passed url does not contain the protocol delimiter");
        }

        String protocol = urlString.substring(0, protocolEndIndex + 3);

        String strToWorkWith = urlString.substring(protocolEndIndex + 3);

        // 1. split by the "/" character
        List<String> levels = List.of(strToWorkWith.split("/"));

        String topLevelUrl = levels.getFirst();

        List<UrlLevelEntity> entities = levels.subList(1, levels.size()).stream().
                map(this::inspectPathSegment).
                toList();

        // make sure the 'entities' variable is mutable
        ArrayList<UrlLevelEntity> m_entities = new ArrayList<>(entities);

        m_entities.addFirst(new UrlLevelEntity(topLevelUrl, null, null, null));

        // add the protocol to the list
        m_entities.addFirst(new UrlLevelEntity(protocol, null, null, null));

        return m_entities;
    }


    public String encode(
                    String urlString, 
                    String encodedUrlPrefix,
                    String topLevelDomainHash, 
                    List<Map<String, String>> encodedData,
                    List<Map<String, String>> decodedData,
                    int minVariableLength,
                    int minParameterLength) {
        
        // make sure the prefix ends with a "/" character
        if (!encodedUrlPrefix.endsWith("/") && !encodedUrlPrefix.isEmpty()) {
            throw new IllegalArgumentException("The prefix must be either empty or ends with a '/' character");
        }

        // 1. Breakdown the urlString into a list of UrlLevelEntity
        List<UrlLevelEntity> urlLevels = breakdown(urlString);

        // Check if we have at least protocol and domain
        if (urlLevels.size() < 2) {
            throw new IllegalArgumentException("Invalid URL structure: missing protocol or domain");
        }

        // 2. Build the encoded URL
        StringBuilder encodedUrl = new StringBuilder();

        // Keep the protocol as is (first element)
        String protocol = urlLevels.getFirst().levelName();
        encodedUrl.append(protocol);

        encodedUrl.append(encodedUrlPrefix);

        // Use the provided hash for the domain (second element)
        encodedUrl.append(topLevelDomainHash);

        // Process remaining path segments
        for (int i = 2; i < urlLevels.size(); i++) {
            UrlLevelEntity currentLevel = urlLevels.get(i);

            // Current segment index in our encoding data is (i-2) because we skip protocol and domain
            int segmentIndex = i - 2;

            // Make sure encodedData has enough entries
            while (encodedData.size() <= segmentIndex) {
                encodedData.add(new HashMap<>());
                decodedData.add(new HashMap<>());
            }

            Map<String, String> currentEncodedMap = encodedData.get(segmentIndex);
            Map<String, String> currentDecodedMap = decodedData.get(segmentIndex);

            // Add path separator
            encodedUrl.append("/");

            // Handle the path segment based on its type
            if (currentLevel.levelName() != null) {
                // This is a named level (like "users", "profile", etc.)
                String levelName = currentLevel.levelName();

                if (currentEncodedMap.containsKey(levelName)) {
                    // We already have an encoding for this segment
                    encodedUrl.append(currentEncodedMap.get(levelName));
                } else {
                    // if the levelName is not in the encodedMap, we first check whether the levelName is long enough
                    if (levelName.length() < minParameterLength) {
                        encodedUrl.append(levelName);
                    } else {
                        // at this point, we know that levelName is long enough to be encoded and saved in the encodedMap

                        String encodedSegment = customGenerator.generateId(currentEncodedMap.size());

                        // Store in both maps
                        currentEncodedMap.put(levelName, encodedSegment);
                        currentDecodedMap.put(encodedSegment, levelName);

                        encodedUrl.append(encodedSegment);

                    }
                }

            }

            else if (currentLevel.pathVariable() != null) {
                // This is a path variable (like IDs, etc.)
                String pathVar = currentLevel.pathVariable();

                if (currentEncodedMap.containsKey(pathVar)) {
                    // We already have an encoding for this variable
                    encodedUrl.append(currentEncodedMap.get(pathVar));
                } else {
                    // Ensure minimum length
                    if (pathVar.length() < minVariableLength) {
                        encodedUrl.append(pathVar);
                    }
                    else{
                        // Generate a new encoding
                        String encodedVar = customGenerator.generateId(currentEncodedMap.size());


                        // Store in both maps
                        currentEncodedMap.put(pathVar, encodedVar);
                        currentDecodedMap.put(encodedVar, pathVar);

                        encodedUrl.append(encodedVar);
                    }
                }
            }

            // Handle query parameters if present
            if (currentLevel.queryParamNames() != null && !currentLevel.queryParamNames().isEmpty()) {
                encodedUrl.append("?");

                for (int j = 0; j < currentLevel.queryParamNames().size(); j++) {
                    String paramName = currentLevel.queryParamNames().get(j);
                    String paramValue = currentLevel.queryParamValues().get(j);

                    // Add parameter separator if not the first param
                    if (j > 0) {
                        encodedUrl.append("&");
                    }

                    // Handle parameter name
                    if (currentEncodedMap.containsKey(paramName)) {
                        encodedUrl.append(currentEncodedMap.get(paramName));
                    } else {
                        // if the paramName is not in the encodedMap, we first check whether the paramName is long enough
                        if (paramName.length() < minParameterLength) {
                            encodedUrl.append(paramName);
                        } else {
                            // at this point, we know that paramName is long enough to be encoded and saved in the encodedMap

                            // Generate a new encoding
                            String encodedParam = customGenerator.generateId(currentEncodedMap.size());

                            // Store in both maps with prefix to avoid collision with path segments
                            currentEncodedMap.put(paramName, encodedParam);
                            currentDecodedMap.put(encodedParam, paramName);

                            // add the encoding of the query parameter name to the encodedUrl
                            encodedUrl.append(encodedParam);
                        }
                    }

                    encodedUrl.append("=");

                    // Handle parameter value
                    if (currentEncodedMap.containsKey(paramValue)) {
                        encodedUrl.append(currentEncodedMap.get(paramValue));
                    } else {
                        // if the paramValue is not in the encodedMap, we first check whether the paramValue is long enough
                        if (paramValue.length() < minVariableLength) {
                            encodedUrl.append(paramValue);
                        } else {
                            // at this point, we know that paramValue is long enough to be encoded and saved in the encodedMap
                            String encodedValue = customGenerator.generateId(currentEncodedMap.size());

                            // Store in both maps with prefix to avoid collision
                            currentEncodedMap.put(paramValue, encodedValue);
                            currentDecodedMap.put(encodedValue, paramValue);

                            // add the encoding of the query parameter value to the encodedUrl
                            encodedUrl.append(encodedValue);
                        }
                    }
                }
            }

        }

        return encodedUrl.toString();
    }

    /**
     * Decodes an encoded URL back to its original form using the stored mapping data
     * @param encodedUrl The shortened/encoded URL to decode
     * @param decodedData The mapping from encoded segments to original segments
     * @return The original URL
     * @throws IllegalArgumentException if the URL cannot be decoded
     */
    public String decode(String encodedUrl, String originalTopLevelDomain, String encodedUrlPrefix, List<Map<String, String>> decodedData) {
        // Check if there's any data to decode with
        if (decodedData == null || decodedData.isEmpty()) {
            throw new IllegalArgumentException("No decoding data available");
        }
        
        // Break down the encoded URL
        List<UrlLevelEntity> levels = breakdown(encodedUrl);
        
        // Need at least protocol and domain to proceed
        if (levels.size() < 3) {
            throw new IllegalArgumentException("Invalid encoded URL format");
        }
        
        // extract the protocol as it is not encoded
        String protocol = levels.getFirst().levelName();

        // extract the encodedUrlPrefix as it is not encoded
        if (!levels.get(1).levelName().equals(encodedUrlPrefix)) {
            throw new IllegalArgumentException("The prefix extracted from the encded url does not match the expected prefix");
        }
        
        // Start building original URL
        StringBuilder originalUrl = new StringBuilder(protocol);
        
        originalUrl.append(encodedUrlPrefix);
        
        originalUrl.append(originalTopLevelDomain);
        
        // Process remaining path segments
        for (int i = 2; i < levels.size(); i++) {
            UrlLevelEntity currentLevel = levels.get(i);
            int segmentIndex = i - 2; // Adjust index to match decodedData
            
            // Add path separator
            originalUrl.append("/");
            
            if (segmentIndex >= decodedData.size()) {
                throw new IllegalArgumentException("Either Inconsistent state or unvalid encoded url");
            }

            // We need to look up the original value for each segment
            Map<String, String> currentDecodedMap = decodedData.get(segmentIndex);
                
            // the exact type of the segment path does not matter (since the encoding is not meant to be human readable)
            String segmentPath = currentLevel.levelName() != null ? currentLevel.levelName() : currentLevel.pathVariable();

            originalUrl.append(currentDecodedMap.getOrDefault(segmentPath, segmentPath));
            
            // Handle query parameters if present
            if (currentLevel.queryParamNames() != null && !currentLevel.queryParamNames().isEmpty()) {
                originalUrl.append("?");
                
                for (int j = 0; j < currentLevel.queryParamNames().size(); j++) {
                    String encodedParamName = currentLevel.queryParamNames().get(j);
                    String encodedParamValue = currentLevel.queryParamValues().get(j);
                    
                    // Add parameter separator if not the first param
                    if (j > 0) {
                        originalUrl.append("&");
                    }

                    originalUrl.append(currentDecodedMap.getOrDefault(encodedParamName, encodedParamName));
                    
                    originalUrl.append("="); 

                    originalUrl.append(currentDecodedMap.getOrDefault(encodedParamValue, encodedParamValue));

                }
            }
        }
        
        return originalUrl.toString();
    }


}       