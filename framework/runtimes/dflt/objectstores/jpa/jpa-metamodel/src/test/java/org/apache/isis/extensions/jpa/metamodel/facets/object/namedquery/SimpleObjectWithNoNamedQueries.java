package org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery;

import javax.persistence.Id;

public class SimpleObjectWithNoNamedQueries {

    private Long id;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}