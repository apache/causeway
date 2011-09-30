package org.apache.isis.viewer.json.viewer.resources.home;

import java.util.List;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkReprBuilder;
import org.apache.isis.viewer.json.viewer.representations.RendererFactory;
import org.apache.isis.viewer.json.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.json.viewer.representations.ReprBuilder;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.DomainServiceResourceHelper;
import org.apache.isis.viewer.json.viewer.resources.user.UserReprRenderer;

public class HomePageReprRenderer extends ReprRendererAbstract<HomePageReprRenderer, Void> {

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.HOME_PAGE);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(ResourceContext resourceContext, JsonRepresentation representation) {
            return new HomePageReprRenderer(resourceContext, getRepresentationType(), representation);
        }
    }

    private HomePageReprRenderer(ResourceContext resourceContext, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, representationType, representation);
    }

    @Override
    public HomePageReprRenderer with(Void t) {
        return this;
    }

    @Override
    public JsonRepresentation render() {
        
        // self
        if(includesSelf) {
            withSelf("/");
        }

        // user
        putLinkToUser(representation);

        // services
        putLinkToServices(representation);
        
        // capabilities
        representation.mapPut("capabilities", LinkReprBuilder.newBuilder(getResourceContext(), "capabilities", "capabilities").render());

        // links and extensions
        representation.mapPut("links", JsonRepresentation.newArray());
        representation.mapPut("extensions", JsonRepresentation.newMap());
        
        return representation;
    }

    private void putLinkToServices(JsonRepresentation representation) {

        final LinkReprBuilder servicesLinkBuilder = LinkReprBuilder.newBuilder(getResourceContext(), "services", "services");
        
        final List<String> followLinks = getResourceContext().getArg(QueryParameter.FOLLOW_LINKS);
        if(followLinks.contains("services")) {
            final ReprBuilder reprBuilder = 
                    new DomainServiceResourceHelper(getResourceContext()).services();
            servicesLinkBuilder.withValue(reprBuilder.render());
        }
        
        representation.mapPut("services", servicesLinkBuilder.render());
    }

    private void putLinkToUser(JsonRepresentation representation) {
        final LinkReprBuilder userLinkBuilder = LinkReprBuilder.newBuilder(getResourceContext(), "user", "user");
        
        final List<String> followLinks = getResourceContext().getArg(QueryParameter.FOLLOW_LINKS);
        if(followLinks.contains("user")) {
            
            final RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.USER);
            final UserReprRenderer renderer = 
                    (UserReprRenderer) factory.newRenderer(getResourceContext(), JsonRepresentation.newMap());
            renderer.with(getResourceContext().getAuthenticationSession());
            
            userLinkBuilder.withValue(renderer.render());
        }
        
        representation.mapPut("user", userLinkBuilder.render());
    }

}