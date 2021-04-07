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

package org.apache.isis.core.metamodel.facets.object.disabled.method;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.disabled.DisabledObjectFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

import lombok.val;

/**
 * Installs the {@link DisabledObjectFacetViaMethod} on the
 * {@link ObjectSpecification}, and copies this facet onto each
 * {@link ObjectMember}.
 *
 * <p>
 * This two-pass design is required because, at the time that the
 * {@link #process(FacetFactory.ProcessClassContext)
 * class is being processed}, the {@link ObjectMember member}s for the
 * {@link ObjectSpecification spec} are not known.
 */
public class DisabledObjectFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.ofSingleton(MethodLiteralConstants.DISABLED);

    public DisabledObjectFacetViaMethodFactory() {
        super(FeatureType.EVERYTHING_BUT_PARAMETERS, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();
        val paramTypes = new Class<?>[] {Identifier.Type.class};

        val method = MethodFinderUtils.findMethod_returningText(
                cls, MethodLiteralConstants.DISABLED,
                paramTypes);
        if (method == null) {
            return;
        }

        val translationService = getTranslationService();
        // sadness: same logic as in I18nFacetFactory
        val translationContext = TranslationContext.forMethod(method);
        
        FacetUtil.addFacet(new DisabledObjectFacetViaMethod(method, translationService, translationContext, facetHolder));

        processClassContext.removeMethod(method);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final FacetedMethod member = processMethodContext.getFacetHolder();
        final Class<?> owningClass = processMethodContext.getCls();
        final ObjectSpecification owningSpec = getSpecificationLoader().loadSpecification(owningClass);
        final DisabledObjectFacet facet = owningSpec.getFacet(DisabledObjectFacet.class);
        if (facet != null) {
            facet.copyOnto(member);
        }
    }
}
