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

package org.apache.isis.viewer.wicket.ui.pages;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;

import org.apache.isis.viewer.wicket.ui.pages.action.ActionPage;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.pages.home.HomePage;

/**
 * Enumerates the different types of pages that can be rendered.
 * 
 * <p>
 * Is used by {@link PageClassRegistry} to lookup the concrete page to render
 * different types of pages. This allows the large-scale structure of page
 * layout (eg headers, footers) to be altered.
 */
public enum PageType {
    SIGN_IN(WebPage.class), 
    HOME(HomePage.class), 
    //ABOUT(AboutPage.class), 
    ENTITY(EntityPage.class), 
    ACTION(ActionPage.class);

    private Class<? extends Page> superClass;

    private PageType() {
        this(Page.class);
    }

    private PageType(final Class<? extends Page> pageClass) {
        this.superClass = pageClass;
    }

    /**
     * The class that pages registered against this page type must be assignable
     * from (ie have as their superclass).
     * 
     * <p>
     * This allows us to perform fail-fast checking when pages are registered,
     * rather than when they are used.
     */
    public Class<? extends Page> getPageClass() {
        return superClass;
    }
}
