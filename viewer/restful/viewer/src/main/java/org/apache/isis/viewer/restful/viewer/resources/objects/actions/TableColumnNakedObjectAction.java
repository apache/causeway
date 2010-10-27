package org.apache.isis.viewer.restful.viewer.resources.objects.actions;


import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.restful.viewer.resources.objects.TableColumnNakedObjectMember;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public abstract class TableColumnNakedObjectAction extends TableColumnNakedObjectMember<ObjectAction> {

    TableColumnNakedObjectAction(
            final String headerText,
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super(headerText, session, nakedObject, resourceContext);
    }

    protected final String getOidStrRealTarget(ObjectAction action) {
    	ObjectAdapter realTarget = action.realTarget(getNakedObject());
    	return getOidStr(realTarget);
	}


}
