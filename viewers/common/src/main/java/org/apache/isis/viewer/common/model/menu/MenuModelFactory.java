package org.apache.isis.viewer.common.model.menu;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.isis.applib.layout.menubars.bootstrap3.BS3Menu;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBar;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.viewer.common.model.action.MenuActionFactory;
import org.apache.isis.viewer.common.model.menuitem.MenuItemUiModel;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MenuModelFactory {

    public static <T, M extends MenuItemUiModel<T, M>> 
    void buildMenuItems(
            final IsisWebAppCommonContext commonContext,
            final BS3MenuBar menuBar,
            final MenuActionFactory<T> menuActionFactory,
            final Function<String, M> menuItemFactory,
            final Consumer<M> onNewMenuItem) {

        // we no longer use ServiceActionsModel#getObject() because the model only holds the services for the
        // menuBar in question, whereas the "Other" menu may reference a service which is defined for some other menubar

        val itemsPerSectionCounter = new LongAdder();
        
        for (final BS3Menu menu : menuBar.getMenus()) {

            val menuItemModel = menuItemFactory.apply(menu.getNamed()); 

            for (val menuSection : menu.getSections()) {

                itemsPerSectionCounter.reset();
                
                for (val actionLayoutData : menuSection.getServiceActions()) {
                    val serviceSpecId = actionLayoutData.getObjectType();

                    val serviceAdapter = commonContext.lookupServiceAdapterById(serviceSpecId);
                    if(serviceAdapter == null) {
                        // service not recognized, presumably the menu layout is out of sync with actual configured modules
                        continue;
                    }

                    val objectAction =
                            serviceAdapter
                                    .getSpecification()
                                    .getObjectAction(actionLayoutData.getId())
                                    .orElse(null);
                    if (objectAction == null) {
                        log.warn("No such action {}", actionLayoutData.getId());
                        continue;
                    }

                    val isFirstInSection = itemsPerSectionCounter.intValue()==0; 
                    
                    val menuActionUiModel = menuActionFactory.newMenuAction(
                            commonContext,
                            actionLayoutData.getNamed(),
                            objectAction,
                            serviceAdapter);

                    // Optionally creates a sub-menu item based on visibility and usability
                    menuItemModel
                    .addSubMenuItemFor(
                        menuActionUiModel, 
                        isFirstInSection,
                        newSubMenuItem->{
                            // increment counter only when a sub item was actually added
                            itemsPerSectionCounter.increment();
                            newSubMenuItem.setMenuActionUiModel(menuActionUiModel);
                    });
                    
                }
            }
            if (menuItemModel.hasSubMenuItems()) {
                onNewMenuItem.accept(menuItemModel);
            }
        }
        
    }

    
    
}
