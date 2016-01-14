package org.apache.isis.viewer.wicket.ui.components.entity.tabpanel;

import org.apache.isis.applib.layout.v1_0.Tab;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class TabPanel extends PanelAbstract {
    private static final long serialVersionUID = 1L;

    private static final String ID_COLUMN = "column";

    public TabPanel(String id, final EntityModel model, final Tab tab) {
        super(id);

        final EntityModel modelWithTabHints = model.cloneWithTabMetadata(tab);

        getComponentFactoryRegistry()
                .addOrReplaceComponent(this,
                        ID_COLUMN, ComponentType.ENTITY_PROPERTIES, modelWithTabHints);

    }
}
