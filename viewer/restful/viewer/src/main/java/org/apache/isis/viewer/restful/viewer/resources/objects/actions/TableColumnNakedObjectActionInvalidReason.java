package org.apache.isis.viewer.restful.viewer.resources.objects.actions;


import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.restful.viewer.resources.objects.TableColumnNakedObjectMemberInvalidReason;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


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
