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
package org.apache.isis.core.metamodel.facets.object.objectspecid;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;

/**
 * DomainObjects must have a non-empty namespace,
 * eg. @DomainObject(objectType="Customer") is considered invalid, 
 * whereas eg. @DomainObject(objectType="sales.Customer") is valid.
 * 
 * @since 2.0
 */
public class ObjectSpecIdMalformedValidator
implements MetaModelRefiner {

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        
        programmingModel.addVisitingValidator(spec->{
                    
            if(!spec.isEntityOrViewModel()
                    && !spec.isManagedBean() ) {
                return;   
            }
            
            val objectSpecIdFacet = spec.getFacet(ObjectSpecIdFacet.class);
            if(objectSpecIdFacet == null) {
                return;
            }
            
            val logicalTypeName = objectSpecIdFacet.value();
            
            val nameParts = _Strings.splitThenStream(logicalTypeName, ".")
                    .collect(Can.toCan());
            
            if(!nameParts.getCardinality().isMultiple()
                    || nameParts.stream()
                        .anyMatch(String::isEmpty)) {
                
                ValidationFailure.raiseFormatted(
                        spec,
                        "%s: the object type must declare a namespace, yet was found to be invalid '%s'; "
                        + "eg. @DomainObject(objectType=\"Customer\") is considered invalid, "
                        + "whereas @DomainObject(objectType=\"sales.Customer\") is valid.",
                        spec.getFullIdentifier(),
                        logicalTypeName);
            }
    
        });

    }
}
