package org.apache.isis.incubator.viewer.vaadin.ui.components.links;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.incubator.viewer.vaadin.model.entity.ObjectVaa;
import org.apache.isis.viewer.common.model.link.LinkAndLabelUiModel;

import lombok.RequiredArgsConstructor;

/**
 * Responsible for creating an action's link and label.
 * @since Apr 5, 2020
 * @implNote corresponds to Wicket
 * org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions.ServiceActionLinkFactory
 *
 */
@RequiredArgsConstructor(staticName = "of")
public class ActionLinkFactoryForVaadin {

    final ObjectVaa entityUiModel;

    public LinkAndLabelUiModel<ActionLink> newLink(ObjectAction objectAction) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
