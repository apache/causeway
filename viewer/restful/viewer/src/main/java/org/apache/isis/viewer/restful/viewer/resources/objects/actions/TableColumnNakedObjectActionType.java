package org.apache.isis.viewer.restful.viewer.resources.objects.actions;

import nu.xom.Element;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


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
