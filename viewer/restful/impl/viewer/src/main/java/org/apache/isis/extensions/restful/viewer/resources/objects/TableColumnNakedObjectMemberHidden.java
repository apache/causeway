package org.apache.isis.extensions.restful.viewer.resources.objects;

import nu.xom.Element;

import org.apache.isis.extensions.restful.viewer.html.HtmlClass;
import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.ObjectMember;


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
