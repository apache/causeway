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

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.viewer.wicket.ui.selector.links.LinksSelectorPanelAbstract;

public final class PanelUtil {

    private PanelUtil(){}

    /**
     * The contribution to the header performed implicitly by {@link PanelAbstract}.
     * 
     * <p>
     * Factored out for reuse by {@link LinksSelectorPanelAbstract}.
     */
    public static void renderHead(final IHeaderResponse response, final Class<?> cls) {
        final CssResourceReference cssResourceReference = cssResourceReferenceFor(cls);
        if(cssResourceReference == null) {
            return;
        }
        final CssReferenceHeaderItem forReference = CssHeaderItem.forReference(cssResourceReference);
        response.render(forReference);
    }

    private static CssResourceReference cssResourceReferenceFor(final Class<?> cls) {
        final String url = cssFor(cls);
        if(url == null) {
            return null;
        }
        return new CssResourceReference(cls, url);
    }

    public static Iterable<CssResourceReference> cssResourceReferencesFor(final Class<?>... classes) {
        final List<CssResourceReference> cssResourceReferences = Lists.newArrayList();
        for (Class<?> cls : classes) {
            final CssResourceReference cssResourceReference = cssResourceReferenceFor(cls);
            if(cssResourceReference != null) {
                cssResourceReferences.add(cssResourceReference);
            }
        }
        return cssResourceReferences;
    }
    
    private static String cssFor(final Class<?> cls) {
        String simpleName = cls.getSimpleName();
        if(Strings.isNullOrEmpty(simpleName)) {
            return null; // eg inner classes
        }
        return simpleName + ".css";
    }
}
