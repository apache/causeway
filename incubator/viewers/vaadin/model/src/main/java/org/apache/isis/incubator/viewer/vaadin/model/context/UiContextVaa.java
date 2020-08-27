package org.apache.isis.incubator.viewer.vaadin.model.context;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.vaadin.flow.component.Component;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;

public interface UiContextVaa {

    //JavaFxViewerConfig getJavaFxViewerConfig();
    
    IsisInteractionFactory getIsisInteractionFactory();
    //ActionUiModelFactoryFx getActionUiModelFactory();
    
    void setNewPageHandler(Consumer<Component> onNewPage);
    void setPageFactory(Function<ManagedObject, Component> pageFactory);
    
    void route(ManagedObject object);
    void route(Supplier<ManagedObject> objectSupplier);
    
    // -- DECORATORS
    
//    IconDecorator<Labeled, Labeled> getIconDecoratorForLabeled();
//    IconDecorator<MenuItem, MenuItem> getIconDecoratorForMenuItem();
//   
//    DisablingDecorator<Button> getDisablingDecoratorForButton();
//    DisablingDecorator<Node> getDisablingDecoratorForFormField();
//    
//    PrototypingDecorator<Button, Node> getPrototypingDecoratorForButton();
//    PrototypingDecorator<Node, Node> getPrototypingDecoratorForFormField();
   
    
    
}
