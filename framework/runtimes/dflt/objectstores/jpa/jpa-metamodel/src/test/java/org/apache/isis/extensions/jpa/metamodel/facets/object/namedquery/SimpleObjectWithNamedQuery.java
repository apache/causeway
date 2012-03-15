package org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery;

import javax.persistence.Id;
import javax.persistence.NamedQuery;

@NamedQuery(name = "searchById", query = "from SimpleObjectWithNameQuery where id=?")
public class SimpleObjectWithNamedQuery {

    private Long id;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}