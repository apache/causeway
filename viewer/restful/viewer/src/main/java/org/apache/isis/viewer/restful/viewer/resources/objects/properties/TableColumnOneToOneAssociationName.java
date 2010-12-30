package org.apache.isis.viewer.restful.viewer.resources.objects.properties;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.resources.objects.TableColumnNakedObjectMemberName;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public class TableColumnOneToOneAssociationName extends TableColumnNakedObjectMemberName<OneToOneAssociation> {

    public TableColumnOneToOneAssociationName(
            final ObjectSpecification noSpec,
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super(noSpec, session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final OneToOneAssociation oneToOneAssociation) {
        final String memberName = oneToOneAssociation.getIdentifier().getMemberName();
        final String memberType = "property";
        final String uri = MessageFormat.format("{0}/specs/{1}/{2}/{3}", getContextPath(), getNoSpec().getFullIdentifier(), memberType,
                memberName);
        return new Element(xhtmlRenderer.aHref(uri, oneToOneAssociation.getName(), "propertySpec", memberType, HtmlClass.PROPERTY));
    }


}
