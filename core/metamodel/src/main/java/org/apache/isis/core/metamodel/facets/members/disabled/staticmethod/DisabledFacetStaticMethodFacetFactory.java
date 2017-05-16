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

package org.apache.isis.core.metamodel.facets.members.disabled.staticmethod;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.lang.MethodExtensions;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodPrefixConstants;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedMethodPrefix;

/**
 * @deprecated
 */
@Deprecated
public class DisabledFacetStaticMethodFacetFactory extends MethodPrefixBasedFacetFactoryAbstract implements MetaModelValidatorRefiner {

    private static final String[] PREFIXES = { MethodPrefixConstants.PROTECT_PREFIX };

    private final MetaModelValidatorForDeprecatedMethodPrefix validator = new MetaModelValidatorForDeprecatedMethodPrefix(PREFIXES[0]);

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public DisabledFacetStaticMethodFacetFactory() {
        super(FeatureType.MEMBERS, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        attachDisabledFacetIfProtectMethodIsFound(processMethodContext);
    }

    public void attachDisabledFacetIfProtectMethodIsFound(final ProcessMethodContext processMethodContext) {

        final Class<?>[] paramTypes = new Class[] {};

        final Class<?> type = processMethodContext.getCls();
        final Method method = processMethodContext.getMethod();

        final String capitalizedName = StringExtensions.asJavaBaseNameStripAccessorPrefixIfRequired(method.getName());

        final Method protectMethod = MethodFinderUtils.findMethodWithOrWithoutParameters(type, MethodScope.CLASS, MethodPrefixConstants.PROTECT_PREFIX + capitalizedName, boolean.class, paramTypes);
        if (protectMethod == null) {
            return;
        }

        processMethodContext.removeMethod(protectMethod);

        final Boolean protectMethodReturnValue = invokeProtectMethod(protectMethod);
        if (!protectMethodReturnValue.booleanValue()) {
            return;
        }

        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        final DisabledFacetForStaticMethod facet = new DisabledFacetForStaticMethod(facetedMethod);
        FacetUtil.addFacet(validator.flagIfPresent(facet, processMethodContext));
    }

    private static Boolean invokeProtectMethod(final Method protectMethod) {
        Boolean protectMethodReturnValue = null;
        try {
            protectMethodReturnValue = (Boolean) MethodExtensions.invokeStatic(protectMethod);
        } catch (final ClassCastException ex) {
            // ignore
        }
        if (protectMethodReturnValue == null) {
            throw new MetaModelException("method " + protectMethod + "must return a boolean");
        }
        return protectMethodReturnValue;
    }


    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(validator);
    }

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        IsisConfiguration configuration = servicesInjector.getConfigurationServiceInternal();
        validator.setConfiguration(configuration);
    }

}
