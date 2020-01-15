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

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacetViaClearMethod;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacetViaSetterMethod;
import org.apache.isis.core.metamodel.facets.properties.update.init.PropertyInitializationFacetViaSetterMethod;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacetViaSetterMethod;

public class PropertySetAndClearFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.ofCollection(_Lists.of(
            MethodLiteralConstants.SET_PREFIX, 
            MethodLiteralConstants.CLEAR_PREFIX));

    public PropertySetAndClearFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final Method setMethod = attachPropertyModifyFacetIfSetterIsFound(processMethodContext);
        final Method clearMethod = attachPropertyClearFacetIfClearMethodIsFound(processMethodContext);

        attachPropertyClearFacetUsingSetterIfRequired(processMethodContext, setMethod, clearMethod);
    }

    /**
     * Sets up the {@link PropertySetterFacetViaSetterMethod} to invoke the
     * property's setter if available, but if none then marks the property as
     * {@link NotPersistableFacet not-persistable} and {@link DisabledFacet
     * disabled} otherwise.
     */
    private static Method attachPropertyModifyFacetIfSetterIsFound(final ProcessMethodContext processMethodContext) {

        final Method getMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseName(getMethod.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Class<?> returnType = getMethod.getReturnType();
        final Class<?>[] paramTypes = new Class[] { returnType };
        final Method setMethod = MethodFinderUtils.findMethod(cls, MethodLiteralConstants.SET_PREFIX + capitalizedName, void.class, paramTypes);
        processMethodContext.removeMethod(setMethod);

        final FacetHolder property = processMethodContext.getFacetHolder();
        if (setMethod != null) {
            FacetUtil.addFacet(new PropertySetterFacetViaSetterMethod(setMethod, property));
            FacetUtil.addFacet(new PropertyInitializationFacetViaSetterMethod(setMethod, property));
        } else {
            FacetUtil.addFacet(new NotPersistableFacetInferred(property));

            // previously we also added the DisabledFacetAlwaysEverywhere facet here.
            // however, the PropertyModifyFacetFactory (which comes next) might install a PropertySetterFacet instead.
            // so, have introduced a new facet factory, to be run "near the end", to install this facet if no
            // setter facet is found to have been installed.

        }

        return setMethod;
    }

    private Method attachPropertyClearFacetIfClearMethodIsFound(final ProcessMethodContext processMethodContext) {
        final Class<?> cls = processMethodContext.getCls();
        final Method getMethod = processMethodContext.getMethod();
        final FacetHolder property = processMethodContext.getFacetHolder();

        final String capitalizedName = StringExtensions.asJavaBaseName(getMethod.getName());
        final Method clearMethod = MethodFinderUtils.findMethod(cls, MethodLiteralConstants.CLEAR_PREFIX + capitalizedName, void.class, NO_ARG);

        if (clearMethod == null) {
            return null;
        }
        processMethodContext.removeMethod(clearMethod);

        FacetUtil.addFacet(new PropertyClearFacetViaClearMethod(clearMethod, property));

        return clearMethod;
    }

    private static void attachPropertyClearFacetUsingSetterIfRequired(final ProcessMethodContext processMethodContext, final Method setMethod, final Method clearMethod) {

        if (clearMethod != null) {
            return;
        }
        if (setMethod == null) {
            return;
        }
        final FacetHolder property = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new PropertyClearFacetViaSetterMethod(setMethod, property));
    }

}
