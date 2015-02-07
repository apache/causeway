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

package org.apache.isis.core.metamodel.facets.actions.notinservicemenu.method;

import java.lang.reflect.Method;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedMethodPrefix;

/**
 * @deprecated
 */
@Deprecated
public class NotInServiceMenuFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract implements MetaModelValidatorRefiner, IsisConfigurationAware {

    public static final String NOT_IN_SERVICE_MENU_PREFIX = "notInServiceMenu";

    private final MetaModelValidatorForDeprecatedMethodPrefix validator = new MetaModelValidatorForDeprecatedMethodPrefix(NOT_IN_SERVICE_MENU_PREFIX);

    public NotInServiceMenuFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Method getMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseNameStripAccessorPrefixIfRequired(getMethod.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Method notInServiceMenuMethod = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, 
                NOT_IN_SERVICE_MENU_PREFIX + capitalizedName, boolean.class, new Class[] {});
        if (notInServiceMenuMethod == null) {
            return;
        }

        processMethodContext.removeMethod(notInServiceMenuMethod);

        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        final NotInServiceMenuFacetViaMethod facet = new NotInServiceMenuFacetViaMethod(notInServiceMenuMethod, facetedMethod);
        FacetUtil.addFacet(validator.flagIfPresent(facet, processMethodContext));
    }

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(validator);
    }

    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
        validator.setConfiguration(configuration);
    }

}
