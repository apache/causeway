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
package org.apache.causeway.core.metamodel.facets.object.ignore.annotation;

import java.util.function.Predicate;

import javax.inject.Inject;

import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.config.beans.CausewayBeanTypeClassifier.Attributes;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;

public class RemoveAnnotatedMethodsFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public RemoveAnnotatedMethodsFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        var policy = getMetaModelContext().getConfiguration().getCore().getMetaModel().getIntrospector().getPolicy();
        switch (policy) {
            case ENCAPSULATION_ENABLED:
                getClassCache()
                        .streamResolvedMethods(processClassContext.getCls())
                        /* honor exclude markers (always) */
                        .filter(method->{
                            if(ProgrammingModelConstants.MethodExcludeMarker.anyMatchOn(method)) {
                                processClassContext.removeMethod(method);
                                return false; // stop processing
                            }
                            return true; // continue processing
                        })
                        /* don't throw away mixin main methods,
                         * those we keep irrespective of IntrospectionPolicy */
                        .filter(_Predicates.not(isMixinMainMethod(processClassContext)))
                        .forEach(method -> {
                            if (!ProgrammingModelConstants.MethodIncludeMarker.anyMatchOn(method)) {
                                processClassContext.removeMethod(method);
                            }
                        });
                break;

            case ANNOTATION_REQUIRED:
                // TODO: this could probably be more precise and insist on @Domain.Include for members.

            case ANNOTATION_OPTIONAL:

                getClassCache()
                        .streamPublicMethods(processClassContext.getCls())
                        .forEach(method->{
                            if(ProgrammingModelConstants.MethodExcludeMarker.anyMatchOn(method)) {
                                processClassContext.removeMethod(method);
                            }
                        });

                break;
        }
    }

    // -- HELPER

    /**
     * We have no MixinFacet yet, so we need to revert to low level introspection tactics.
     */
    private Predicate<ResolvedMethod> isMixinMainMethod(final @NonNull ProcessClassContext processClassContext) {

        // shortcut, when we already know the class is not a mixin
        if(processClassContext.getFacetHolder() instanceof ObjectSpecification) {
            var spec = (ObjectSpecification) processClassContext.getFacetHolder();
            if(!spec.getBeanSort().isMixin()) {
                return method->false;
            }
        }
        // lookup attribute from class-cache as it should have been already processed by the BeanTypeClassifier
        var cls = processClassContext.getCls();
        var mixinMainMethodName = Attributes.MIXIN_MAIN_METHOD_NAME.lookup(getClassCache(), cls)
            .orElse(null);
        return method->method.name().equals(mixinMainMethodName);
    }

}
