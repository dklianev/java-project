package org.informatics.entity;

import java.io.Serializable;

public abstract class Person implements Serializable {

    protected final String id;
    protected final String name;

    protected Person(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
