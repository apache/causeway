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

package org.apache.isis.core.progmodel.facets.properties.modify;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.lang.NameUtils;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.object.notpersistable.NotPersistableFacet;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.progmodel.facets.MethodFinderUtils;
import org.apache.isis.core.progmodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.progmodel.facets.MethodPrefixConstants;
import org.apache.isis.core.progmodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.progmodel.facets.members.disabled.staticmethod.DisabledFacetAlwaysEverywhere;
import org.apache.isis.core.progmodel.facets.properties.derived.inferred.NotPersistableFacetInferred;

public class PropertySetAndClearFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String[] PREFIXES = { MethodPrefixConstants.SET_PREFIX, MethodPrefixConstants.CLEAR_PREFIX };

    public PropertySetAndClearFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY, PREFIXES);
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
        final String capitalizedName = NameUtils.javaBaseName(getMethod.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Class<?> returnType = getMethod.getReturnType();
        final Class<?>[] paramTypes = new Class[] { returnType };
        final Method setMethod = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, MethodPrefixConstants.SET_PREFIX + capitalizedName, void.class, paramTypes);
        processMethodContext.removeMethod(setMethod);

        final FacetHolder property = processMethodContext.getFacetHolder();
        if (setMethod != null) {
            FacetUtil.addFacet(new PropertySetterFacetViaSetterMethod(setMethod, property));
            FacetUtil.addFacet(new PropertyInitializationFacetViaSetterMethod(setMethod, property));
        } else {
            FacetUtil.addFacet(new NotPersistableFacetInferred(property));
            FacetUtil.addFacet(new DisabledFacetAlwaysEverywhere(property));
        }

        return setMethod;
    }

    private Method attachPropertyClearFacetIfClearMethodIsFound(final ProcessMethodContext processMethodContext) {
        final Class<?> cls = processMethodContext.getCls();
        final Method getMethod = processMethodContext.getMethod();
        final FacetHolder property = processMethodContext.getFacetHolder();

        final String capitalizedName = NameUtils.javaBaseName(getMethod.getName());
        final Method clearMethod = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, MethodPrefixConstants.CLEAR_PREFIX + capitalizedName, void.class, NO_PARAMETERS_TYPES);

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
