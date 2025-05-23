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
package org.apache.causeway.viewer.wicket.viewer.registries.pages;

import jakarta.inject.Named;

import org.apache.wicket.Page;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassList;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistrySpi;
import org.apache.causeway.viewer.wicket.ui.pages.about.AboutPage;
import org.apache.causeway.viewer.wicket.ui.pages.accmngt.password_reset.PasswordResetPage;
import org.apache.causeway.viewer.wicket.ui.pages.accmngt.register.RegisterPage;
import org.apache.causeway.viewer.wicket.ui.pages.accmngt.signup.RegistrationFormPage;
import org.apache.causeway.viewer.wicket.ui.pages.home.HomePage;
import org.apache.causeway.viewer.wicket.ui.pages.login.WicketSignInPage;
import org.apache.causeway.viewer.wicket.ui.pages.obj.DomainObjectPage;
import org.apache.causeway.viewer.wicket.ui.pages.standalonecollection.StandaloneCollectionPage;
import org.apache.causeway.viewer.wicket.ui.pages.value.ValuePage;
import org.apache.causeway.viewer.wicket.ui.pages.voidreturn.VoidReturnPage;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;

public class PageClassListDefault implements PageClassList {

    /**
     * Default implementation of {@link PageClassList}, specifying the default pages
     * for each of the {@link PageType}s.
     */
    @Configuration
    public static class AutoConfiguration {

        @Bean
        @Named(CausewayModuleViewerWicketViewer.NAMESPACE + ".PageClassListDefault")
        @ConditionalOnMissingBean(PageClassList.class)
        @Order(PriorityPrecedence.MIDPOINT)
        @Qualifier("Default")
        public PageClassList pageClassListDefault() {
            return new PageClassListDefault();
        }
    }

    private static final long serialVersionUID = 1L;

    @Override
    public void registerPages(final PageClassRegistrySpi pageRegistry) {
        pageRegistry.registerPage(PageType.SIGN_IN, getSignInPageClass());
        pageRegistry.registerPage(PageType.SIGN_UP, getSignUpPageClass());
        pageRegistry.registerPage(PageType.SIGN_UP_VERIFY, getSignUpVerifyPageClass());
        pageRegistry.registerPage(PageType.PASSWORD_RESET, getPasswordResetPageClass());
        pageRegistry.registerPage(PageType.ABOUT, getAboutPageClass());
        pageRegistry.registerPage(PageType.DOMAIN_OBJECT, getDomainObjectPageClass());
        pageRegistry.registerPage(PageType.HOME, getHomePageClass());
        pageRegistry.registerPage(PageType.HOME_AFTER_PAGETIMEOUT, getHomePageClass());
        pageRegistry.registerPage(PageType.STANDALONE_COLLECTION, getStandaloneCollectionPageClass());
        pageRegistry.registerPage(PageType.VALUE, getValuePageClass());
        pageRegistry.registerPage(PageType.VOID_RETURN, getVoidReturnPageClass());
    }

    /**
     * For subclassing if required.
     */
    protected Class<? extends Page> getDomainObjectPageClass() {
        return DomainObjectPage.class;
    }

    /**
     * For subclassing if required.
     */
    protected Class<? extends Page> getStandaloneCollectionPageClass() {
        return StandaloneCollectionPage.class;
    }

    /**
     * For subclassing if required.
     */
    protected Class<? extends Page> getValuePageClass() {
        return ValuePage.class;
    }

    /**
     * For subclassing if required.
     */
    protected Class<? extends Page> getVoidReturnPageClass() {
        return VoidReturnPage.class;
    }

    /**
     * For subclassing if required.
     */
    protected Class<? extends Page> getSignInPageClass() {
        return WicketSignInPage.class;
    }

    /**
     * For subclassing if required.
     */
    protected Class<? extends Page> getSignUpPageClass() {
        return RegistrationFormPage.class;
    }

    /**
     * For subclassing if required.
     */
    protected Class<? extends Page> getSignUpVerifyPageClass() {
        return RegisterPage.class;
    }

    /**
     * For subclassing if required.
     */
    protected Class<? extends Page> getPasswordResetPageClass() {
        return PasswordResetPage.class;
    }

    /**
     * For subclassing if required.
     */
    protected Class<? extends Page> getHomePageClass() {
        return HomePage.class;
    }

    /**
     * For subclassing if required.
     */
    protected Class<? extends Page> getAboutPageClass() {
        return AboutPage.class;
    }
}
