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
package org.apache.isis.core.metamodel.facets.object.defaults.annotcfg;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Defaulted;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultsProviderUtil;

import lombok.val;

public class DefaultedFacetAnnotationElseConfigurationFactory
extends FacetFactoryAbstract {

    @Inject
    public DefaultedFacetAnnotationElseConfigurationFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();
        val config = super.getConfiguration();
        val defaultedIfAny = processClassContext.synthesizeOnType(Defaulted.class);

        addFacetIfPresent(

            // create from annotation, if present
            defaultedIfAny
                .flatMap(defaultedAnnot->DefaultedFacetAnnotation.create(config, cls, facetHolder))
            .or(

                // otherwise, try to create from configuration, if present
                ()->{
                    val providerName = DefaultsProviderUtil.defaultsProviderNameFromConfiguration(config, cls);
                    return _Strings.isNotEmpty(providerName)
                        ? DefaultedFacetFromConfiguration.create(providerName, facetHolder)
                        : Optional.empty();
                })
        );
    }


}
