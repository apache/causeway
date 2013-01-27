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

import org.apache.isis.viewer.wicket.ui.selector.links.LinksSelectorPanelAbstract;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;

import com.google.common.base.Strings;

public final class PanelUtil {

    private PanelUtil(){}

    /**
     * The contribution to the header performed implicitly by {@link PanelAbstract}.
     * 
     * <p>
     * Factored out for reuse by {@link LinksSelectorPanelAbstract}.
     */
    public static void renderHead(final IHeaderResponse response, final Class<?> cls) {
        String simpleName = cls.getSimpleName();
        if(Strings.isNullOrEmpty(simpleName)) {
            return; // eg inner classes
        }
        final String url = simpleName + ".css";
        response.render(CssHeaderItem.forReference(new CssResourceReference(cls, url)));
    }
}
