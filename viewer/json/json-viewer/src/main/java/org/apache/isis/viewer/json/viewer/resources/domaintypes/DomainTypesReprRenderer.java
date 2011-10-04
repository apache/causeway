package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import java.util.Collection;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;

public class DomainTypesReprRenderer extends ReprRendererAbstract<DomainTypesReprRenderer, Collection<ObjectSpecification>> {

    private Collection<ObjectSpecification> specifications;

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.DOMAIN_TYPES);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(ResourceContext resourceContext, JsonRepresentation representation) {
            return new DomainTypesReprRenderer(resourceContext, getRepresentationType(), representation);
        }
    }

    private DomainTypesReprRenderer(ResourceContext resourceContext, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, representationType, representation);
    }

    @Override
    public DomainTypesReprRenderer with(Collection<ObjectSpecification> specifications) {
        this.specifications = specifications;
        return this;
    }

    @Override
    public JsonRepresentation render() {
        
        // self
        if(includesSelf) {
            withSelf("domainTypes");
        }
        
        JsonRepresentation specList = JsonRepresentation.newArray();
        for (ObjectSpecification objectSpec : specifications) {
            final LinkBuilder linkBuilder = 
                    LinkBuilder.newBuilder(getResourceContext(), "domainType", RepresentationType.DOMAIN_TYPE, "domainTypes/%s", objectSpec.getFullIdentifier());
            specList.arrayAdd(linkBuilder.build());
        }
        
        representation.mapPut("domainTypes", specList);

        // links and extensions
        representation.mapPut("links", JsonRepresentation.newArray());
        representation.mapPut("extensions", JsonRepresentation.newMap());
        
        return representation;
    }


}