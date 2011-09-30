package org.apache.isis.viewer.json.viewer.resources.capabilities;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkReprBuilder;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;

public class CapabilitiesReprRenderer extends ReprRendererAbstract<CapabilitiesReprRenderer, Void> {

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.CAPABILITIES);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(ResourceContext resourceContext, JsonRepresentation representation) {
            return new CapabilitiesReprRenderer(resourceContext, getRepresentationType(), representation);
        }
    }
    
    private CapabilitiesReprRenderer(ResourceContext resourceContext, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, representationType, representation);
        includesSelf();
    }

    @Override
    public CapabilitiesReprRenderer with(Void t) {
        return this;
    }

    @Override
    public JsonRepresentation render() {

        if(includesSelf) {
            withSelf("capabilities/");
        }

        JsonRepresentation capabilities = JsonRepresentation.newMap();

        capabilities.mapPut("concurrencyChecking", "no");
        capabilities.mapPut("transientObjects", "no");
        capabilities.mapPut("deleteObjects", "no");
        capabilities.mapPut("simpleArguments", "no");
        capabilities.mapPut("partialArguments", "no");
        capabilities.mapPut("followLinks", "no");
        capabilities.mapPut("validateOnly", "no");
        capabilities.mapPut("pagination", "no");
        capabilities.mapPut("sorting", "no");
        capabilities.mapPut("domainModel", "rich");

        representation.mapPut("capabilities", capabilities);
        representation.mapPut("links", JsonRepresentation.newArray());
        representation.mapPut("extensions", JsonRepresentation.newMap());

        return representation;
    }
}