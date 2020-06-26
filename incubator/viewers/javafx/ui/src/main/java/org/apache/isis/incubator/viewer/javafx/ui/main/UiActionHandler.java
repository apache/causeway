package org.apache.isis.incubator.viewer.javafx.ui.main;

import java.util.function.Consumer;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.collections.TableViewFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.object.ObjectViewFx;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.scene.Node;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class UiActionHandler {

    private final IsisInteractionFactory isisInteractionFactory;
    private final UiComponentFactoryFx uiComponentFactory;

    public void handleActionLinkClicked(ManagedAction managedAction, Consumer<Node> onNewPageContent) {

        log.info("about to build an action prompt for {}", managedAction.getIdentifier());
        
        // TODO get an ActionPrompt, then on invocation show the result in the content view

        isisInteractionFactory.runAnonymous(()->{

            //Thread.sleep(1000); // simulate long running

            val actionResultOrVeto = managedAction.invoke(Can.empty());
            
            actionResultOrVeto.left()
            .ifPresent(actionResult->
                    onNewPageContent.accept(uiComponentForActionResult(actionResult, onNewPageContent)));

        });

    }
    
    private Node uiComponentForActionResult(ManagedObject actionResult, Consumer<Node> onNewPageContent) {
        if (actionResult.getSpecification().isParentedOrFreeCollection()) {
            return TableViewFx.fromCollection(actionResult);
        } else {
            return ObjectViewFx.fromObject(
                    uiComponentFactory, 
                    action->handleActionLinkClicked(action, onNewPageContent), 
                    actionResult);
        }
    }


}
