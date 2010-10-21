package org.apache.isis.extensions.restful.viewer.resources.objects;


import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectMember;


public abstract class TableColumnNakedObjectMemberName<T extends ObjectMember> extends TableColumnNakedObjectMember<T> {
    private final ObjectSpecification noSpec;

    public TableColumnNakedObjectMemberName(
            final ObjectSpecification noSpec,
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Name", session, nakedObject, resourceContext);
        this.noSpec = noSpec;
    }

    protected ObjectSpecification getNoSpec() {
        return noSpec;
    }
    
}
