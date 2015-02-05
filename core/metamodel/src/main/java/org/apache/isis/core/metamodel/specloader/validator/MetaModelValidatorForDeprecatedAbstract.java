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
package org.apache.isis.core.metamodel.specloader.validator;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;

public abstract class MetaModelValidatorForDeprecatedAbstract extends MetaModelValidatorAbstract implements IsisConfigurationAware {

    public static final String ISIS_REFLECTOR_ALLOW_DEPRECATED_KEY = "isis.reflector.validator.allowDeprecated";
    public static final boolean ISIS_REFLECTOR_ALLOW_DEPRECATED_DEFAULT = true;

    private final ValidationFailures failures = new ValidationFailures();

    private IsisConfiguration configuration;

    public <T extends Facet> T invalidIfPresent(final T facet) {
        if(facet != null) {
            failures.add(failureMessageFor(facet));
        }
        return facet;
    }

    protected abstract String failureMessageFor(final Facet facet);

    public void addFacet(final Facet facet) {
        FacetUtil.addFacet(invalidIfPresent(facet));
    }

    @Override
    public void validate(final ValidationFailures validationFailures) {
        if(configuration.getBoolean(ISIS_REFLECTOR_ALLOW_DEPRECATED_KEY, ISIS_REFLECTOR_ALLOW_DEPRECATED_DEFAULT)) {
            return;
        }
        validationFailures.addAll(failures);
    }

    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
        this.configuration = configuration;
    }

}
