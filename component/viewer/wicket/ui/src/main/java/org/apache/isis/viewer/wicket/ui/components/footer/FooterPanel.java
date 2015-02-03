package org.apache.isis.viewer.wicket.ui.components.footer;

import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.themepicker.ThemeChooser;
import org.apache.isis.viewer.wicket.ui.pages.about.AboutPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * A panel for the default page footer
 */
public class FooterPanel extends PanelAbstract<Model<String>> {

    private static final String ID_BREADCRUMBS = "breadcrumbs";
    private static final String ID_ABOUT_LINK = "aboutLink";
    private static final String ID_ABOUT_MESSAGE = "aboutMessage";
    private static final String ID_THEME_PICKER = "themePicker";

    /**
     * Constructor.
     *
     * @param id The component id
     */
    public FooterPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        addBreadcrumbs();
        addAboutLink();
        addThemePicker();
    }

    private void addBreadcrumbs() {
        final BreadcrumbPanel breadcrumbPanel = new BreadcrumbPanel(ID_BREADCRUMBS);
        addOrReplace(breadcrumbPanel);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        PageParameters parameters = getPage().getPageParameters();
        setVisible(parameters.get(PageParametersUtils.ISIS_NO_FOOTER_PARAMETER_NAME).isNull());
    }

    private void addAboutLink() {
        final BookmarkablePageLink<Void> aboutLink = new BookmarkablePageLink<>(ID_ABOUT_LINK, AboutPage.class);
        add(aboutLink);

        final Label aboutLabel = new Label(ID_ABOUT_MESSAGE, new ResourceModel("aboutLabel"));
        aboutLink.add(aboutLabel);
        addDevModeWarning(aboutLink);
    }

    /**
     * Adds a component that shows a warning sign next to "About" link in development mode
     * @param container The parent component
     */
    private void addDevModeWarning(final MarkupContainer container) {
        final WebComponent devModeWarning = new WebComponent("devModeWarning");
        devModeWarning.setVisible(getApplication().usesDevelopmentConfig());
        container.add(devModeWarning);
    }

    private void addThemePicker() {
        final ThemeChooser themeChooser = new ThemeChooser(ID_THEME_PICKER);
        addOrReplace(themeChooser);
    }
}
