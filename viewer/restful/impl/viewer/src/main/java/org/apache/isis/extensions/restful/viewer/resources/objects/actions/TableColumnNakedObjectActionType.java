package org.apache.isis.extensions.restful.viewer.resources.objects.actions;

import nu.xom.Element;

import org.apache.isis.extensions.restful.viewer.html.HtmlClass;
import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.ObjectAction;


public final class TableColumnNakedObjectActionType extends TableColumnNakedObjectAction {

    public TableColumnNakedObjectActionType(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Type", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final ObjectAction action) {
        return xhtmlRenderer.p(action.getType().name(), HtmlClass.ACTION);
    }
}
