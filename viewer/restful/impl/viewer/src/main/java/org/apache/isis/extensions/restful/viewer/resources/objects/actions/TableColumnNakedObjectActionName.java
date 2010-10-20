package org.apache.isis.extensions.restful.viewer.resources.objects.actions;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.extensions.restful.viewer.html.HtmlClass;
import org.apache.isis.extensions.restful.viewer.resources.objects.TableColumnNakedObjectMemberName;
import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;


public class TableColumnNakedObjectActionName extends TableColumnNakedObjectMemberName<ObjectAction> {

    public TableColumnNakedObjectActionName(
            final ObjectSpecification noSpec,
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super(noSpec, session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final ObjectAction nakedObjectAction) {
        final String actionId = nakedObjectAction.getIdentifier().toNameParmsIdentityString();
        final String memberType = "action";
        final String uri = MessageFormat.format("{0}/specs/{1}/{2}/{3}", getContextPath(), getNoSpec().getFullName(), memberType, actionId);
        return new Element(xhtmlRenderer.aHref(uri, nakedObjectAction.getName(), "actionSpec", memberType, HtmlClass.ACTION));
    }

}
