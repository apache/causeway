package org.apache.isis.incubator.viewer.vaadin.ui.pages.main;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;

import com.vaadin.flow.component.Component;

import org.springframework.stereotype.Service;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.incubator.viewer.vaadin.model.context.UiContextVaa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class UiContextVaaDefault implements UiContextVaa {

//    @Getter(onMethod_ = {@Override})
//    private final JavaFxViewerConfig javaFxViewerConfig;
    @Getter(onMethod_ = {@Override})
    private final IsisInteractionFactory isisInteractionFactory;
//    @Getter(onMethod_ = {@Override})
//    private final ActionUiModelFactoryFx actionUiModelFactory = new ActionUiModelFactoryFx();
    
    @Setter(onMethod_ = {@Override})
    private Consumer<Component> newPageHandler;
    
    @Setter(onMethod_ = {@Override})
    private Function<ManagedObject, Component> pageFactory;

    @Override
    public void route(ManagedObject object) {
        log.info("about to render object {}", object);
        newPage(pageFor(object));
    }
    
    @Override
    public void route(Supplier<ManagedObject> objectSupplier) {
        isisInteractionFactory.runAnonymous(()->{
            val object = objectSupplier.get();
            route(object);
        });
    }
    
    // -- DECORATORS

//    @Getter(onMethod_ = {@Override})
//    private final IconDecorator<Labeled, Labeled> iconDecoratorForLabeled;
//    @Getter(onMethod_ = {@Override})
//    private final IconDecorator<MenuItem, MenuItem> iconDecoratorForMenuItem;
//   
//    @Getter(onMethod_ = {@Override})
//    private final DisablingDecorator<Button> disablingDecoratorForButton;
//    @Getter(onMethod_ = {@Override})
//    private final DisablingDecorator<Node> disablingDecoratorForFormField;
//    
//    @Getter(onMethod_ = {@Override})
//    private final PrototypingDecorator<Button, Node> prototypingDecoratorForButton;
//    @Getter(onMethod_ = {@Override})
//    private final PrototypingDecorator<Node, Node> prototypingDecoratorForFormField;
    
    // -- HELPER
    
    private void newPage(Component content) {
        if(newPageHandler!=null && content!=null) {
            newPageHandler.accept(content);
        }
    }
    
    private Component pageFor(ManagedObject object) {
        return pageFactory!=null
                ? pageFactory.apply(object)
                : null;
    }
    
}
