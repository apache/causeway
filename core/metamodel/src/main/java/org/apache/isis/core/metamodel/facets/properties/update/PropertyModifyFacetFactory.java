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

package org.apache.isis.core.metamodel.facets.properties.update;

import java.lang.reflect.Method;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodPrefixConstants;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacetViaModifyMethod;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class PropertyModifyFacetFactory extends MethodPrefixBasedFacetFactoryAbstract implements ServicesInjectorAware {

    private static final String[] PREFIXES = { MethodPrefixConstants.MODIFY_PREFIX };
    private ServicesInjector servicesInjector;

    public PropertyModifyFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        attachPropertyModifyFacetIfModifyMethodIsFound(processMethodContext);
    }

    private void attachPropertyModifyFacetIfModifyMethodIsFound(final ProcessMethodContext processMethodContext) {

        final Method getMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseName(getMethod.getName());

        final Class<?> returnType = getMethod.getReturnType();
        final Class<?>[] paramTypes = new Class[] { returnType };

        final Class<?> cls = processMethodContext.getCls();
        final Method modifyMethod = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, MethodPrefixConstants.MODIFY_PREFIX + capitalizedName, void.class, paramTypes);

        if (modifyMethod == null) {
            return;
        }
        processMethodContext.removeMethod(modifyMethod);

        final FacetHolder property = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new PropertySetterFacetViaModifyMethod(modifyMethod, property, servicesInjector));
    }

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }
}
