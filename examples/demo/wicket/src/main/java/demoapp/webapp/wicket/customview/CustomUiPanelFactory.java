package demoapp.webapp.wicket.customview;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityComponentFactoryAbstract;

import demoapp.dom.ui.custom.geocoding.GeoapifyClient;
import demoapp.dom.ui.custom.vm.CustomUiVm;

@org.springframework.stereotype.Component
@Order(OrderPrecedence.EARLY)
public class CustomUiPanelFactory extends EntityComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public CustomUiPanelFactory() {
        super(ComponentType.ENTITY, CustomUiPanel.class);
    }

    @Override
    protected ApplicationAdvice doAppliesTo(EntityModel entityModel) {
        return ApplicationAdvice.appliesIf(entityModel.getObject().getPojo() instanceof CustomUiVm);
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final EntityModel entityModel = (EntityModel) model;

        return new CustomUiPanel(id, entityModel, this, geoapifyClient);
    }

    @Inject
    private GeoapifyClient geoapifyClient;

}
