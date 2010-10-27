package org.apache.isis.viewer.restful.viewer.resources.objects;


import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public abstract class TableColumnNakedObjectAssociation<T extends ObjectAssociation> extends
        TableColumnNakedObjectMember<T> {

    protected TableColumnNakedObjectAssociation(
            final String headerText,
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super(headerText, session, nakedObject, resourceContext);
    }

}
