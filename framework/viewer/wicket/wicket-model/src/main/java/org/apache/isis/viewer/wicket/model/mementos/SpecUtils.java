package org.apache.isis.viewer.wicket.model.mementos;

import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

public final class SpecUtils {
    
    private SpecUtils(){}

    public static ObjectSpecification getSpecificationFor(ObjectSpecId objectSpecId) {
        ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecId(objectSpecId);
        if(objectSpec != null) {
            return objectSpec;
        } 
        
        // attempt to load directly.
        return getSpecificationLoader().loadSpecification(objectSpecId.asString());
    }

    protected static SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}
