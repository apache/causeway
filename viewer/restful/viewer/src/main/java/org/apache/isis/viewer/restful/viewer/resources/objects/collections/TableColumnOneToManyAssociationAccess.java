package org.apache.isis.viewer.restful.viewer.resources.objects.collections;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.resources.objects.TableColumnNakedObjectAssociation;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnOneToManyAssociationAccess extends TableColumnNakedObjectAssociation<OneToManyAssociation> {

    public TableColumnOneToManyAssociationAccess(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Access", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final OneToManyAssociation collection) {
        if (!collection.isVisible(getSession(), getNakedObject()).isAllowed()) {
            return xhtmlRenderer.p(null, HtmlClass.PROPERTY);
        }

        final String contextPath = resourceContext.getHttpServletRequest().getContextPath();
        final String collectionId = collection.getId();
        final String uri = MessageFormat.format("{0}/object/{1}/collection/{2}", contextPath, getOidStr(), collectionId);
        return xhtmlRenderer.aHref(uri, collectionId, "collection", "nakedObject", HtmlClass.COLLECTION);
    }

}
