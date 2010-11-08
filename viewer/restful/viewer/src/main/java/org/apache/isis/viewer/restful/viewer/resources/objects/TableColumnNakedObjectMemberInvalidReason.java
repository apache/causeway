package org.apache.isis.viewer.restful.viewer.resources.objects;

import nu.xom.Element;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public abstract class TableColumnNakedObjectMemberInvalidReason<T extends ObjectMember> extends
        TableColumnNakedObjectMember<T> {

    public TableColumnNakedObjectMemberInvalidReason(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Invalid Reason", session, nakedObject, resourceContext);
    }

    /**
     * Creates a &lt;p id=&quot;association-invalid-<i>associationId</i>&quot;/&gt; element so that Javascript
     * code can populate during validation.
     */
    @Override
    public Element doTd(final T member) {
        return builder().el("p").classAttr(HtmlClass.VALID).idAttr(getDomId(member)).build();
    }

    protected abstract String getDomId(T member);

}
