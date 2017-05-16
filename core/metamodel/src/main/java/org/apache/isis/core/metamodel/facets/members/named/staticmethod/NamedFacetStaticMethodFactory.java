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

package org.apache.isis.core.metamodel.facets.members.named.staticmethod;

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
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedMethodPrefix;

/**
 * Sets up a {@link NamedFacet} if a {@value MethodPrefixConstants#NAME_PREFIX}
 * -prefixed method is present.
 *
 * @deprecated
 */
@Deprecated
public class NamedFacetStaticMethodFactory extends MethodPrefixBasedFacetFactoryAbstract implements MetaModelValidatorRefiner {

    private static final String[] PREFIXES = { MethodPrefixConstants.NAME_PREFIX };

    private final MetaModelValidatorForDeprecatedMethodPrefix validator = new MetaModelValidatorForDeprecatedMethodPrefix(PREFIXES[0]);

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public NamedFacetStaticMethodFactory() {
        super(FeatureType.MEMBERS, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        // namedXxx()
        attachNamedFacetIfNamedMethodIsFound(processMethodContext);

    }

    public void attachNamedFacetIfNamedMethodIsFound(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseNameStripAccessorPrefixIfRequired(method.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Method nameMethod = MethodFinderUtils.findMethod(cls, MethodScope.CLASS, MethodPrefixConstants.NAME_PREFIX + capitalizedName, String.class, new Class[0]);

        if (nameMethod == null) {
            return;
        }

        processMethodContext.removeMethod(nameMethod);
        final String name = invokeNameMethod(nameMethod);

        final FacetHolder facetHolder = processMethodContext.getFacetHolder();
        final NamedFacetStaticMethod facet = new NamedFacetStaticMethod(name, nameMethod, facetHolder);
        FacetUtil.addFacet(validator.flagIfPresent(facet, processMethodContext));
    }

    private static String invokeNameMethod(final Method nameMethod) {
        String name = null;
        try {
            name = (String) MethodExtensions.invokeStatic(nameMethod);
        } catch (final ClassCastException e) {
            // ignore
        }
        if (name == null) {
            throw new MetaModelException("method " + nameMethod + "must return a string");
        }
        return name;
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
