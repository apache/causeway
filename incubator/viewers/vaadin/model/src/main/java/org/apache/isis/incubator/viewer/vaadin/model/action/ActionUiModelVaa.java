package org.apache.isis.incubator.viewer.vaadin.model.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.incubator.viewer.vaadin.model.decorator.Decorators;
import org.apache.isis.viewer.common.model.action.ActionUiMetaModel;
import org.apache.isis.viewer.common.model.action.ActionUiModel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "of")
public class ActionUiModelVaa implements ActionUiModel<Component, Component> {

    @Getter 
    private final ManagedAction managedAction;
    
    @Getter(lazy = true, onMethod_ = {@Override}) 
    private final ActionUiMetaModel actionUiMetaModel = ActionUiMetaModel.of(getManagedAction());


    @Override
    public Component createMenuUiComponent() {
        return createRegularUiComponent(); 
    }

    @Override
    public Component createRegularUiComponent() {
        val actionMeta = getActionUiMetaModel();
        val uiLabel = new Label(actionMeta.getLabel());
        
        return Decorators.getIcon().decorate(uiLabel, actionMeta.getFontAwesomeUiModel());
    }


}
