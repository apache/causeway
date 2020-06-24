package org.apache.isis.incubator.viewer.vaadin.ui.pages.main;

import java.util.function.Consumer;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.menubar.MenuBar;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.incubator.viewer.vaadin.model.action.ActionLinkFactoryVaa;
import org.apache.isis.incubator.viewer.vaadin.model.decorator.Decorators;
import org.apache.isis.viewer.common.model.menu.MenuItemDto;
import org.apache.isis.viewer.common.model.menu.MenuVisitor;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "of") 
class MenuBuilderVaa implements MenuVisitor {

    private final IsisAppCommonContext commonContext; 
    private final Consumer<ManagedAction> subMenuEventHandler;
    private final MenuBar menuBar;

    private MenuItem currentTopLevelMenu = null;
    private ActionLinkFactoryVaa actionLinkFactory = new ActionLinkFactoryVaa();

    @Override
    public void addTopLevel(MenuItemDto menuDto) {

        if(menuDto.isTertiaryRoot()) {
            currentTopLevelMenu = menuBar.addItem(Decorators.getUser()
                    .decorateWithAvatar(new Label(), commonContext));
        } else {
            currentTopLevelMenu = menuBar.addItem(Decorators.getMenu()
                    .decorateTopLevel(new Label(menuDto.getName())));
        }
    }

    @Override
    public void addSubMenu(MenuItemDto menu) {
        val managedAction = menu.getManagedAction();
        val actionLink = actionLinkFactory.newActionLink(menu.getName(), managedAction);
        currentTopLevelMenu.getSubMenu()
        .addItem(actionLink.getUiComponent(), e->subMenuEventHandler.accept(managedAction));
    }

    @Override
    public void addSectionSpacer() {
        val spacer = new Hr();
        //spacer.addClassName("spacer"); TODO vertical margin or padding is currently a bit too large 
        currentTopLevelMenu.getSubMenu()
        .addItem(spacer);
    }

}