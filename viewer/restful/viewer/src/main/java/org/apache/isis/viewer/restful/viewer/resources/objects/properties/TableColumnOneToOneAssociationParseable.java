package org.apache.isis.viewer.restful.viewer.resources.objects.properties;

import nu.xom.Element;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.resources.objects.TableColumnNakedObjectAssociation;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnOneToOneAssociationParseable extends TableColumnNakedObjectAssociation<OneToOneAssociation> {

    public TableColumnOneToOneAssociationParseable(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Parseable", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final OneToOneAssociation property) {
        final boolean parseable = property.getFacet(ParseableFacet.class) != null;
        return xhtmlRenderer.p(parseable, HtmlClass.VISIBLE);
    }
}
