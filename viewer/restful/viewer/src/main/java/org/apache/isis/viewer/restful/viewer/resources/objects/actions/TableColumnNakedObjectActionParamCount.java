package org.apache.isis.viewer.restful.viewer.resources.objects.actions;

import nu.xom.Element;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnNakedObjectActionParamCount extends TableColumnNakedObjectAction {

    public TableColumnNakedObjectActionParamCount(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("# Params", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final ObjectAction action) {
        return xhtmlRenderer.p("" + action.getParameterCount(), HtmlClass.ACTION);
    }

}
