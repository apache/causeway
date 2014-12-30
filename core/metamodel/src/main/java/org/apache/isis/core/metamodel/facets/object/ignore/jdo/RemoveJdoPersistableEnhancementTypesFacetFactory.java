package org.apache.isis.core.metamodel.facets.object.ignore.jdo;

import org.apache.isis.core.metamodel.facets.object.ignore.javalang.AbstractRemoveMethodsFacetFactory;

/**
 * Removes all methods inherited from <tt>javax.jdo.spi.Persistable</tt> (if JDO is on the classpath).
 */
public class RemoveJdoPersistableEnhancementTypesFacetFactory extends AbstractRemoveMethodsFacetFactory {

    public RemoveJdoPersistableEnhancementTypesFacetFactory() {
        super("javax.jdo.spi.Persistable");
    }

}
