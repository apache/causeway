package org.apache.isis.extensions.restful.viewer.resources.objects;


import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;


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
