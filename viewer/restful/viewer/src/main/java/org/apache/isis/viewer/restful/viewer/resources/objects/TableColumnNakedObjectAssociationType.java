package org.apache.isis.viewer.restful.viewer.resources.objects;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnNakedObjectAssociationType<T extends ObjectAssociation> extends
        TableColumnNakedObjectAssociation<T> {

    public TableColumnNakedObjectAssociationType(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Type", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final T association) {
        final String assocTypeFullName = association.getSpecification().getFullName();
        final String uri = MessageFormat.format("{0}/specs/{1}", getContextPath(), assocTypeFullName);
        return xhtmlRenderer.aHref(uri, assocTypeFullName, "propertyTypeSpec", "property", HtmlClass.PROPERTY);
    }
}
