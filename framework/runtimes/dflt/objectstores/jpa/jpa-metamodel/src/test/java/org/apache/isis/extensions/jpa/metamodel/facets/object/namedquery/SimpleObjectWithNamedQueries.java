package org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery;

import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@NamedQueries({
        @NamedQuery(name = "searchById", query = "from SimpleObjectWithNameQuery where id=?"),
        @NamedQuery(name = "searchByName", query = "from SimpleObjectWithNameQuery where name=?") })
public class SimpleObjectWithNamedQueries {

    private Long id;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}