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

import org.apache.isis.commons.internal.base._Strings;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Implementation of {@link AttributeAppender} that appends the provided CSS
 * <tt>id</tt> attribute.
 */
public class CssIdAppender extends AttributeAppender {

    private static final long serialVersionUID = 1L;

    public CssIdAppender(final IModel<String> appendModel) {
        super("id", appendModel, " ");
    }

    public CssIdAppender(final String append) {
        this(Model.of(append));
    }

    /**
     * Adds CSS id to tag (providing that the id is non-null and non-empty).
     */
    public static void appendCssIdTo(
            final ComponentTag tag,
            final String cssId) {
        if(_Strings.isNullOrEmpty(cssId)) {
            return;
        }
        tag.append("id", cssId, " ");
    }

    /**
     * Adds CSS class to container (providing that the class is non-null and non-empty).
     */
    public static void appendCssIdTo(
            final MarkupContainer markupContainer,
            final String cssClass) {
        if(_Strings.isNullOrEmpty(cssClass)) {
            return;
        }
        markupContainer.add(new CssIdAppender(cssClass));
    }

}