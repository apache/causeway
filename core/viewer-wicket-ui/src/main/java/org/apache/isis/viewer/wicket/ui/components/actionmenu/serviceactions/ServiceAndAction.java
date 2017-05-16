package org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

class ServiceAndAction {
    final String serviceName;
    final EntityModel serviceEntityModel;
    final ObjectAction objectAction;
    final ServiceActionLinkFactory linkAndLabelFactory;

    public boolean separator;

    ServiceAndAction(
            final String serviceName,
            final EntityModel serviceEntityModel,
            final ObjectAction objectAction) {
        this.serviceName = serviceName;
        this.serviceEntityModel = serviceEntityModel;
        this.objectAction = objectAction;
        this.linkAndLabelFactory = new ServiceActionLinkFactory(serviceEntityModel);
    }

    @Override
    public String toString() {
        return serviceName + " ~ " + objectAction.getIdentifier().toFullIdentityString();
    }
}
