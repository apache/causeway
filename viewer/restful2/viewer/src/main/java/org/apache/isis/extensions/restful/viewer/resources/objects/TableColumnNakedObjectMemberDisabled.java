package org.apache.isis.extensions.restful.viewer.resources.objects;

import nu.xom.Element;

import org.apache.isis.extensions.restful.viewer.html.HtmlClass;
import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.ObjectMember;


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
