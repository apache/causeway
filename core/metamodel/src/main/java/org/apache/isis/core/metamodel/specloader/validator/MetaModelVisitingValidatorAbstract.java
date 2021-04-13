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

import lombok.val;

public abstract class MetaModelVisitingValidatorAbstract 
extends MetaModelValidatorAbstract
implements MetaModelVisitingValidator {

    @Override
    public final void validate() {

        val isActionExplicit = getConfiguration().getApplib().getAnnotation().getAction().isExplicit();
        
        super.getMetaModelContext().getSpecificationLoader()
        .forEach(spec->{
            
            if(!isActionExplicit
                    && spec.getBeanSort().isUnknown()) {
                    return; // in support of @Action not being forced, we need to relax 
            }
            
            validate(spec);
        });
        
        summarize();
        
    }
    
}
