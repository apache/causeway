package org.apache.isis.extensions.restful.viewer.resources.objects.collections;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.extensions.restful.viewer.html.HtmlClass;
import org.apache.isis.extensions.restful.viewer.resources.objects.TableColumnNakedObjectMemberName;
import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;


public class TableColumnOneToManyAssociationName extends TableColumnNakedObjectMemberName<OneToManyAssociation> {

    public TableColumnOneToManyAssociationName(
            final ObjectSpecification noSpec,
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super(noSpec, session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final OneToManyAssociation oneToManyAssociation) {
        final String memberName = oneToManyAssociation.getIdentifier().getMemberName();
        final String memberType = "collection";
        final String uri = MessageFormat.format("{0}/specs/{1}/{2}/{3}", getContextPath(), getNoSpec().getFullName(), memberType,
                memberName);
        return new Element(xhtmlRenderer.aHref(uri, oneToManyAssociation.getName(), "propertySpec", memberType,
                HtmlClass.COLLECTION));
    }



}
