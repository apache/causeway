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
package org.apache.isis.viewer.wicket.ui.panels;

import com.google.common.base.Strings;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;

public final class PanelUtil {

    private PanelUtil(){}

    /**
     * The contribution to the header performed implicitly by {@link PanelAbstract}.
     */
    public static void renderHead(final IHeaderResponse response, final Class<?> cls) {
        final CssResourceReference cssResourceReference = cssResourceReferenceFor(cls);
        if(cssResourceReference == null) {
            return;
        }
        final CssReferenceHeaderItem forReference = CssHeaderItem.forReference(cssResourceReference);
        response.render(forReference);
    }

    public static CssResourceReference cssResourceReferenceFor(final Class<?> cls) {
        return cssResourceReferenceFor(cls, null);
    }
    
    public static CssResourceReference cssResourceReferenceFor(final Class<?> cls, final String suffix) {
        final String url = cssFor(cls, suffix);
        if(url == null) {
            return null;
        }
        return new CssResourceReference(cls, url);
    }

    private static String cssFor(final Class<?> cls, String suffix) {
        if(cls == null) {
            return null;
        }
        String simpleName = cls.getSimpleName();
        if(Strings.isNullOrEmpty(simpleName)) {
            return null; // eg inner classes
        }
        String string;
        if (suffix != null) {
            string = ("-" + suffix);
        } else {
            string = "";
        }
        return simpleName + string + ".css";
    }
}
