package org.apache.isis.core.metamodel.facets.object.objecttype;


import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;


/**
 * Identifies the type of entity, such that OIDs are self-describing.
 */
public interface ObjectSpecIdFacet extends Facet {
    
    ObjectSpecId value();

}
