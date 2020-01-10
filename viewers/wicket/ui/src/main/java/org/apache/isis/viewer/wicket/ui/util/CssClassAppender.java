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

package org.apache.isis.viewer.wicket.ui.util;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.commons.internal.base._Strings;

/**
 * Implementation of {@link AttributeAppender} that appends the provided CSS
 * <tt>class</tt> attribute.
 */
public class CssClassAppender extends AttributeAppender {

    private static final long serialVersionUID = 1L;

    public CssClassAppender(final IModel<String> appendModel) {
        super("class", appendModel, " ");
    }

    public CssClassAppender(final String append) {
        this(Model.of(append));
    }

    /**
     * Adds CSS class to tag (providing that the class is non-null and non-empty).
     */
    public static void appendCssClassTo(
            final ComponentTag tag,
            final String cssClass) {
        if(_Strings.isNullOrEmpty(cssClass)) {
            return;
        }
        tag.append("class", cssClass, " ");
    }

    /**
     * Adds CSS class to container (providing that the class is non-null and non-empty).
     */
    public static void appendCssClassTo(
            final MarkupContainer markupContainer,
            final String cssClass) {
        if(_Strings.isNullOrEmpty(cssClass)) {
            return;
        }
        markupContainer.add(new CssClassAppender(cssClass));
    }

    /**
     * Utility method to sanitize string into a single CSS class.
     */
    public static String asCssStyle(final String str) {
        return str.replaceAll("[^A-Za-z0-9- ]", "").replaceAll("\\s+", "-");
    }
}