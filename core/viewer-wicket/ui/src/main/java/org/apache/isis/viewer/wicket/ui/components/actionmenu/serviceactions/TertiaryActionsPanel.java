package org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipBehavior;

import java.util.List;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.CssClassFaBehavior;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * A panel responsible to render the application actions as menu in a navigation bar.
 *
 * <p>
 *     The multi-level sub menu support is borrowed from
 *     <a href="http://bootsnipp.com/snippets/featured/multi-level-dropdown-menu-bs3">Bootsnip</a>
 * </p>
 */
public class TertiaryActionsPanel extends Panel {

    public TertiaryActionsPanel(String id, List<CssMenuItem> menuItems) {
        super(id);

        addLogoutLink(this);

        final List<CssMenuItem> subMenuItems = flatten(menuItems);

        final ListView<CssMenuItem> subMenuItemsView = new ListView<CssMenuItem>("subMenuItems", subMenuItems) {
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

        WebComponent divider = new WebComponent("divider") {
            @Override
            protected void onConfigure() {
                super.onConfigure();

                subMenuItemsView.configure();
                setVisible(!subMenuItems.isEmpty());
            }
        };

        add(subMenuItemsView, divider);
    }

    protected List<CssMenuItem> flatten(List<CssMenuItem> menuItems) {
        List<CssMenuItem> subMenuItems = Lists.newArrayList();
        for (CssMenuItem menuItem : menuItems) {
            subMenuItems.addAll(menuItem.getSubMenuItems());
        }
        return subMenuItems;
    }

    private void addLogoutLink(MarkupContainer themeDiv) {
        Link logoutLink = new Link("logoutLink") {

            @Override
            public void onClick() {
                getSession().invalidate();
                setResponsePage(getSignInPage());
            }
        };
        themeDiv.add(logoutLink);
    }

    private Class<? extends Page> getSignInPage() {
        return pageClassRegistry.getPageClass(PageType.SIGN_IN);
    }


    private void addFolderItem(CssMenuItem subMenuItem, ListItem<CssMenuItem> listItem) {

        listItem.add(new CssClassAppender("dropdown-submenu"));

        Fragment folderItem = new Fragment("content", "folderItem", TertiaryActionsPanel.this);
        listItem.add(folderItem);

        folderItem.add(new Label("folderName", subMenuItem.getName()));
        final List<CssMenuItem> subMenuItems = subMenuItem.getSubMenuItems();
        final List<CssMenuItem> menuItems = subMenuItems;
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

    private void addLeafItem(
            final CssMenuItem menuItem,
            final ListItem<CssMenuItem> listItem) {

        Fragment leafItem;
        if (!menuItem.isSeparator()) {
            leafItem = new Fragment("content", "leafItem", TertiaryActionsPanel.this);

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
            leafItem = new Fragment("content", "empty", TertiaryActionsPanel.this);
            listItem.add(new CssClassAppender("divider"));
        }
        listItem.add(leafItem);

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new CssResourceReference(TertiaryActionsPanel.class, "TertiaryActionsPanel.css")));
    }

    /**
     * {@link com.google.inject.Inject}ed when {@link #init() initialized}.
     */
    @Inject
    private PageClassRegistry pageClassRegistry;

}
