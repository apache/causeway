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
package org.apache.causeway.incubator.viewer.vaadin.model.decorator;

import java.net.URL;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.commons.applib.services.userprof.UserProfileUiModel;
import org.apache.causeway.viewer.commons.applib.services.userprof.UserProfileUiService;
import org.apache.causeway.viewer.commons.model.decorators.IconDecorator;
import org.apache.causeway.viewer.commons.model.decorators.IconDecorator.FontAwesomeDecorationModel;
import org.apache.causeway.viewer.commons.model.decorators.TooltipDecorator;
import org.apache.causeway.viewer.commons.model.decorators.TooltipDecorator.TooltipDecorationModel;

import lombok.Getter;
import lombok.val;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 *
 */
@UtilityClass
@Log4j2
public class Decorators {

    @Getter(lazy = true) private final static Tooltip tooltip = new Tooltip();
    @Getter(lazy = true) private final static Icon icon = new Icon();
    @Getter(lazy = true) private final static Menu menu = new Menu();
    @Getter(lazy = true) private final static User user = new User();

    // -- DECORATOR CLASSES

    public final static class Tooltip implements TooltipDecorator<Component> {

        @Override
        public void decorate(final Component uiComponent, final TooltipDecorationModel tooltipDecorationModel) {
            log.warn("not implemented yet");
        }

    }

    public final static class Icon implements IconDecorator<Component, Component> {

        @Override
        public Component decorate(
                final Component uiComponent,
                final Optional<FontAwesomeDecorationModel> fontAwesomeDecorationModel) {

            val decoratedUiComponent = fontAwesomeDecorationModel
            .map(fontAwesome->{

                val faIcon = new Span();

                fontAwesome.streamCssClasses()
                .forEach(faIcon::addClassName);

                return CssClassFaPosition.isLeftOrUnspecified(fontAwesome.getPosition())
                        ? new HorizontalLayout(faIcon, uiComponent)
                        : new HorizontalLayout(uiComponent, faIcon);

            })
            .orElseGet(()->{

                // TODO add spacer, to account for missing fa icon?
                // but then where to add, left or right?

                return new HorizontalLayout(uiComponent);
            });

            return (HorizontalLayout)decoratedUiComponent;

        }

    }

    public final static class Menu {

        public Component decorateTopLevel(
                final Label label) {
            val icon = getTopLevelMenuIcon();
            val layout =  new HorizontalLayout(label, icon);
            layout.setVerticalComponentAlignment(Alignment.END, icon);
            return layout;
        }

        private Component getTopLevelMenuIcon() {
            val menuIcon = new com.vaadin.flow.component.icon.Icon(VaadinIcon.CARET_DOWN);
            menuIcon.setSize("1em");
            menuIcon.getElement().getStyle().set("margin-left", "2px");
            return menuIcon;
        }

    }

    public final static class User {

        public Component decorateWithAvatar(
                final Label label,
                final MetaModelContext commonContext) {

            val profileIfAny = commonContext.lookupServiceElseFail(UserProfileUiService.class)
                    .userProfile();
            return decorateWithAvatar(label, Optional.ofNullable(profileIfAny));
        }

        public Component decorateWithAvatar(
                final Label label,
                final Optional<UserProfileUiModel> userProfileUiModel) {

            val decoratedUiComponent = userProfileUiModel
            .map(userProfile->{

                label.setText(userProfile.getUserProfileName());

                val userIcon = userProfile.avatarUrl()
                .map(this::getUserIcon)
                .orElseGet(this::getFallbackUserIcon);

                return (Component) new HorizontalLayout(userIcon, label);

            })
            .orElseGet(()->{
                label.setText("<anonymous>");
                return label;
            });

            return (Component) decoratedUiComponent;

        }

        private Component getUserIcon(final URL avatarUrl) {
            return new Image(avatarUrl.toExternalForm(), "avatar");
        }

        private Component getFallbackUserIcon() {
            val userIcon = new com.vaadin.flow.component.icon.Icon(VaadinIcon.USER);
            userIcon.setSize("1em");
            return userIcon;
        }



    }




}
