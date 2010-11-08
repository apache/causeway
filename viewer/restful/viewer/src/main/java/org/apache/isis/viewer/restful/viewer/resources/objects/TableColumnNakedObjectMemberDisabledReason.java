package org.apache.isis.viewer.restful.viewer.resources.objects;

import nu.xom.Element;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnNakedObjectMemberDisabledReason<T extends ObjectMember> extends
        TableColumnNakedObjectMember<T> {

    public TableColumnNakedObjectMemberDisabledReason(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Disabled Reason", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final T property) {
        final Consent usable = property.isUsable(getSession(), getNakedObject());
        if (usable.isAllowed()) {
            return xhtmlRenderer.p(null, null);
        }

        return xhtmlRenderer.p(usable.getReason(), HtmlClass.USABLE);
    }
}
