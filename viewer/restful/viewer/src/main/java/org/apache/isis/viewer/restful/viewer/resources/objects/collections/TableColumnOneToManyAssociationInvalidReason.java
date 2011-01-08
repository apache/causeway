package org.apache.isis.viewer.restful.viewer.resources.objects.collections;


import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.restful.viewer.resources.objects.TableColumnNakedObjectMemberInvalidReason;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnOneToManyAssociationInvalidReason extends
        TableColumnNakedObjectMemberInvalidReason<OneToManyAssociation> {

    public TableColumnOneToManyAssociationInvalidReason(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super(session, nakedObject, resourceContext);
    }

    @Override
    protected String getDomId(final OneToManyAssociation member) {
        return "collection-invalid-" + member.getId();
    }

}
