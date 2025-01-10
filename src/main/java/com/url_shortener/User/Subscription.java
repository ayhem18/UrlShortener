package com.url_shortener.User;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Subscription {

    // one troublesome fact is that the @Id annotation has 2 variants one for MongoDB and one for SQL,
    // need to be careful not to use SQL and nonSQL annotation with the same class
    @Id
    String tier;

    // the maximum number of levels
    int maxNumLevels;

    // max query parameter names per level
    int maxQueryParams;

    // max query parameters values per level
    int maxQueryValues;

    // max path variables per level per level
    int maxPathVariables;

}

