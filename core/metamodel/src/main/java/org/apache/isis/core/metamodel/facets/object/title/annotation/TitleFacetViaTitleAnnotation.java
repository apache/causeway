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

package org.apache.isis.core.metamodel.facets.object.title.annotation;

import java.lang.reflect.Method;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacetAbstract;

public class TitleFacetViaTitleAnnotation extends TitleFacetAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(TitleFacetViaTitleAnnotation.class);
    private final List<TitleComponent> components;
    private final AdapterManager adapterManager;

    public static class TitleComponent {
        public static final Function<? super Method, ? extends TitleComponent> FROM_METHOD = new Function<Method, TitleComponent>() {

            @Override
            public TitleComponent apply(final Method input) {
                return TitleComponent.of(input);
            }
        };

        private final String prepend;
        private final String append;
        private final Method method;
        private final int abbreviateTo;

        private TitleComponent(final String prepend, final String append, final Method method, final int abbreviateTo) {
            super();
            this.prepend = prepend;
            this.append = append;
            this.method = method;
            this.abbreviateTo = abbreviateTo;
        }

        public String getPrepend() {
            return prepend;
        }

        public String getAppend() {
            return append;
        }

        public Method getMethod() {
            return method;
        }

        public static TitleComponent of(final Method method) {
            final Title annotation = method.getAnnotation(Title.class);
            final String prepend = annotation != null ? annotation.prepend() : " ";
            final String append = annotation != null ? annotation.append() : "";
            final int abbreviateTo = annotation != null ? annotation.abbreviatedTo() : Integer.MAX_VALUE;
            return new TitleComponent(prepend, append, method, abbreviateTo);
        }
    }

    public TitleFacetViaTitleAnnotation(final List<TitleComponent> components, final FacetHolder holder, final AdapterManager adapterManager) {
        super(holder);
        this.components = components;
        this.adapterManager = adapterManager;
    }

    @Override
    public String title(final ObjectAdapter targetAdapter, final Localization localization) {
        return title(null, targetAdapter, localization);
    }

    private String titleOf(final ObjectAdapter adapter) {
        if (adapter == null) {
            return null;
        }
        return adapter.titleString(null);
    }

    public List<TitleComponent> getComponents() {
        return components;
    }

    private static String abbreviated(final String str, final int maxLength) {
        return str.length() < maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }

    @Override
    public String title(ObjectAdapter contextAdapter, ObjectAdapter targetAdapter, Localization localization) {
        final StringBuilder stringBuilder = new StringBuilder();

        try {
            for (final TitleComponent component : this.components) {
                final Object titlePart = ObjectAdapter.InvokeUtils.invoke(component.getMethod(), targetAdapter);
                if (titlePart == null) {
                    continue;
                } 
                // ignore context, if provided
                final ObjectAdapter titlePartAdapter = adapterManager.adapterFor(titlePart);
                if(Objects.equal(contextAdapter, titlePartAdapter)) {
                    continue;
                }
                String title = titleOf(titlePartAdapter);
                if (Strings.isNullOrEmpty(title)) {
                    // ... use the toString() otherwise
                    // (mostly for benefit of testing...)
                    title = titlePart.toString().trim();
                }
                if(Strings.isNullOrEmpty(title)) {
                    continue;
                }
                stringBuilder.append(component.getPrepend());
                stringBuilder.append(abbreviated(title, component.abbreviateTo));
                stringBuilder.append(component.getAppend());
            }

            return stringBuilder.toString().trim();
        } catch (final RuntimeException ex) {
            LOG.warn("Title failure", ex);
            return "Failed Title";
        }
    }
}
