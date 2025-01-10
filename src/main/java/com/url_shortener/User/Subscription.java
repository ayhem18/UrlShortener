package com.url_shortener.User;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Subscription {
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

