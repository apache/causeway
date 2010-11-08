package org.apache.isis.viewer.restful.viewer.resources.objects;

import nu.xom.Element;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnNakedObjectMemberDisabled<T extends ObjectMember> extends TableColumnNakedObjectMember<T> {

    public TableColumnNakedObjectMemberDisabled(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Disabled", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final T member) {
        final boolean disabled = !member.isUsable(getSession(), getNakedObject()).isAllowed();
        return xhtmlRenderer.p(disabled, HtmlClass.USABLE);
    }
}
