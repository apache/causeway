package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;

public interface RendererFactory {

    RepresentationType getRepresentationType();

    ReprRenderer<?, ?> newRenderer(ResourceContext resourceContext, PathFollower pathFollower, JsonRepresentation representation);
    
}