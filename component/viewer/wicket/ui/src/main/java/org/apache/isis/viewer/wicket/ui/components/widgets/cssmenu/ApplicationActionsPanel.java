package org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu;

import java.util.List;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 *
 */
public class ApplicationActionsPanel extends Panel {

    public ApplicationActionsPanel(String id, List<CssMenuItem> menuItems) {
        super(id);

        ListView<CssMenuItem> menuItemsView = new ListView<CssMenuItem>("menuItems", menuItems) {

            @Override
            protected void populateItem(ListItem<CssMenuItem> listItem) {
                CssMenuItem menuItem = listItem.getModelObject();
                listItem.add(new Label("name", menuItem.getName()));

                List<CssMenuItem> subMenuItems = menuItem.getSubMenuItems();
                ListView<CssMenuItem> subMenuItemsView = new ListView<CssMenuItem>("subMenuItems", subMenuItems) {
                    @Override
                    protected void populateItem(ListItem<CssMenuItem> listItem) {
                        CssMenuItem subMenuItem = listItem.getModelObject();
                        AbstractLink subMenuItemLink = subMenuItem.getLink();
                        subMenuItemLink.setBody(Model.of(subMenuItem.getName()));
                        listItem.add(subMenuItemLink);
                    }
                };
                listItem.add(subMenuItemsView);
            }
        };
        add(menuItemsView);

    }
}
