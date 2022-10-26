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
package org.apache.causeway.viewer.wicket.ui.pages;

import java.io.Serializable;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.viewer.wicket.model.models.PageType;

/**
 * A service helping with the navigation to pages by type
 */
public interface PageNavigationService extends Serializable {

    /**
     * Schedules a page with a given {@link org.apache.causeway.viewer.wicket.model.models.PageType type}
     * to provide the response
     *
     * @param pageType The type of the page that should provide the response
     */
    void navigateTo(PageType pageType);

    /**
     * Schedules a page with a given {@link org.apache.causeway.viewer.wicket.model.models.PageType type}
     * and {@link org.apache.wicket.request.mapper.parameter.PageParameters parameters} to provide the response
     *
     * @param pageType The type of the page that should provide the response
     * @param parameters The page parameters for the page that should provide the response
     */
    void navigateTo(PageType pageType, PageParameters parameters);

    /**
     * Restarts the current request cycle and schedules another page to provide the response
     *
     * @param pageType The type of the page that should provide the response
     */
    void restartAt(PageType pageType);

    /**
     * Restarts the current request cycle and schedules another page to provide the response.
     * Information about the current page url and request parameters is preserved so that
     * the application can return later by using {@link org.apache.wicket.Component#continueToOriginalDestination()}
     *
     * @param pageType The type of the page that should provide the response
     */
    void interceptAndRestartAt(PageType pageType);
}
