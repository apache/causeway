package demoapp.webapp.wicket.ui.custom;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityComponentFactoryAbstract;

import demoapp.dom.ui.custom.geocoding.GeoapifyClient;
import demoapp.dom.ui.custom.vm.WhereInTheWorldVm;

//tag::class[]
@org.springframework.stereotype.Component
@Order(OrderPrecedence.EARLY)                                             // <.>
public class WhereInTheWorldPanelFactory extends EntityComponentFactoryAbstract {

    public WhereInTheWorldPanelFactory() {
        super(
            ComponentType.ENTITY                                          // <.>
            , WhereInTheWorldPanel.class
        );
    }

    @Override
    protected ApplicationAdvice doAppliesTo(EntityModel entityModel) {    // <.>
        final ManagedObject managedObject = entityModel.getObject();      // <.>
        final Object domainObject = managedObject.getPojo();              // <.>
        return ApplicationAdvice.appliesIf(
                domainObject instanceof WhereInTheWorldVm);               // <.>
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        EntityModel entityModel = (EntityModel) model;                    // <.>
        return new WhereInTheWorldPanel(id, entityModel, geoapifyClient); // <.>
    }

    @Inject
    private GeoapifyClient geoapifyClient;                                // <.>

}
//end::class[]
