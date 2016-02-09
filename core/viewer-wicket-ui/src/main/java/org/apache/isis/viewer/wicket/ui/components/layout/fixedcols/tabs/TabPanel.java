package org.apache.isis.viewer.wicket.ui.components.layout.fixedcols.tabs;

import org.apache.isis.core.metamodel.services.grid.fixedcols.applib.FCTab;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class TabPanel extends PanelAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_COLUMN = "column";

    public TabPanel(String id, final EntityModel model, final FCTab fcTab) {
        super(id);

        final EntityModel modelWithTabHints = model.cloneWithLayoutMetadata(fcTab);

        getComponentFactoryRegistry()
                .addOrReplaceComponent(this,
                        ID_COLUMN, ComponentType.ENTITY_PROPERTIES, modelWithTabHints);

    }
}
