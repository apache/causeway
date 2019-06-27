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
package org.apache.isis.metamodel.specloader.validator;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.FacetFactory;

public abstract class MetaModelValidatorForDeprecatedAbstract extends MetaModelValidatorAbstract {

    public static final String ISIS_REFLECTOR_ALLOW_DEPRECATED_KEY = "isis.reflector.validator.allowDeprecated";
    public static final boolean ISIS_REFLECTOR_ALLOW_DEPRECATED_DEFAULT = true;

    private final ValidationFailures failures = new ValidationFailures();

    private IsisConfiguration configuration;

    /**
     * @param facet
     */
    public <T extends Facet> T flagIfPresent(final T facet) {
        if(facet != null) {
            failures.add(failureMessageFor(facet, null));
        }
        return facet;
    }

    /**
     * @param facet
     * @param processMethodContext - can be null if none available.
     */
    public <T extends Facet> T flagIfPresent(final T facet, final FacetFactory.AbstractProcessWithMethodContext processMethodContext) {
        if(facet != null) {
            failures.add(failureMessageFor(facet, processMethodContext));
        }
        return facet;
    }

    /**
     * Convenience for subclasses.
     */
    static boolean isInherited(final FacetFactory.AbstractProcessWithMethodContext<?> processContext) {
        if (processContext == null) {
            return false;
        }
        final Class<?> introspectedCls = processContext.getCls();
        final Class<?> declaringClass = processContext.getMethod().getDeclaringClass();
        return introspectedCls != declaringClass;
    }

    protected abstract String failureMessageFor(final Facet facet, final FacetFactory.AbstractProcessWithMethodContext<?> processMethodContext);

    @Override
    public void validate(final ValidationFailures validationFailures) {
        if(configuration.getBoolean(ISIS_REFLECTOR_ALLOW_DEPRECATED_KEY, ISIS_REFLECTOR_ALLOW_DEPRECATED_DEFAULT)) {
            return;
        }
        validationFailures.addAll(failures);
    }

    public void setConfiguration(final IsisConfiguration configuration) {
        this.configuration = configuration;
    }

}
