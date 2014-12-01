package org.apache.isis.viewer.wicket.ui.components.actionprompt;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * A panel used as a title for the action prompts
 */
public class ActionPromptHeaderPanel extends PanelAbstract<ActionModel> {

    private static final String ID_ACTION_NAME = "actionName";

    public ActionPromptHeaderPanel(String id, final ActionModel model) {
        super(id, model);

        ObjectAdapter targetAdapter = model.getTargetAdapter();

        getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.ENTITY_ICON_AND_TITLE, new EntityModel(targetAdapter));

        add(new Label(ID_ACTION_NAME, new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return model.getActionMemento().getAction().getName();
            }
        }));
    }

}
