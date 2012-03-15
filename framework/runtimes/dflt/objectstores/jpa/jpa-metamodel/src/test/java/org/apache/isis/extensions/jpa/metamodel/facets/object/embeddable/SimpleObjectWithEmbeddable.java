package org.apache.isis.extensions.jpa.metamodel.facets.object.embeddable;

import javax.persistence.Embeddable;
import javax.persistence.Id;

@Embeddable
public class SimpleObjectWithEmbeddable {

    private Long id;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}