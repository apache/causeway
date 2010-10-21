package org.apache.isis.extensions.restful.viewer.resources.objects;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.extensions.restful.viewer.html.HtmlClass;
import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;


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
