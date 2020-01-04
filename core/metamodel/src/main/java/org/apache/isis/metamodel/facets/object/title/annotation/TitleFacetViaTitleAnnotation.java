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

package org.apache.isis.metamodel.facets.object.title.annotation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.apache.isis.applib.annotation.Title;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.object.title.TitleFacetAbstract;
import org.apache.isis.metamodel.spec.ManagedObject;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TitleFacetViaTitleAnnotation extends TitleFacetAbstract {

    private final List<TitleComponent> components;

    public static class TitleComponent {
        public static final Function<Annotations.Evaluator<Title>, TitleComponent> FROM_EVALUATORS = 
                titleEvaluator -> TitleComponent.of(titleEvaluator);

                private final String prepend;
                private final String append;
                private final Annotations.Evaluator<Title> titleEvaluator;
                private final int abbreviateTo;

                private TitleComponent(final String prepend, final String append, final Annotations.Evaluator<Title> titleEvaluator, final int abbreviateTo) {
                    super();
                    this.prepend = prepend;
                    this.append = append;
                    this.titleEvaluator = titleEvaluator;
                    this.abbreviateTo = abbreviateTo;
                }

                public String getPrepend() {
                    return prepend;
                }

                public String getAppend() {
                    return append;
                }

                public Annotations.Evaluator<Title> getTitleEvaluator() {
                    return titleEvaluator;
                }

                private static TitleComponent of(final Annotations.Evaluator<Title> titleEvaluator) {
                    final Title annotation = titleEvaluator.getAnnotation();
                    final String prepend = annotation != null ? annotation.prepend() : " ";
                    final String append = annotation != null ? annotation.append() : "";
                    final int abbreviateTo = annotation != null ? annotation.abbreviatedTo() : Integer.MAX_VALUE;
                    return new TitleComponent(prepend, append, titleEvaluator, abbreviateTo);
                }

                @Override
                public String toString() {
                    final List<String> parts = _Lists.newArrayList();
                    if(prepend != null && !_Strings.isNullOrEmpty(prepend.trim())) {
                        parts.add("prepend=" + prepend);
                    }
                    if(append != null && !_Strings.isNullOrEmpty(append.trim())) {
                        parts.add("append=" + append);
                    }
                    if(abbreviateTo != Integer.MAX_VALUE) {
                        parts.add("abbreviateTo=" + abbreviateTo);
                    }
                    return String.join(";", parts);
                }
    }

    public TitleFacetViaTitleAnnotation(final List<TitleComponent> components, final FacetHolder holder) {
        super(holder);
        this.components = components;
    }

    @Override
    public String title(final ManagedObject targetAdapter) {
        return title(null, targetAdapter);
    }

    private String titleOf(final ManagedObject adapter) {
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
    public String title(ManagedObject contextAdapter, ManagedObject targetAdapter) {
        val stringBuilder = new StringBuilder();
        val objectManager = getObjectManager();

        try {
            for (final TitleComponent component : this.components) {
                final Object titlePart = component.getTitleEvaluator().value(targetAdapter.getPojo());
                if (titlePart == null) {
                    continue;
                }
                // ignore context, if provided
                val titlePartAdapter = objectManager.adapt(titlePart);
                if(Objects.equals(contextAdapter, titlePartAdapter)) {
                    continue;
                }
                String title = titleOf(titlePartAdapter);
                if (_Strings.isNullOrEmpty(title)) {
                    // ... use the toString() otherwise
                    // (mostly for benefit of testing...)
                    title = titlePart.toString().trim();
                }
                if(_Strings.isNullOrEmpty(title)) {
                    continue;
                }
                stringBuilder.append(component.getPrepend());
                stringBuilder.append(abbreviated(title, component.abbreviateTo));
                stringBuilder.append(component.getAppend());
            }

            return stringBuilder.toString().trim();
        } catch (final RuntimeException ex) {
            
            val isUnitTesting = super.getMetaModelContext().getSystemEnvironment().isUnitTesting();
            
            if(!isUnitTesting) {
                log.warn("Title failure", ex);    
            }
            return "Failed Title";
        }
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        if(components != null && !_Strings.isNullOrEmpty(components.toString())) {
            attributeMap.put("components", components);
        }
    }
}
