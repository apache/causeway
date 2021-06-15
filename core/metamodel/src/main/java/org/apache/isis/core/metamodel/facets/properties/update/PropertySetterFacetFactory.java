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

import javax.inject.Inject;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacetViaSetterMethod;
import org.apache.isis.core.metamodel.facets.properties.update.init.PropertyInitializationFacetViaSetterMethod;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacetViaSetterMethod;
import org.apache.isis.core.metamodel.methods.MethodFinderUtils;
import org.apache.isis.core.metamodel.methods.MethodLiteralConstants;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

public class PropertySetterFacetFactory
extends MethodPrefixBasedFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.empty();

    @Inject
    public PropertySetterFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final Method setMethod = attachPropertyModifyFacetIfSetterIsFound(processMethodContext);

        attachPropertyClearFacetUsingSetterIfRequired(processMethodContext, setMethod);
    }

    /**
     * Sets up the {@link PropertySetterFacetViaSetterMethod} to invoke the
     * property's setter if available, but if none then marks the property as
     * {@link NotPersistableFacet not-persistable} and {@link DisabledFacet
     * disabled} otherwise.
     */
    private Method attachPropertyModifyFacetIfSetterIsFound(
            final ProcessMethodContext processMethodContext) {

        final Method getMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseName(getMethod.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Class<?> returnType = getMethod.getReturnType();
        final Class<?>[] paramTypes = new Class[] { returnType };
        final Method setMethod = MethodFinderUtils
                .findMethod(cls, MethodLiteralConstants.SET_PREFIX + capitalizedName, void.class, paramTypes);
        processMethodContext.removeMethod(setMethod);

        final FacetHolder property = processMethodContext.getFacetHolder();
        if (setMethod != null) {
            addFacetIfPresent(new PropertySetterFacetViaSetterMethod(setMethod, property));
            addFacetIfPresent(new PropertyInitializationFacetViaSetterMethod(setMethod, property));
        } else {
            addFacetIfPresent(new SnapshotExcludeFacetInferred(property));

            // previously we also added the DisabledFacetAlwaysEverywhere facet here.
            // however, the PropertyModifyFacetFactory (which comes next) might install a PropertySetterFacet instead.
            // so, have introduced a new facet factory, to be run "near the end", to install this facet if no
            // setter facet is found to have been installed.

        }

        return setMethod;
    }

    private void attachPropertyClearFacetUsingSetterIfRequired(
            final ProcessMethodContext processMethodContext, final Method setMethod) {

        if (setMethod == null) {
            return;
        }
        final FacetHolder property = processMethodContext.getFacetHolder();
        addFacetIfPresent(new PropertyClearFacetViaSetterMethod(setMethod, property));
    }

}
