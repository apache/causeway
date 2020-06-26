package org.apache.isis.incubator.viewer.javafx.model.action;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.viewer.common.model.action.ActionUiMetaModel;
import org.apache.isis.viewer.common.model.action.ActionUiModel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;

@RequiredArgsConstructor(staticName = "of")
public class ActionUiModelFx implements ActionUiModel<MenuItem, Node> {

    @Getter 
    private final ManagedAction managedAction;
    
    @Getter(lazy = true, onMethod_ = {@Override}) 
    private final ActionUiMetaModel actionUiMetaModel = ActionUiMetaModel.of(getManagedAction());


    @Override
    public MenuItem createMenuUiComponent() {
        val actionMeta = getActionUiMetaModel();
        return new MenuItem(actionMeta.getLabel());
        
    }

    @Override
    public Node createRegularUiComponent() {
        val actionMeta = getActionUiMetaModel();
        val uiLabel = new Label(actionMeta.getLabel());
        
        return uiLabel;
        //return Decorators.getIcon().decorate(uiLabel, actionMeta.getFontAwesomeUiModel());
    }


}
