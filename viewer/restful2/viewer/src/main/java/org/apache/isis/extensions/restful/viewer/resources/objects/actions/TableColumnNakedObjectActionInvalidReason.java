package org.apache.isis.extensions.restful.viewer.resources.objects.actions;


import org.apache.isis.extensions.restful.viewer.resources.objects.TableColumnNakedObjectMemberInvalidReason;
import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.ObjectAction;


public final class TableColumnNakedObjectActionInvalidReason extends TableColumnNakedObjectMemberInvalidReason<ObjectAction> {

    public TableColumnNakedObjectActionInvalidReason(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super(session, nakedObject, resourceContext);
    }

    @Override
    protected String getDomId(final ObjectAction action) {
        return "action-invalid-" + action.getId();
    }

}
