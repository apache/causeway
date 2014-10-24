package org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.button.DropdownAutoOpenJavaScriptReference;

import java.util.List;
import com.google.common.base.Strings;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * A panel responsible to render the application actions as menu in a navigation bar.
 *
 * <p>
 *     The multi-level sub menu support is borrowed from
 *     <a href="http://bootsnipp.com/snippets/featured/multi-level-dropdown-menu-bs3">Bootsnip</a>
 * </p>
 */
public class ApplicationActionsPanel extends Panel {

    /**
     * Constructor.
     *
     * @param id
     *          the Wicket component id
     * @param menuItems
     *          the menu items with their sub menu items
     */
    public ApplicationActionsPanel(String id, List<CssMenuItem> menuItems) {
        super(id);

        ListView<CssMenuItem> menuItemsView = new ListView<CssMenuItem>("menuItems", menuItems) {

            @Override
            protected void populateItem(ListItem<CssMenuItem> listItem) {
                CssMenuItem menuItem = listItem.getModelObject();
                listItem.add(new Label("name", menuItem.getName()));

                List<CssMenuItem> subMenuItems = menuItem.getSubMenuItems();

// fake data to test multi-level menus

//                if (menuItem.getName().equals("Text")) {
//                    CssMenuItem fakeItem = menuItem.newSubMenuItem("Fake item").build();
//
//                    fakeItem.newSubMenuItem("Fake item 1").link(new ExternalLink("menuLink", "http://abv.bg")).build();
//                    CssMenuItem fakeMenu12 = fakeItem.newSubMenuItem("Fake item 2").link(new ExternalLink("menuLink", "http://google.com")).build();
//
//                    fakeMenu12.newSubMenuItem("Fake item 2.1").link(new ExternalLink("menuLink", "http://web.de")).build();
//                }

                ListView<CssMenuItem> subMenuItemsView = new ListView<CssMenuItem>("subMenuItems", subMenuItems) {
                    @Override
                    protected void populateItem(ListItem<CssMenuItem> listItem) {
                        CssMenuItem subMenuItem = listItem.getModelObject();

                        if (subMenuItem.hasSubMenuItems()) {
                            addFolderItem(subMenuItem, listItem);
                        } else {
                            addLeafItem(subMenuItem, listItem);
                        }
                    }
                };
                listItem.add(subMenuItemsView);
            }
        };
        add(menuItemsView);
    }

    private void addFolderItem(CssMenuItem subMenuItem, ListItem<CssMenuItem> listItem) {

        listItem.add(new CssClassAppender("dropdown-submenu"));

        Fragment folderItem = new Fragment("content", "folderItem", ApplicationActionsPanel.this);
        listItem.add(folderItem);

        folderItem.add(new Label("folderName", subMenuItem.getName()));
        ListView<CssMenuItem> subMenuItemsView = new ListView<CssMenuItem>("subMenuItems", subMenuItem.getSubMenuItems()) {
            @Override
            protected void populateItem(ListItem<CssMenuItem> listItem) {
                CssMenuItem subMenuItem = listItem.getModelObject();

                if (subMenuItem.hasSubMenuItems()) {
                    addFolderItem(subMenuItem, listItem);
                } else {
                    addLeafItem(subMenuItem, listItem);
                }
            }
        };
        folderItem.add(subMenuItemsView);
    }

    private void addLeafItem(CssMenuItem menuItem, ListItem<CssMenuItem> listItem) {
        Fragment leafItem = new Fragment("content", "leafItem", ApplicationActionsPanel.this);

        AbstractLink subMenuItemLink = menuItem.getLink();

        subMenuItemLink.setBody(Model.of(menuItem.getName()));
        if (!menuItem.isEnabled()) {
            listItem.add(new CssClassNameAppender("disabled"));
        }
        if (menuItem.isPrototyping()) {
            listItem.add(new CssClassNameAppender("bg-warning"));
        }
        leafItem.add(subMenuItemLink);
        listItem.add(leafItem);

        String cssClassFa = menuItem.getCssClassFa();
        if (Strings.isNullOrEmpty(cssClassFa)) {
            Components.permanentlyHide(leafItem, "menuLinkFontAwesome");
            subMenuItemLink.add(new CssClassAppender("menuLinkSpacer"));
        } else {
            Label dummy = new Label("menuLinkFontAwesome", "");
            dummy.add(new CssClassAppender(cssClassFa));
            leafItem.add(dummy);
        }

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new CssResourceReference(ApplicationActionsPanel.class, "ApplicationActionsPanel.css")));
        response.render(JavaScriptHeaderItem.forReference(DropdownAutoOpenJavaScriptReference.instance()));
        response.render(OnDomReadyHeaderItem.forScript("$('.dropdown-toggle').dropdownHover();"));
    }
}
