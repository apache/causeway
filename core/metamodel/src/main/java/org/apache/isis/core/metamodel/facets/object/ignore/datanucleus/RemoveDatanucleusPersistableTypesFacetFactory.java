package org.apache.isis.core.metamodel.facets.object.ignore.datanucleus;

import org.apache.isis.core.metamodel.facets.object.ignore.javalang.AbstractRemoveMethodsFacetFactory;

/**
 * Removes all methods inherited from <tt>org.datanucleus.enhancer.Persistable</tt> (if datanucleus 4.x is on the classpath).
 */
public class RemoveDatanucleusPersistableTypesFacetFactory extends AbstractRemoveMethodsFacetFactory {

    public RemoveDatanucleusPersistableTypesFacetFactory() {
        super("org.datanucleus.enhancer.Persistable");
    }

}
