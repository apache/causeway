package org.apache.isis.incubator.viewer.vaadin.ui.pages.main;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBar;
import org.apache.isis.applib.services.menu.MenuBarsService.Type;
import org.apache.isis.core.runtimeservices.menubars.bootstrap3.MenuBarsServiceBS3;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.incubator.viewer.vaadin.model.action.MenuActionFactoryVaa;
import org.apache.isis.incubator.viewer.vaadin.model.action.MenuActionVaa;
import org.apache.isis.incubator.viewer.vaadin.model.menu.MenuItemVaa;
import org.apache.isis.viewer.common.model.menu.MenuModelFactory;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
//@Log4j2
final class MenuUtil {

    static Component createMenu(
            final IsisWebAppCommonContext commonContext, 
            final MenuBarsServiceBS3 menuBarsService,
            final Consumer<MenuActionVaa> subMenuEventHandler) {
        
        val titleOrLogo = createTitleOrLogo(commonContext);
        val leftMenuBar = new MenuBar();
        val horizontalSpacer = new Div();
//        horizontalSpacer.setWidthFull();
        val rightMenuBar = new MenuBar();
        
        leftMenuBar.setOpenOnHover(true);
        rightMenuBar.setOpenOnHover(true);
        
        // holds the top level left and right aligned menu parts
        // TODO does not honor small displays yet, overflow is just not visible
        val menuBarContainer = new FlexLayout(titleOrLogo, leftMenuBar, horizontalSpacer, rightMenuBar);
        menuBarContainer.setWrapMode(FlexLayout.WrapMode.WRAP);
        menuBarContainer.setAlignSelf(Alignment.CENTER, leftMenuBar);
        menuBarContainer.setAlignSelf(Alignment.CENTER, rightMenuBar);

        // right align using css
        rightMenuBar.getStyle().set("margin-left", "auto");
        
        menuBarContainer.setWidthFull();
        
        val bs3MenuBars = menuBarsService.menuBars(Type.DEFAULT);
        
        // menu section handler, that creates and adds sub-menus to their parent top level menu   
        final BiConsumer<MenuBar, MenuItemVaa> menuSectionBuilder = (parentMenu, menuSectionUiModel) -> {
            val menuItem = parentMenu.addItem(menuSectionUiModel.getName());
            val subMenu = menuItem.getSubMenu();
            menuSectionUiModel.getSubMenuItems().forEach(menuItemModel -> {
                val menuActionModel = (MenuActionVaa)menuItemModel.getMenuActionUiModel();
                
                if(menuActionModel.isFirstInSection() 
                        && subMenu.getItems().size()>0) {
                    subMenu.addItem(new Hr());
                }
                
                subMenu.addItem(
                        (Component)menuItemModel.getActionLinkComponent(), 
                        e->subMenuEventHandler.accept(menuActionModel));
            });
                    
        };
        
        // top level left aligned ...
        buildMenuModel(commonContext, bs3MenuBars.getPrimary(), newMenuItem->
            menuSectionBuilder.accept(leftMenuBar, newMenuItem));
        
        // top level right aligned ...
        buildMenuModel(commonContext, bs3MenuBars.getSecondary(), newMenuItem->
            menuSectionBuilder.accept(rightMenuBar, newMenuItem));
        // TODO tertiary menu items should get collected under a top level menu labeled with the current user's name 
        buildMenuModel(commonContext, bs3MenuBars.getTertiary(), newMenuItem->
            menuSectionBuilder.accept(rightMenuBar, newMenuItem));
        
        return menuBarContainer;
        
    }
    
    // -- HELPER

    private static Component createTitleOrLogo(IsisWebAppCommonContext commonContext) {
        
        val isisConfiguration = commonContext.getConfiguration(); 
        val webAppContextPath = commonContext.getWebAppContextPath();
        
        //TODO application name/logo borrowed from Wicket's configuration, we might generalize this config option to all viewers
        val applicationName = isisConfiguration.getViewer().getWicket().getApplication().getName();
        val applicationLogo = isisConfiguration.getViewer().getWicket().getApplication().getBrandLogoHeader();
        
        if(applicationLogo.isPresent()) {
            val logo = new Image(webAppContextPath.prependContextPathIfLocal(applicationLogo.get()), "logo");
            logo.setWidth("48px");
            logo.setHeight("48px");
            return logo;
        }
        
        return new Text(applicationName);
        
    }
    
    private static void buildMenuModel(
            final IsisWebAppCommonContext commonContext,
            final BS3MenuBar menuBar,
            final Consumer<MenuItemVaa> onNewMenuItem) {
        
        MenuModelFactory.buildMenuItems(
                commonContext, 
                menuBar,
                new MenuActionFactoryVaa(),
                MenuItemVaa::newMenuItem,
                onNewMenuItem);
    }
    
    
}
