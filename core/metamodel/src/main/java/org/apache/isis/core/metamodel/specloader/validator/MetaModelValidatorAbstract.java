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

import org.apache.isis.applib.id.Identifier;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class MetaModelValidatorAbstract 
implements MetaModelValidator, MetaModelContextAware {

    protected final ValidationFailures failures = new ValidationFailures();
    
    @Getter @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;
    
    /**
     * Collect any {@link ValidationFailure} to given validationFailures. 
     *  
     * @param validationFailures
     */
    public void collectFailuresInto(@NonNull ValidationFailures validationFailures) {
        validationFailures.addAll(failures);
    }

    @Override
    public void onFailure(
            @NonNull FacetHolder facetHolder, 
            @NonNull Identifier deficiencyOrigin,
            @NonNull String deficiencyMessageFormat, 
            Object... args) {
        
        MetaModelValidator.super.onFailure(facetHolder, deficiencyOrigin, deficiencyMessageFormat, args);
        failures.add(deficiencyOrigin, deficiencyMessageFormat, args);
    }
    
    protected IsisConfiguration getConfiguration() {
        return metaModelContext.getConfiguration();
    }

    @Override
    public String toString() {
        return getClass().getName();
    }

}
