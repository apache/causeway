package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.TypedReprBuilder;
import org.apache.isis.viewer.json.viewer.representations.TypedReprBuilderFactoryAbstract;

public class DomainObjectReprBuilderFactory extends TypedReprBuilderFactoryAbstract {

    public DomainObjectReprBuilderFactory() {
        super(RepresentationType.DOMAIN_OBJECT);
    }

    @Override
    public TypedReprBuilder<?,?> newBuilder(ResourceContext resourceContext) {
        return DomainObjectReprBuilder.newBuilder(resourceContext);
    }
    
}