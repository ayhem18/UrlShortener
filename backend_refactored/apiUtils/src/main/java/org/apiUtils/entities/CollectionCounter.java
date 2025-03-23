package org.apiUtils.entities;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class CollectionCounter {
    /*
     * This collection simply saves the total number of records created for a given collection guaranteeing
     * the persistence of such information. Otherwise, I might have to resort to static fields or other work-around
     * that might make the service stateful (not a good design choice overall)
     * */

    @Id
    private String collectionName;
    private long count;

    public CollectionCounter(String collectionName) {
        this.collectionName = collectionName;
        this.count = 0;
    }

    @SuppressWarnings("unused")
    private CollectionCounter() {
        // for Jackson
    }

    //////////////////////////////////////////// GETTERS ////////////////////////////////////////////

    public String getCollectionName() {
        return collectionName;
    }

    public long getCount() {
        return count;
    }

    //////////////////////////////////////////// SETTERS ////////////////////////////////////////////
    public void setCount(long count) {
        this.count = count;
    }

    // created mainly for MongoDB and Jackson (the collectionName is effectively final)
    @SuppressWarnings("unused")
    private void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

}
