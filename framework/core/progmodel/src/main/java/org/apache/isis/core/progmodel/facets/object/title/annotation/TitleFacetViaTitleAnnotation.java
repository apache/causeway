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

package org.apache.isis.core.progmodel.facets.object.title.annotation;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.util.AdapterInvokeUtils;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacetAbstract;
import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Strings;

public class TitleFacetViaTitleAnnotation extends TitleFacetAbstract  {

    private static final Logger LOG = Logger.getLogger(TitleFacetViaTitleAnnotation.class);
    private final List<TitleComponent> components;

    public static class TitleComponent {
        public static final Function<? super Method, ? extends TitleComponent> FROM_METHOD = new Function<Method, TitleComponent>() {

            @Override
            public TitleComponent apply(Method input) {
                return TitleComponent.of(input);
            }
        };


        private final String prepend;
        private final String append;
        private final Method method;
        private TitleComponent(String prepend, String append, Method method) {
            super();
            this.prepend = prepend;
            this.append = append;
            this.method = method;
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
        public static TitleComponent of(Method method) {
            final Title annotation = method.getAnnotation(Title.class);
            final String prepend = annotation!=null?annotation.prepend():" ";
            final String append = annotation!=null?annotation.append():"";
            return new TitleComponent(prepend, append, method);
        }
    }
    public TitleFacetViaTitleAnnotation(final List<TitleComponent> components, final FacetHolder holder) {
        super(holder);
        this.components = components;
    }

    @Override
    public String title(final ObjectAdapter owningAdapter, final Localization localization) {
    	StringBuilder stringBuilder = new StringBuilder();

        try {
        	for (TitleComponent entry : this.components) {
        		final Object titlePart = AdapterInvokeUtils.invoke(entry.getMethod(), owningAdapter);
        		if(titlePart == null) {
                    continue;
                }
        		final String trim = titlePart.toString().trim();
                if (Strings.isNullOrEmpty(trim)) {
                    continue;
                }
                if(stringBuilder.length() > 0) {
                    stringBuilder.append(entry.getPrepend());
                }
                stringBuilder.append(trim);
                stringBuilder.append(entry.getAppend());
        	}

        	return stringBuilder.toString().trim();
        } catch (final RuntimeException ex) {
            LOG.warn("Title failure", ex);
            return "Failed Title";
        }
    }

    public List<TitleComponent> getComponents() {
        return components;
    }

}
