package org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery;

import java.util.List;

import org.apache.isis.core.metamodel.facetapi.Facet;


/**
 * In the standard JPA Model, corresponds to annotating the class with either
 * {@link javax.persistence.NamedQuery} or
 * {@link javax.persistence.NamedQueries}.
 * <p>
 * For a {@link javax.persistence.NamedQuery}, returns a singleton list of
 * {@link org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.NamedQuery}.
 * For a {@link javax.persistence.NamedQueries}, returns a list of
 * {@link org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.NamedQuery}s.
 * <p>
 * In both cases, mapping is as follows
 * <ul>
 * <li>{@link javax.persistence.NamedQuery#name()} ->
 * {@link org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.NamedQuery#getName()
 * getName()} property of JPA Object Store's own
 * {@link org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.NamedQuery
 * NamedQuery} value object</li>
 * <li>{@link javax.persistence.NamedQuery#query()} ->
 * {@link org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.NamedQuery#getQuery()}
 * property of JPA Object Store's own
 * {@link org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.NamedQuery
 * NamedQuery} value object</li>
 * <li>{@link javax.persistence.NamedQuery#hints()} -> (no corresponding
 * attribute or facet)</li>
 * </ul>
 */
public interface JpaNamedQueryFacet extends Facet {

    /**
     * Returns an immutable {@link List}.
     */
    List<org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.NamedQuery> getNamedQueries();
}
