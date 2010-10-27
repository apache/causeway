package org.apache.isis.viewer.restful.viewer.resources.objects.properties;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.resources.objects.TableColumnNakedObjectAssociation;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnOneToOneAssociationAccess extends TableColumnNakedObjectAssociation<OneToOneAssociation> {

    public TableColumnOneToOneAssociationAccess(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Access", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final OneToOneAssociation property) {
        if (!property.isVisible(getSession(), getNakedObject()).isAllowed()) {
            return xhtmlRenderer.p(null, HtmlClass.PROPERTY);
        }

        final ObjectAdapter propertyValue = property.get(getNakedObject());
        if (propertyValue == null) {
            return xhtmlRenderer.p(null, HtmlClass.PROPERTY);
        }

        ParseableFacet parseable = property.getFacet(ParseableFacet.class);
        if (parseable == null) {
            final ObjectSpecification propertySpec = property.getSpecification();
            parseable = propertySpec.getFacet(ParseableFacet.class);
        }
        if (parseable != null) {
            return doTdForParseable(propertyValue);
        } else {
            return doTdForNonParseable(propertyValue);
        }
    }

    private Element doTdForParseable(final ObjectAdapter propertyValue) {
        final String titleString = propertyValue.titleString();
        return xhtmlRenderer.p(titleString, HtmlClass.PROPERTY);
    }

    private Element doTdForNonParseable(final ObjectAdapter propertyValue) {
        final String titleString = propertyValue.titleString();
        final Oid oid = propertyValue.getOid();
        if (oid == null) {
            return xhtmlRenderer.p(null, HtmlClass.PROPERTY);
        }
        final String uri = MessageFormat.format("{0}/object/{1}", getContextPath(), getOidStr(propertyValue));
        return new Element(xhtmlRenderer.aHref(uri, titleString, "propertyValue", "property", HtmlClass.PROPERTY));
    }

}
