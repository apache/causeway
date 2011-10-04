package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;

public abstract class ReprRendererAbstract<R extends ReprRendererAbstract<R, T>, T> implements ReprRenderer<R, T> {

    protected final ResourceContext resourceContext;
    private final RepresentationType representationType;
    protected final JsonRepresentation representation;
    
    protected boolean includesSelf;

    public ReprRendererAbstract(ResourceContext resourceContext, RepresentationType representationType, JsonRepresentation representation) {
        this.resourceContext = resourceContext;
        this.representationType = representationType;
        this.representation = representation;
    }

    public ResourceContext getResourceContext() {
        return resourceContext;
    }

    @Override
    public RepresentationType getRepresentationType() {
        return representationType;
    }


    @SuppressWarnings("unchecked")
    public R includesSelf() {
        this.includesSelf = true;
        return (R) this;
    }

    public R withSelf(String href) {
        if(href != null) {
            representation.mapPut("self", LinkBuilder.newBuilder(resourceContext, "self", representationType, href).build());
        }
        return cast(this);
    }

    
    public R withLinks() {
        return withLinks(JsonRepresentation.newArray());
    }

    public R withLinks(JsonRepresentation links) {
        if(!links.isArray()) {
            throw new IllegalArgumentException("links must be a list");
        }
        representation.mapPut("links", links);
        return cast(this);
    }

    public R withExtensions() {
        return withExtensions(JsonRepresentation.newMap());
    }

    public R withExtensions(JsonRepresentation extensions) {
        if(!extensions.isMap()) {
            throw new IllegalArgumentException("extensions must be a map");
        }
        representation.mapPut("extensions", extensions);
        return cast(this);
    }

    
    @SuppressWarnings("unchecked")
    protected static <R extends ReprRendererAbstract<R, T>, T> R cast(ReprRendererAbstract<R,T> builder) {
        return (R) builder;
    }

    public abstract JsonRepresentation render();


    
    protected OidStringifier getOidStringifier() {
        return getOidGenerator().getOidStringifier();
    }

    protected OidGenerator getOidGenerator() {
        return getPersistenceSession().getOidGenerator();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AuthenticationSession getSession() {
        return IsisContext.getAuthenticationSession();
    }

    protected Localization getLocalization() {
        return IsisContext.getLocalization();
    }
}
