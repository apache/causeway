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

import java.io.Serializable;

import org.apache.wicket.Page;

import org.apache.isis.viewer.wicket.model.models.PageType;

/**
 * A Wicket specific service that may be used to create a link to a
 * page by {@link org.apache.isis.viewer.wicket.model.models.PageType page type}
 * with encoded/encrypted datum as first indexed parameter in the url for
 * mail verification purposes.
 */
public interface EmailVerificationUrlService extends Serializable {

    /**
     * Creates a url to the passed <em>pageType</em> by encrypting the given
     * <em>datum</em> as a first indexed parameter
     *
     * @param pageType The type of the page to link to
     * @param datum The data to encrypt in the url
     * @return The full url to the page with the encrypted data
     */
    String createVerificationUrl(PageType pageType, String datum);

    /**
     * Creates a url to the passed <em>pageClass</em> by encrypting the given
     * <em>datum</em> as a first indexed parameter
     *
     * @param pageClass The class of the page to link to
     * @param datum The data to encrypt in the url
     * @return The full url to the page with the encrypted data
     */
    String createVerificationUrl(final Class<? extends Page> pageClass, final String datum);
}
