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
package org.apache.isis.core.metamodel.facets.object.support;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.ObjectSupportMethod;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.object.cssclass.method.CssClassFacetViaCssClassMethod;
import org.apache.isis.core.metamodel.facets.object.disabled.DisabledObjectFacet;
import org.apache.isis.core.metamodel.facets.object.disabled.method.DisabledObjectFacetViaMethod;
import org.apache.isis.core.metamodel.facets.object.hidden.HiddenObjectFacet;
import org.apache.isis.core.metamodel.facets.object.hidden.method.HiddenObjectFacetViaMethod;
import org.apache.isis.core.metamodel.facets.object.icon.method.IconFacetViaIconNameMethod;
import org.apache.isis.core.metamodel.facets.object.layout.LayoutFacetViaLayoutMethod;
import org.apache.isis.core.metamodel.facets.object.title.methods.TitleFacetInferredFromToStringMethod;
import org.apache.isis.core.metamodel.facets.object.title.methods.TitleFacetViaTitleMethod;
import org.apache.isis.core.metamodel.methods.MethodFinder;
import org.apache.isis.core.metamodel.methods.MethodFinderOptions;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

import lombok.val;

/**
 * Installs {@link DisabledObjectFacetViaMethod}
 * and {@link HiddenObjectFacetViaMethod} on the
 * {@link ObjectSpecification}, and copies this facet onto each
 * {@link ObjectMember}.
 * <p>
 * This two-pass design is required because, at the time that the
 * {@link #process(FacetFactory.ProcessClassContext)
 * class is being processed}, the {@link ObjectMember member}s for the
 * {@link ObjectSpecification spec} are not known.
 * <p>
 * Also removes the {@link Object#toString()} method as action candidate,
 * regardless of whether this method is used for the domain-object's title or not
 */
public class ObjectSupportFacetFactory
extends MethodPrefixBasedFacetFactoryAbstract {

    @Inject
    public ObjectSupportFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.EVERYTHING_BUT_PARAMETERS, OrphanValidation.VALIDATE,
                Stream.of(ObjectSupportMethod.values())
                .map(ObjectSupportMethod::getMethodNames)
                .flatMap(Can::stream)
                .collect(Can.toCan()));
    }

    @Override
    public final void process(final ProcessClassContext processClassContext) {

        // priming 'toString()' into Precedence.INFERRED rank
        inferTitleFromToString(processClassContext);

        processObjectSupport(processClassContext, ObjectSupportMethod.HIDDEN, HiddenObjectFacetViaMethod::create);
        processObjectSupport(processClassContext, ObjectSupportMethod.DISABLED, DisabledObjectFacetViaMethod::create);
        processObjectSupport(processClassContext, ObjectSupportMethod.TITLE, TitleFacetViaTitleMethod::create);
        processObjectSupport(processClassContext, ObjectSupportMethod.LAYOUT, LayoutFacetViaLayoutMethod::create);
        processObjectSupport(processClassContext, ObjectSupportMethod.ICON_NAME, IconFacetViaIconNameMethod::create);
        processObjectSupport(processClassContext, ObjectSupportMethod.CSS_CLASS, CssClassFacetViaCssClassMethod::create);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final FacetedMethod member = processMethodContext.getFacetHolder();
        final Class<?> owningClass = processMethodContext.getCls();
        val owningSpec = getSpecificationLoader().loadSpecification(owningClass);

        owningSpec.lookupFacet(DisabledObjectFacet.class)
        .map(disabledObjectFacet->disabledObjectFacet.clone(member))
        .ifPresent(FacetUtil::addFacet);

        owningSpec.lookupFacet(HiddenObjectFacet.class)
        .map(hiddenObjectFacet->hiddenObjectFacet.clone(member))
        .ifPresent(FacetUtil::addFacet);
    }

    // -- HELPER

    private void inferTitleFromToString(final ProcessClassContext processClassContext) {

        val toString = ObjectSupportMethod.TO_STRING;

        MethodFinder
        .findMethod_returningCategory(
                MethodFinderOptions.publicOnly(
                        processClassContext.getCls(),
                        toString.getMethodNames()),
                toString.getReturnTypeCategory(),
                NO_ARG)
        .peek(processClassContext::removeMethod)
        .forEach(method->{
            addFacetIfPresent(TitleFacetInferredFromToStringMethod
                    .create(method, processClassContext.getFacetHolder()));
        });

    }

    private void processObjectSupport(
            final ProcessClassContext processClassContext,
            final ObjectSupportMethod objectSupportMethodEnum,
            final BiFunction<Method, FacetHolder, Optional<? extends Facet>> ojectSupportFacetConstructor) {

        MethodFinder
        .findMethod_returningCategory(
                MethodFinderOptions
                .objectSupport(
                        processClassContext.getCls(),
                        objectSupportMethodEnum.getMethodNames(),
                        processClassContext.getIntrospectionPolicy()),
                objectSupportMethodEnum.getReturnTypeCategory(),
                NO_ARG)
        .peek(processClassContext::removeMethod)
        .forEach(method->{
            addFacetIfPresent(ojectSupportFacetConstructor
                    .apply(method, processClassContext.getFacetHolder()));
        });
    }


}
