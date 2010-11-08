package org.apache.isis.viewer.restful.viewer.resources.objects;

import nu.xom.Element;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnNakedObjectMemberHidden<T extends ObjectMember> extends TableColumnNakedObjectMember<T> {

    public TableColumnNakedObjectMemberHidden(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Hidden", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final T member) {
        final boolean hidden = !member.isVisible(getSession(), getNakedObject()).isAllowed();
        return xhtmlRenderer.p(hidden, HtmlClass.VISIBLE);
    }
}
