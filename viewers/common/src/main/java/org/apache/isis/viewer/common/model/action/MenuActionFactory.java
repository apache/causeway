package org.apache.isis.viewer.common.model.action;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;

public interface MenuActionFactory<T> {

    MenuActionUiModel<T> newMenuAction(
            IsisWebAppCommonContext commonContext, 
            String named,
            ObjectAction objectAction,
            ManagedObject actionHolder);

}
