package org.apache.isis.viewer.json.viewer.resources.home;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.RendererFactory;
import org.apache.isis.viewer.json.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;
import org.apache.isis.viewer.json.viewer.resources.capabilities.CapabilitiesReprRenderer;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.DomainServiceLinkTo;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.ListReprRenderer;
import org.apache.isis.viewer.json.viewer.resources.user.UserReprRenderer;

public class HomePageReprRenderer extends ReprRendererAbstract<HomePageReprRenderer, Void> {

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.HOME_PAGE);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(ResourceContext resourceContext, LinkFollower linkFollower, JsonRepresentation representation) {
            return new HomePageReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    private HomePageReprRenderer(ResourceContext resourceContext, LinkFollower linkFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    @Override
    public HomePageReprRenderer with(Void t) {
        return this;
    }

    @Override
    public JsonRepresentation render() {
        
        // self
        if(includesSelf) {
            putLinkToSelf(representation);
        }

        // user
        putLinkToUser(representation);

        // services
        putLinkToServices(representation);
        
        // capabilities
        putLinkToCapabilities(representation);

        // links and extensions
        representation.mapPut("links", JsonRepresentation.newArray());
        representation.mapPut("extensions", JsonRepresentation.newMap());

        return representation;
    }

    private void putLinkToSelf(JsonRepresentation representation) {
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(resourceContext, "self", getRepresentationType(), "");

        final LinkFollower linkFollower = getLinkFollower().follow("self");
        if(linkFollower.isFollowing()) {

            final RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.HOME_PAGE);
            final HomePageReprRenderer renderer = 
                    (HomePageReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            
            linkBuilder.withValue(renderer.render());
        }
        representation.mapPut("self", linkBuilder.build());
    }

    private void putLinkToCapabilities(final JsonRepresentation representation) {
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), "capabilities", RepresentationType.CAPABILITIES, "capabilities");
        
        final LinkFollower linkFollower = getLinkFollower().follow("capabilities");
        if(linkFollower.isFollowing()) {

            final RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.CAPABILITIES);
            final CapabilitiesReprRenderer renderer = (CapabilitiesReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            
            linkBuilder.withValue(renderer.render());
        }
        
        representation.mapPut("capabilities", linkBuilder.build());
    }

    private void putLinkToServices(JsonRepresentation representation) {

        final LinkBuilder linkBuilder = 
                LinkBuilder.newBuilder(getResourceContext(), "services", RepresentationType.LIST, "services");
        
        final LinkFollower linkFollower = getLinkFollower().follow("services");
        if(linkFollower.isFollowing()) {
            
            final List<ObjectAdapter> serviceAdapters = getResourceContext().getPersistenceSession().getServices();

            final RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.LIST);
            
            final ListReprRenderer renderer = (ListReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            renderer.usingLinkToBuilder(new DomainServiceLinkTo())
                    .withSelf("services")
                    .with(serviceAdapters);
            
            linkBuilder.withValue(renderer.render());
        }
        
        representation.mapPut("services", linkBuilder.build());
    }

    private void putLinkToUser(JsonRepresentation representation) {
        final LinkBuilder userLinkBuilder = 
                LinkBuilder.newBuilder(getResourceContext(), "user", RepresentationType.USER, "user");
        
        final LinkFollower linkFollower = getLinkFollower().follow("user");
        if(linkFollower.isFollowing()) {
            final RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.USER);
            final UserReprRenderer renderer = 
                    (UserReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            renderer.with(getResourceContext().getAuthenticationSession());
            
            userLinkBuilder.withValue(renderer.render());
        }
        
        representation.mapPut("user", userLinkBuilder.build());
    }

}