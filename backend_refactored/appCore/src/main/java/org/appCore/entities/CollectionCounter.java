package org.appCore.entities;
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

    public CollectionCounter() {

    }

    //////////////////////////////////////////// GETTERS ////////////////////////////////////////////

    public String getCollectionName() {
        return collectionName;
    }

    public long getCount() {
        return count;
    }

    //////////////////////////////////////////// SETTERS ////////////////////////////////////////////
    // created mainly for MongoDB and Jackson (the collectionName is effectively final)
    private void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
