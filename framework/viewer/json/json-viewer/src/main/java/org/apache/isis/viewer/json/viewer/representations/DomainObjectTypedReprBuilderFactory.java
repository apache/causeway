package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.DomainObjectReprBuilder;

class DomainObjectTypedReprBuilderFactory extends TypedReprBuilderFactoryAbstract {

    public DomainObjectTypedReprBuilderFactory() {
        super(RepresentationType.DOMAIN_OBJECT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypedReprBuilder<T> newBuilder(ResourceContext resourceContext, Class<T> cls) {
        return (TypedReprBuilder<T>) DomainObjectReprBuilder.newBuilder(resourceContext);
    }
    
}