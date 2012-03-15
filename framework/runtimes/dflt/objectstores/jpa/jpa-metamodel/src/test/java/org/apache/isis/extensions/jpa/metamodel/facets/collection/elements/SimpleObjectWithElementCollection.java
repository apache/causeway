package org.apache.isis.extensions.jpa.metamodel.facets.collection.elements;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.Id;

public class SimpleObjectWithElementCollection {

    private Long id;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    private List<SimpleObjectWithElementCollection> objects = new ArrayList<SimpleObjectWithElementCollection>();

    @ElementCollection(targetClass = SimpleObjectWithElementCollection.class, fetch=FetchType.LAZY)
    public List<SimpleObjectWithElementCollection> getObjects() {
        return objects;
    }

    public void setObjects(
            final List<SimpleObjectWithElementCollection> objects) {
        this.objects = objects;
    }

    private List<SimpleObjectWithElementCollection> otherObjects = 
        new ArrayList<SimpleObjectWithElementCollection>();

    public List<SimpleObjectWithElementCollection> getOtherObjects() {
        return otherObjects;
    }

    public void setOtherObjects(
            final List<SimpleObjectWithElementCollection> otherObjects) {
        this.otherObjects = otherObjects;
    }

}