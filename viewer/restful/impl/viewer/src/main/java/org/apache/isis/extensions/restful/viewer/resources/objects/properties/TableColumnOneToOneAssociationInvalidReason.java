package org.apache.isis.extensions.restful.viewer.resources.objects.properties;


import org.apache.isis.extensions.restful.viewer.resources.objects.TableColumnNakedObjectMemberInvalidReason;
import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;


public final class TableColumnOneToOneAssociationInvalidReason extends
        TableColumnNakedObjectMemberInvalidReason<OneToOneAssociation> {

    public TableColumnOneToOneAssociationInvalidReason(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super(session, nakedObject, resourceContext);
    }

    @Override
    protected String getDomId(final OneToOneAssociation member) {
        return "property-invalid-" + member.getId();
    }

}
