package org.apache.isis.extensions.restful.viewer.resources.objects.collections;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.extensions.restful.viewer.html.HtmlClass;
import org.apache.isis.extensions.restful.viewer.resources.objects.TableColumnNakedObjectAssociation;
import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;


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
