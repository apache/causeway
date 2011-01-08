package org.apache.isis.viewer.restful.viewer.resources.objects.actions;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnNakedObjectActionRealTarget extends TableColumnNakedObjectAction {

    public TableColumnNakedObjectActionRealTarget(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Real Target", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final ObjectAction action) {
        String oidStrRealTarget = getOidStrRealTarget(action);
		final String uri = MessageFormat.format("{0}/object/{1}", getContextPath(), oidStrRealTarget);
        return new Element(xhtmlRenderer.aHref(uri, oidStrRealTarget, "actionRealTarget", "action", HtmlClass.ACTION));
    }


}
