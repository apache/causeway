package org.apache.isis.core.metamodel.facets.object.ignore.datanucleus;

import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

import java.lang.reflect.Method;

/**
 * Removes all methods inherited from <tt>org.datanucleus.enhancer.Persistable</tt> (if DN is on the classpath).
 */
public class RemoveDnPrefixedMethodsFacetFactory extends FacetFactoryAbstract {

    public RemoveDnPrefixedMethodsFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext context) {
        Class<?> cls = context.getCls();
        Method[] methods = cls.getMethods();
        for(Method method: methods) {
            if(method.getName().startsWith("dn")) {
                context.removeMethod(method);
            }
        }
    }

}
