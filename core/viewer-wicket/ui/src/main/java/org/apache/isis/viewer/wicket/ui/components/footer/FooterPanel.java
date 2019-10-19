/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.footer;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.themepicker.ThemeChooser;
import org.apache.isis.viewer.wicket.ui.pages.about.AboutPage;
import org.apache.isis.viewer.wicket.ui.pages.home.HomePage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

/**
 * A panel for the default page footer
 */
public class FooterPanel extends PanelAbstract<Model<String>> {

    private static final long serialVersionUID = 1L;

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

    static class Credit {
        private final int num;
        private final String url;
        private final String name;
        private final String image;

        final boolean defined;
        private Credit(final int num, final String url, final String name, final String image) {
            this.num = num;
            this.url = url;
            this.name = name;
            this.image = image;
            this.defined = (name != null || image != null) && url != null;
        }

        int getNum() {
            return num;
        }

        boolean isDefined() {
            return defined;
        }

        String getId() {
            return idFor("");
        }

        String getUrl() {
            return url;
        }
        String getUrlId() {
            return idFor("Url");
        }

        String getName() {
            return name;
        }
        String getNameId() {
            return idFor("Name");
        }

        String getImage() {
            return image;
        }
        String getImageId() {
            return idFor("Image");
        }

        private String idFor(final String component) {
            return "credit" + num + component;
        }

        public static Credit create(final IsisConfigurationLegacy configurationLegacy, final int num) {
            String base = "isis.viewer.wicket.credit." + num + ".";
            String url = configurationLegacy.getString(base + "url");
            String name = configurationLegacy.getString(base + "name");
            String image = configurationLegacy.getString(base + "image");
            return new Credit(num, url, name, image);
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        addBreadcrumbs();
        addCredits();
        addAboutLink();
        addThemePicker();
    }

    private void addCredits() {
        boolean credits = false;
        credits = addCredit(1) || credits;
        credits = addCredit(2) || credits;
        credits = addCredit(3) || credits;
        final Label creditsLabel = new Label("creditsLabel", "Credits: ");
        add(creditsLabel);
        creditsLabel.setVisibilityAllowed(credits);
    }

    private boolean addCredit(final int num) {
        final Credit credit = Credit.create(super.getCommonContext().getConfigurationLegacy(), num);
        final WebMarkupContainer creditLink = newLink(credit);
        if(credit.isDefined()) {
            creditLink.add(new CreditImage(credit.getImageId(), credit.getImage()));
            creditLink.add(new CreditName(credit.getNameId(), credit.getName()));

            add(creditLink);
        } else {
            Components.permanentlyHide(this, credit.getId());
        }
        return credit.isDefined();
    }

    private WebMarkupContainer newLink(final Credit credit) {
        final WebMarkupContainer creditLink;
        final String url = credit.getUrl();
        final String creditId = credit.getId();
        if(url != null) {
            creditLink = new ExternalLink(creditId, url) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag)
                {
                    super.onComponentTag(tag);
                    tag.put("target", "_blank");
                }
            }   ;
        } else {
            creditLink = new BookmarkablePageLink<>(creditId, HomePage.class);
        }
        return creditLink;
    }

    private void addBreadcrumbs() {

        boolean showBreadcrumbs = getConfiguration().getViewer().getWicket().getBreadcrumbs().isShowChooser();
        final Component breadcrumbPanel =
                showBreadcrumbs
                ? new BreadcrumbPanel(ID_BREADCRUMBS)
                        : new EmptyPanel(ID_BREADCRUMBS).setVisible(false);
                addOrReplace(breadcrumbPanel);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        final boolean showFooter = getConfiguration().getViewer().getWicket().isShowFooter();

        if(!showFooter) {
            setVisible(false);
            return;
        }

        PageParameters parameters = getPage().getPageParameters();
        setVisible(parameters.get(PageParametersUtils.ISIS_NO_FOOTER_PARAMETER_NAME).isNull());
    }

    private void addAboutLink() {
        final BookmarkablePageLink<Void> aboutLink = new BookmarkablePageLink<>(ID_ABOUT_LINK, AboutPage.class);
        add(aboutLink);

        String applicationVersion = webAppConfigBean.getApplicationVersion();

        final Label aboutLabel =
                applicationVersion != null && !applicationVersion.isEmpty()?
                        new Label(ID_ABOUT_MESSAGE,  applicationVersion) :
                            new Label(ID_ABOUT_MESSAGE,  new ResourceModel("aboutLabel"))
                            ;
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
