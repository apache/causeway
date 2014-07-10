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

package org.apache.isis.core.metamodel.facets.properties.mandatory.staticmethod;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.lang.MethodExtensions;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodPrefixConstants;

public class MandatoryFacetOnPropertyStaticMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String[] PREFIXES = { MethodPrefixConstants.OPTIONAL_PREFIX };

    public MandatoryFacetOnPropertyStaticMethodFactory() {
        super(FeatureType.PROPERTIES_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        attachMandatoryFacetIfOptionalMethodIsFound(processMethodContext);
    }

    private static void attachMandatoryFacetIfOptionalMethodIsFound(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();

        final String capitalizedName = StringExtensions.asJavaBaseName(method.getName());
        final Class<?> returnType = method.getReturnType();

        final Class<?> cls = processMethodContext.getCls();
        final Method optionalMethod = MethodFinderUtils.findMethod(cls, MethodScope.CLASS, MethodPrefixConstants.OPTIONAL_PREFIX + capitalizedName, boolean.class, NO_PARAMETERS_TYPES);
        processMethodContext.removeMethod(optionalMethod);

        if (!indicatesOptional(optionalMethod)) {
            return;
        }
        if (returnType.isPrimitive()) {
            throw new MetaModelException(cls.getName() + "#" + capitalizedName + " cannot be an optional property as it is of a primitive type");
        }
        final FacetHolder property = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new MandatoryFacetOnPropertyStaticMethod(property));
    }

    private static boolean indicatesOptional(final Method method) {
        if (method != null) {
            Boolean optionalMethodReturnValue = null;
            try {
                optionalMethodReturnValue = (Boolean) MethodExtensions.invoke(method, new Object[0]);
            } catch (final ClassCastException ex) {
                // ignore
            }
            if (optionalMethodReturnValue == null) {
                throw new MetaModelException("method " + method + " should return a boolean");
            }
            return optionalMethodReturnValue.booleanValue();
        }
        return false;
    }

}