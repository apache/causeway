package org.apache.isis.core.runtime.services.menubars.bootstrap3;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

class ServiceAndAction {
    final String serviceName;
    final ObjectAdapter serviceAdapter;
    final ObjectAction objectAction;

    public boolean separator;

    ServiceAndAction(
            final String serviceName,
            final ObjectAdapter serviceAdapter,
            final ObjectAction objectAction) {
        this.serviceName = serviceName;
        this.serviceAdapter = serviceAdapter;
        this.objectAction = objectAction;
    }

    @Override
    public String toString() {
        return serviceName + " ~ " + objectAction.getIdentifier().toFullIdentityString();
    }

}
