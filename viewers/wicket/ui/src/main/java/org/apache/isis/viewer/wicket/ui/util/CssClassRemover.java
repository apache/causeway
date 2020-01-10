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

import java.util.stream.Collectors;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.Model;

import org.apache.isis.core.commons.internal.base._Strings;

/**
 * Implementation of {@link AttributeAppender} that appends the provided CSS
 * <tt>class</tt> attribute.
 */
public class CssClassRemover extends AttributeModifier {

    private static final long serialVersionUID = 1L;

    public CssClassRemover(String cssClass) {
        super("class", new Model<String>(cssClass));
    }

    @Override
    protected String newValue(String currentValue, String valueToRemove) {
        if (currentValue == null) return "";

        return _Strings.splitThenStream(currentValue, " ")
        .filter(x->x.equals(valueToRemove))
        .collect(Collectors.joining(" "));
    }

}