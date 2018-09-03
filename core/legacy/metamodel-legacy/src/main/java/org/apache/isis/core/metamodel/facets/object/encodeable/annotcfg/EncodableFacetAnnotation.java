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

package org.apache.isis.core.metamodel.facets.object.encodeable.annotcfg;

import com.google.common.base.Strings;

import org.apache.isis.applib.annotation.Encodable;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncoderDecoderUtil;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class EncodableFacetAnnotation extends EncodableFacetAbstract {

    private static String encoderDecoderName(final Class<?> annotatedClass, final IsisConfiguration configuration) {
        final Encodable annotation = annotatedClass.getAnnotation(Encodable.class);
        final String encoderDecoderName = annotation.encoderDecoderName();
        if (!Strings.isNullOrEmpty(encoderDecoderName)) {
            return encoderDecoderName;
        }
        return EncoderDecoderUtil.encoderDecoderNameFromConfiguration(annotatedClass, configuration);
    }

    private static Class<?> encoderDecoderClass(final Class<?> annotatedClass) {
        final Encodable annotation = annotatedClass.getAnnotation(Encodable.class);
        return annotation.encoderDecoderClass();
    }

    public EncodableFacetAnnotation(final Class<?> annotatedClass, final FacetHolder holder, final ServicesInjector servicesInjector) {
        this(encoderDecoderName(annotatedClass, servicesInjector.getConfigurationServiceInternal()),
                encoderDecoderClass(annotatedClass), holder, 
                servicesInjector.getPersistenceSessionServiceInternal(), servicesInjector);
    }

    private EncodableFacetAnnotation(final String candidateEncoderDecoderName, final Class<?> candidateEncoderDecoderClass, final FacetHolder holder, final ObjectAdapterProvider adapterProvider, final ServicesInjector dependencyInjector) {
        super(candidateEncoderDecoderName, candidateEncoderDecoderClass, holder, adapterProvider, dependencyInjector);
    }

}
