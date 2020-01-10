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

package org.apache.isis.metamodel.facets.object.value.annotcfg;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.value.ValueFacetAbstract;
import org.apache.isis.metamodel.facets.object.value.vsp.ValueSemanticsProviderUtil;

public class ValueFacetAnnotation extends ValueFacetAbstract {

    public ValueFacetAnnotation(
            IsisConfiguration config,
            Class<?> annotatedClass, 
            FacetHolder holder) {

        this(
                semanticsProviderName(config, annotatedClass), 
                semanticsProviderClass(annotatedClass),
                holder);
    }

    /**
     * Always valid, even if the specified semanticsProviderName might have been
     * wrong.
     */
    @Override
    public boolean isValid() {
        return true;
    }
    
    // -- HELPER
    
    private ValueFacetAnnotation(
            String candidateSemanticsProviderName, 
            Class<?> candidateSemanticsProviderClass, 
            FacetHolder holder) {

        super(ValueSemanticsProviderUtil
                .valueSemanticsProviderOrNull(candidateSemanticsProviderClass, candidateSemanticsProviderName), 
                AddFacetsIfInvalidStrategy.DO_ADD, 
                holder);
    }
    
    private static String semanticsProviderName(IsisConfiguration config, Class<?> annotatedClass) {
        
        final Value annotation = annotatedClass.getAnnotation(Value.class);
        final String semanticsProviderName = annotation.semanticsProviderName();
        if (!_Strings.isNullOrEmpty(semanticsProviderName)) {
            return semanticsProviderName;
        }
        return ValueSemanticsProviderUtil
                .semanticsProviderNameFromConfiguration(config, annotatedClass);
    }

    private static Class<?> semanticsProviderClass(final Class<?> annotatedClass) {
        final Value annotation = annotatedClass.getAnnotation(Value.class);
        return annotation.semanticsProviderClass();
    }


}
