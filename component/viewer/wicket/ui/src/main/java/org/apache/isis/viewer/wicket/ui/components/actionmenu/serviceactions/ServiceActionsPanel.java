package org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipBehavior;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.button.DropdownAutoOpenJavaScriptReference;

import java.util.List;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.CssClassFaBehavior;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * A panel responsible to render the application actions as menu in a navigation bar.
 *
 * <p>
 *     The multi-level sub menu support is borrowed from
 *     <a href="http://bootsnipp.com/snippets/featured/multi-level-dropdown-menu-bs3">Bootsnip</a>
 * </p>
 */
public class ServiceActionsPanel extends Panel {

    public ServiceActionsPanel(String id, List<CssMenuItem> menuItems) {
        super(id);

        ListView<CssMenuItem> menuItemsView = new ListView<CssMenuItem>("menuItems", menuItems) {

            @Override
            protected void populateItem(ListItem<CssMenuItem> listItem) {
                CssMenuItem menuItem = listItem.getModelObject();
                listItem.add(new Label("name", menuItem.getName()));

                MarkupContainer topMenu = new WebMarkupContainer("topMenu");

                topMenu.add(new CssClassAppender("top-menu-" + CssClassAppender.asCssStyle(menuItem.getName())));
                listItem.add(topMenu);
                List<CssMenuItem> subMenuItems = withSeparators(menuItem);

// fake data to test multi-level menus
//                if (menuItem.getName().equals("ToDos")) {
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
                topMenu.add(subMenuItemsView);
            }
        };
        add(menuItemsView);
    }

    private void addFolderItem(CssMenuItem subMenuItem, ListItem<CssMenuItem> listItem) {

        listItem.add(new CssClassAppender("dropdown-submenu"));

        Fragment folderItem = new Fragment("content", "folderItem", ServiceActionsPanel.this);
        listItem.add(folderItem);

        folderItem.add(new Label("folderName", subMenuItem.getName()));
        final List<CssMenuItem> menuItems = withSeparators(subMenuItem);
        ListView<CssMenuItem> subMenuItemsView = new ListView<CssMenuItem>("subMenuItems",
                menuItems) {
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

    private static List<CssMenuItem> withSeparators(CssMenuItem subMenuItem) {
        final List<CssMenuItem> subMenuItems = subMenuItem.getSubMenuItems();
        return withSeparators(subMenuItems);
    }

    private static List<CssMenuItem> withSeparators(List<CssMenuItem> subMenuItems) {
        final List<CssMenuItem> itemsWithSeparators = Lists.newArrayList();
        for (CssMenuItem menuItem : subMenuItems) {
            if(menuItem.isSeparator() ) {
                if(!itemsWithSeparators.isEmpty()) {
                    // bit nasty... we add a new separator item
                    itemsWithSeparators.add(
                            CssMenuItem.newMenuItem(menuItem.getName() + "-separator")
                                    .separator(menuItem.isSeparator())
                                    .prototyping(menuItem.isPrototyping())
                                    .build());
                }
                menuItem.setSeparator(false);
            }
            itemsWithSeparators.add(menuItem);
        }
        return itemsWithSeparators;
    }

    private void addLeafItem(
            final CssMenuItem menuItem,
            final ListItem<CssMenuItem> listItem) {

        Fragment leafItem;
        if (!menuItem.isSeparator()) {
            leafItem = new Fragment("content", "leafItem", ServiceActionsPanel.this);

            AbstractLink subMenuItemLink = menuItem.getLink();

            Label menuItemLabel = new Label("menuLinkLabel", menuItem.getName());
            subMenuItemLink.addOrReplace(menuItemLabel);

            if (!menuItem.isEnabled()) {
                listItem.add(new CssClassAppender("disabled"));
                subMenuItemLink.setEnabled(false);
                TooltipBehavior tooltipBehavior = new TooltipBehavior(Model.of(menuItem.getDisabledReason()));
                listItem.add(tooltipBehavior);
            }
            if (menuItem.isPrototyping()) {
                subMenuItemLink.add(new CssClassAppender("prototype"));
            }
            leafItem.add(subMenuItemLink);

            String cssClassFa = menuItem.getCssClassFa();
            if (Strings.isNullOrEmpty(cssClassFa)) {
                subMenuItemLink.add(new CssClassAppender("menuLinkSpacer"));
            } else {
                menuItemLabel.add(new CssClassFaBehavior(cssClassFa, menuItem.getCssClassFaPosition()));
            }
        } else {
            leafItem = new Fragment("content", "empty", ServiceActionsPanel.this);
            listItem.add(new CssClassAppender("divider"));
        }
        listItem.add(leafItem);

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new CssResourceReference(ServiceActionsPanel.class, "ServiceActionsPanel.css")));
        response.render(JavaScriptHeaderItem.forReference(DropdownAutoOpenJavaScriptReference.instance()));
        response.render(OnDomReadyHeaderItem.forScript("$('.dropdown-toggle').dropdownHover();"));
    }

}
