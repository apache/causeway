package demoapp.webapp.wicket.customview;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.BS3GridPanel;

import lombok.val;

@org.springframework.stereotype.Component
public class MyEntityPanelFactory  extends EntityComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public MyEntityPanelFactory() {
        super(ComponentType.ENTITY, MyEntityPanel.class);
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final EntityModel entityModel = (EntityModel) model;

        val objectAdapter = entityModel.getObject();
        final ObjectSpecification specification = entityModel.getTypeOfSpecification();
        final GridFacet facet = specification.getFacet(GridFacet.class);

        final Grid grid = facet.getGrid(objectAdapter);
        if (grid != null) {
            if(grid instanceof BS3Grid) {
                final BS3Grid bs3Grid = (BS3Grid) grid;
                return new BS3GridPanel(id, entityModel, bs3Grid);
            }
        }
        return new MyEntityPanel(id, entityModel, this);
    }
}
