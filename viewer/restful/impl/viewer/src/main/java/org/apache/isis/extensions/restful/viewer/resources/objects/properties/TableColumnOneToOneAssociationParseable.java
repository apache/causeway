package org.apache.isis.extensions.restful.viewer.resources.objects.properties;

import nu.xom.Element;

import org.apache.isis.extensions.restful.viewer.html.HtmlClass;
import org.apache.isis.extensions.restful.viewer.resources.objects.TableColumnNakedObjectAssociation;
import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;


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
