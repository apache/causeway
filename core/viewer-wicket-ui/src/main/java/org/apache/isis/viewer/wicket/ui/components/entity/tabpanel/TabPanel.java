package org.apache.isis.viewer.wicket.ui.components.entity.tabpanel;

import org.apache.isis.applib.layout.v1_0.TabMetadata;
import org.apache.isis.viewer.wicket.model.hints.UiHintPathSignificant;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class TabPanel extends PanelAbstract implements UiHintPathSignificant {

    private static final long serialVersionUID = 1L;

    private static final String ID_COLUMN = "column";

    public TabPanel(String id, final EntityModel model, final TabMetadata tabMetadata) {
        super(id);

        final EntityModel modelWithTabHints = model.cloneWithTabMetadata(tabMetadata);

        getComponentFactoryRegistry()
                .addOrReplaceComponent(this,
                        ID_COLUMN, ComponentType.ENTITY_PROPERTIES, modelWithTabHints);

    }
}
