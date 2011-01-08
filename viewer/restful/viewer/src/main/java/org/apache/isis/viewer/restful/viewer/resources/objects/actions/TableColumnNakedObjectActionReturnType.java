package org.apache.isis.viewer.restful.viewer.resources.objects.actions;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnNakedObjectActionReturnType extends TableColumnNakedObjectAction {

    public TableColumnNakedObjectActionReturnType(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Type", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final ObjectAction action) {
        final String returnTypeFullName = action.getReturnType().getFullIdentifier();
        final String uri = MessageFormat.format("{0}/specs/{1}", getContextPath(), returnTypeFullName);
        return xhtmlRenderer.aHref(uri, returnTypeFullName, "actionReturnTypeSpec", "action", HtmlClass.ACTION);
    }

}
