/**
 * 
 */
package org.apache.isis.extensions.jpa.metamodel.facets.prop.id;

import javax.persistence.Id;

public class SimpleObjectWithId {

    private Long id;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}