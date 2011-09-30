package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;

public abstract class ReprRendererAbstract<R extends ReprRendererAbstract<R, T>, T> extends ReprBuilderAbstract<R>  implements ReprRenderer<R, T> {

    private final RepresentationType representationType;
    
    protected boolean includesSelf;

    public ReprRendererAbstract(ResourceContext resourceContext, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, representation);
        this.representationType = representationType;
    }
    
    @SuppressWarnings("unchecked")
    public R includesSelf() {
        this.includesSelf = true;
        return (R) this;
    }

    @Override
    public RepresentationType getRepresentationType() {
        return representationType;
    }

    
}
